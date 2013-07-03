package auth

import securesocial.core._
import securesocial.core.OAuth2Info
import securesocial.core.PasswordInfo
import securesocial.core.OAuth1Info
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Json
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONObjectID

/**
 * @author alari
 * @since 7/3/13 10:08 PM
 */
case class User(id: String,  userId: String,
                providerId: String,

                firstName: String,
                lastName: String,
                fullName: String,

                email: Option[String],
                avatarUrl: Option[String],

                authMethod: AuthenticationMethod,
                oAuth1Info: Option[OAuth1Info],
                oAuth2Info: Option[OAuth2Info],

                passwordInfo: Option[PasswordInfo])

object User {
  val db = ReactiveMongoPlugin.db
  val collectionName = "user"
  val Timeout = Duration.create(1, "second")

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  collection.indexesManager.ensure(Index(Seq("userId"->IndexType.Descending, "providerId"->IndexType.Descending), unique = true, dropDups = true))

  /**
   * Find user by securesocial userid -- combined from id and provider
   *
   * @param id
   * @return
   */
  def findByUserId(id: UserId): Future[Option[User]] = collection.find(Json.obj("userId" -> id.id, "providerId" -> id.providerId)).one[User]

  /**
   * Find user by email and securesocial provider id
   * @param email
   * @param providerId
   * @return
   */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[User]] = collection.find(Json.obj("email" -> email, "providerId" -> providerId)).one[User]

  /**
   * Insert user; set bson id if required
   * @param user
   * @return
   */
  def insert(user: User): Future[LastError] = {
    val u = if(user.id == "" || user.id.isEmpty) {
      user.copy(id = BSONObjectID.generate.stringify)
    } else user
    collection insert u
  }

  /**
   * Finds by user id or creates a new user instance
   * @param user
   * @return
   */
  implicit def identity2user(user: Identity): User = {
    Await.result(findByUserId(user.id), Timeout).getOrElse(
      User("", user.id.id, user.id.providerId, user.firstName, user.lastName, user.fullName,
        user.email, user.avatarUrl,
        user.authMethod,
        user.oAuth1Info, user.oAuth2Info, user.passwordInfo)
    )
  }
}
