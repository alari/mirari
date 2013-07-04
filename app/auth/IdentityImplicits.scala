package auth

import securesocial.core.{AuthenticationMethod, OAuth1Info, OAuth2Info, PasswordInfo}
import play.api.libs.json.Json

/**
 * @author alari
 * @since 7/4/13 11:08 PM
 */
trait IdentityImplicits {
  implicit val authenticationMethodFormat = Json.format[AuthenticationMethod]
  implicit val oAuth1Format = Json.format[OAuth1Info]
  implicit val oAuth2Format = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]
}
