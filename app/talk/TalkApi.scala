package talk

import play.api.mvc.{WebSocket, Controller}
import play.api.libs.json.{JsError, JsSuccess, Json, JsValue}
import securesocial.core.SecureSocial
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author alari
 * @since 6/15/13 8:52 PM
 */
object TalkApi extends Controller with securesocial.core.SecureSocial {

  def newTalk = UserAwareAction(parse.json) {implicit request=>
    request.user match {
      case Some(user) =>
        TalkChain.format.reads(request.body) match {
          case JsSuccess(t, _) => Ok(Json.toJson(t))
          case err @ JsError(_) => BadRequest(JsError.toFlatJson(err))
        }
      case _ => Unauthorized
    }
  }

  def list = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) =>
        Async {
          TalkChain.list(user.id.id).map {list => Ok(Json.toJson(list))}
        }
      case _ => Unauthorized
    }
  }

  def socket = WebSocket.async[JsValue] { implicit request =>
    SecureSocial.currentUser match {
      case Some(u) => ChatRoom.join(u.fullName)
      case _ => ChatRoom.join("not authenticated")
    }
  }

}


