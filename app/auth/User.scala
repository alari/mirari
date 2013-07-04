package auth

import _root_.util.MongoImplicits
import securesocial.core._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration.Duration
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.LastError
import reactivemongo.bson.BSONObjectID
import securesocial.core.UserId
import securesocial.core.OAuth2Info
import reactivemongo.api.indexes.Index
import play.modules.reactivemongo.json.collection.JSONCollection
import securesocial.core.OAuth1Info
import securesocial.core.PasswordInfo
import scala.Some
import play.api.Play.current
import ExecutionContext.Implicits.global

/**
 * @author alari
 * @since 7/3/13 10:08 PM
 */
case class User(_id: Option[BSONObjectID],
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

                passwordInfo: Option[PasswordInfo]) extends Identity {
  def id: UserId = UserId(userId, providerId)
}

object User extends MongoImplicits with IdentityImplicits{
  val db = ReactiveMongoPlugin.db
  val collectionName = "users"
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
    val u = if(user._id.isEmpty) {
      user.copy(_id = Some(BSONObjectID.generate))
    } else user
    collection insert u
  }

  implicit val formats = Json.format[User]

  /**
   * Finds by user id or creates a new user instance
   * @param user
   * @return
   */
  implicit def identity2user(user: Identity): User =
        Await.result(findByUserId(user.id), Timeout).getOrElse(
          User(Option.empty, user.id.id, user.id.providerId, user.firstName, user.lastName, user.fullName,
            user.email, user.avatarUrl,
            user.authMethod,
            user.oAuth1Info, user.oAuth2Info, user.passwordInfo)
        )
}
