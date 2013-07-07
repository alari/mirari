package talk

import play.api.mvc.{WebSocket, Controller}
import play.api.libs.json.{JsError, JsSuccess, Json, JsValue}
import securesocial.core.SecureSocial
import scala.concurrent.ExecutionContext.Implicits.global
import auth.User

/**
 * @author alari
 * @since 6/15/13 8:52 PM
 */
object TalkApi extends Controller with securesocial.core.SecureSocial {

  def newTalk = UserAwareAction(parse.json) {implicit request=>
    request.user match {
      case Some(user: User) =>
        NewTalk.format.reads(request.body) match {
          case JsSuccess(t, _) =>

          Async {
            TalkChain.create(user, t).map {
              case Some(tc) =>Ok(Json.toJson(tc))
              case _ => BadRequest("Failed!")
            }

          }

          case err @ JsError(_) => BadRequest(JsError.toFlatJson(err))
        }
      case Some(u) =>
        println(u)
        Forbidden
      case _ => Unauthorized
    }
  }

  def messages(talkId: String) = UserAwareAction { implicit request =>
    request.user match {
      case Some(user: User) =>
        Async {
          TalkChain.messages(user, talkId).map {
            case tc: Seq[Message] =>Ok(Json.toJson(tc))
            case _ => BadRequest("Failed!")
          }
        }
      case _ =>
        Forbidden
    }
  }

  def list = UserAwareAction { implicit request =>
    request.user match {
      case Some(user: User) =>
        Async {
          TalkChain.list(user).map {list => Ok(Json.toJson(list))}
        }
      case _ => Unauthorized
    }
  }

  def socket(talkId:String) = WebSocket.async[JsValue] { implicit request =>
    SecureSocial.currentUser match {
      case Some(u: User) => ChatRoom.join(u.fullName)
      case _ => ChatRoom.join("not authenticated")
    }
  }

}

