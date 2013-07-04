package talk

import java.util.Date
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import auth.User
import reactivemongo.bson.BSONObjectID
import util.MongoImplicits
import scala.concurrent.Future

case class Message(_id: Option[BSONObjectID], userId: String, talkId: String, unread: Seq[String], text: String)

object Message extends MongoImplicits {
  implicit val format = Json.format[Message]

  val db = ReactiveMongoPlugin.db
  val collectionName = "talk.message"

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)
}

/**
 * @author alari
 * @since 6/17/13 12:52 AM
 */
case class TalkChain(_id: Option[BSONObjectID], participants: Seq[String], topic: String, last: Date)

object TalkChain extends MongoImplicits{
  implicit val format = Json.format[TalkChain]

  val db = ReactiveMongoPlugin.db
  val collectionName = "talk.chain"

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  def getById(id: String) = collection.find(toObjectId.writes(id)).one[TalkChain]

  def list(userId:String) = {
    collection.find(Json.obj("participants" -> userId)).sort(Json.obj("last" -> -1)).cursor[TalkChain].toList()
  }

  def create(user: User, talk: NewTalk): Future[Option[TalkChain]] = {
    val chain = TalkChain(Some(BSONObjectID.generate), Seq(user._id.get.stringify, talk.to), talk.topic, new Date())

    collection.insert(chain).map {lastError =>
      if(lastError.inError) None
      else {
        Message.collection.insert(Message(None, user._id.get.stringify, chain._id.get.stringify, chain.participants, talk.text))
        Some(chain)
      }
    }
  }

  def messages(user: User, talkId: String): Future[Seq[Message]] = {
    getById(talkId).flatMap{
      case None => throw new NoSuchElementException(s"talk not found $talkId")

      case Some(talkChain)=>
        if(talkChain.participants.contains(user._id.get.stringify)) {
          Message.collection.find(Json.obj("talkId" -> talkId)).cursor[Message].toList()
        } else {
          throw new NoSuchElementException("not in participants")
        }
    }
  }
}



case class NewTalk(to:String, topic: String, text: String)
object NewTalk {
  implicit val format = Json.format[NewTalk]
}