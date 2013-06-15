package model

import securesocial.core._
import play.api.libs.json._
import securesocial.core.PasswordInfo
import securesocial.core.UserId
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info

case class UserIdentity(
                         userId: String,
                         providerId: String,

                         firstName: String,
                         lastName: String,
                         fullName: String,

                         email: Option[String],
                         avatarUrl: Option[String],

                         authMethod: AuthenticationMethod,
                         oAuth1Info: Option[OAuth1Info],
                         oAuth2Info: Option[OAuth2Info],

                         passwordInfo: Option[PasswordInfo]

                         ) extends Identity {
  def id: UserId = UserId(userId, providerId)
}

object UserIdentity {
  implicit val authenticationMethodFormat = Json.format[AuthenticationMethod]
  implicit val oAuth1Format = Json.format[OAuth1Info]
  implicit val oAuth2Format = Json.format[OAuth2Info]
  implicit val passwordInfoFormat = Json.format[PasswordInfo]

  implicit val userIdentityFormat = Json.format[UserIdentity]

  implicit def identity2user(user: Identity): UserIdentity = UserIdentity(
    user.id.id, user.id.providerId, user.firstName, user.lastName, user.fullName,
    user.email, user.avatarUrl,
    user.authMethod,
    user.oAuth1Info, user.oAuth2Info, user.passwordInfo
  )
}