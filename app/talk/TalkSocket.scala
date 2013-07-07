package talk

import auth.User
import akka.actor._
import java.util.Date
import scala.concurrent.duration._

import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

/**
 * @author alari
 * @since 7/8/13 12:11 AM
 */
object TalkSocket {
  implicit val timeout = Timeout(1 second)

  lazy val default = Akka.system.actorOf(Props[TalkSocket])

  def join(user:Option[User], talkId: String):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(user, talkId)).map {

      case Connected(enumerator, u) =>
             println("connected")
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
        // there we are SENDING to default actor
          println("iteratee eats event")
          default ! DoTalk(u, talkId, (event \ "text").as[String])
        }.map { _ =>
          println("quit")
          default ! Quit(u, talkId)
        }

        (iteratee,enumerator)

      case CannotConnect(error) =>
        println("cannot connect")
        // Connection error

        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)
    }

  }
}

class TalkSocket extends Actor {
  val (enumerator, channel) = Concurrent.broadcast[JsValue]


  def receive = {
    case DoTalk(user, talkId, text) =>
      println(text)
      channel.push(Json.obj(
        "type" -> "talk",
        "text" -> text,
        "talkId" -> talkId,
        "user" -> Map(
          "id" -> user.stringId,
          "fullName" -> user.fullName
        )))

    case Join(user, talkId) =>
      user match {
        case None =>
          sender ! CannotConnect("Not Authenticated")
        case Some(u: User) =>
          TalkChain.getById(talkId).map {
            case None =>
              sender ! CannotConnect("Talk Not Found")

            case Some(tc) =>
              if(tc.participants.contains(u.stringId)) {
                sender ! Connected(enumerator, u)
              } else {
                sender ! CannotConnect("Forbidden")
              }
          }
      }


    case m =>
      println(m.getClass.getCanonicalName)
      println(m)
  }

  def filter(talkId: String) = Enumeratee.filter[JsValue] {
    json: JsValue => (json \ "talkId").as[String] == talkId
  }
}

case class DoTalk(user: User, talkId: String, text: String)
case class Join(user: Option[User], talkId: String)
case class Quit(user: User, talkId: String)

case class Connected(enumerator:Enumerator[JsValue], user: User)
case class CannotConnect(msg: String)
