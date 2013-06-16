package auth

import play.api.mvc._
import play.api.libs.json.{JsObject, Json}
import securesocial.core._
import scala.Some
import play.api.i18n.Messages
import play.Logger

object AuthApi extends Controller with securesocial.core.SecureSocial {

  def jsonStatus(user: Option[Identity]) : JsObject = user match {
    case Some(u) => Json.obj("isAuthenticated" -> true, "username" -> u.fullName)
    case _ => Json.obj("isAuthenticated" -> false)
  }

  def status = UserAwareAction { implicit request =>
    Ok(jsonStatus(request.user))
  }

  def signIn = Action { implicit request =>
    Registry.providers.get("userpass") match {
      case Some(p) =>{
        try {
          p.authenticate().fold( result => {
            result.asInstanceOf[PlainResult].header.status match {
              case 400 => BadRequest(Json.obj("error" -> "Not authenticated"))
              case _ => result
            }
          } , {
            user => println("about to complete auth"); completeAuthentication(user, session)
          })
        } catch {
          case ex: AccessDeniedException => {
            Unauthorized(Messages("securesocial.login.accessDenied"))
          }

          case other: Throwable => {
            Logger.error("Unable to log user in. An exception was thrown", other)
            Forbidden(Messages("securesocial.login.errorLoggingIn"))
          }
        }
      }
      case _ => {
        NotFound
      }
    }
  }

  def completeAuthentication(user: Identity, session: Session)(implicit request: RequestHeader): PlainResult = {
    if ( Logger.isDebugEnabled ) {
      Logger.debug("[securesocial] user logged in : [" + user + "]")
    }
    val withSession = Events.fire(new LoginEvent(user)).getOrElse(session)
    Authenticator.create(user) match {
      case Right(authenticator) => {
        Ok(jsonStatus(Option.apply(user))).withSession(withSession -
          SecureSocial.OriginalUrlKey -
          IdentityProvider.SessionId -
          OAuth1Provider.CacheKey).withCookies(authenticator.toCookie)
      }
      case Left(error) => {
        // improve this
        throw new RuntimeException("Error creating authenticator")
      }
    }
  }

  def signOut = Action { implicit request =>
    val user = for (
      authenticator <- SecureSocial.authenticatorFromRequest ;
      user <- UserService.find(authenticator.userId)
    ) yield {
      Authenticator.delete(authenticator.id)
      user
    }
    val result = Ok(Json.obj("isAuthenticated" -> false)).discardingCookies(Authenticator.discardingCookie)
    user match {
      case Some(u) => result.withSession( Events.fire(new LogoutEvent(u)).getOrElse(session) )
      case None => result
    }
  }
}