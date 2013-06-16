package talk

import play.api.mvc.{WebSocket, Controller}
import play.api.libs.json.JsValue
import securesocial.core.SecureSocial

/**
 * @author alari
 * @since 6/15/13 8:52 PM
 */
object TalkApi extends Controller with securesocial.core.SecureSocial {

  def socket = WebSocket.async[JsValue] { implicit request =>

    SecureSocial.currentUser match {
      case Some(u) => ChatRoom.join(u.fullName)
      case _ => ChatRoom.join("not authenticated")

    }

  }

}


