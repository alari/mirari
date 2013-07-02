package auth

import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.UserId
import play.api._
import reactivemongo.api.DefaultDB
import play.modules.reactivemongo.ReactiveMongoPlugin

import play.api.libs.json._
import play.api.libs.json.Writes._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.joda.time.DateTime
import reactivemongo.api.indexes.{IndexType, Index}

class MongoUserService(application: Application) extends UserServicePlugin(application) {
  implicit def app = application

  var db: DefaultDB = _
  val Timeout = Duration.create(1, "second")

  def usersCollection: JSONCollection = db.collection[JSONCollection]("users")
  def tokensCollection: JSONCollection = db.collection[JSONCollection]("user.tokens")

  override def onStart() {
    db = ReactiveMongoPlugin.db

    usersCollection.indexesManager.ensure(Index(Seq("userId"->IndexType.Descending, "providerId"->IndexType.Descending), unique = true, dropDups = true))
    tokensCollection.indexesManager.ensure(Index(Seq("uuid"->IndexType.Descending), unique = true, dropDups = true))
    tokensCollection.indexesManager.ensure(Index(Seq("expirationDate"->IndexType.Descending)))

    super.onStart()
  }

  def find(id: UserId): Option[UserIdentity] = {
    val result = Await.result(usersCollection.find(Json.obj("userId" -> id.id, "providerId" -> id.providerId)).one[UserIdentity], Timeout)
    if (result.isEmpty) {
      return Option.empty
    }
    val user = result.get

    Option.apply(user)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[UserIdentity] = {
    val result = Await.result(usersCollection.find(Json.obj("email" -> email, "providerId" -> providerId)).one[UserIdentity], Timeout)

    if (result.isEmpty) {
      return Option.empty
    }
    val user = result.get

    Option.apply(user)
  }

  def save(user: Identity): UserIdentity = {
    val jsonUser: UserIdentity = user

    Await.ready(usersCollection.insert(jsonUser).map(lastError => Logger.error(lastError.stringify)), Timeout)

    find(user.id).get
  }



  implicit val tokenFormat = Json.format[Token]

  def save(token: Token) {
    tokensCollection.insert(token)
  }

  def findToken(token: String): Option[Token] = {
    val result = Await.result(tokensCollection.find(Json.obj("uuid"->token)).one[Token], Timeout)
    if(result.isEmpty) {
      return Option.empty[Token]
    }
    Option.apply(result.get)
  }

  def deleteToken(uuid: String) {
    tokensCollection.remove(Json.obj("uuid"->uuid))
  }

  def deleteTokens() {
    tokensCollection.drop()
  }

  def deleteExpiredTokens() {
    tokensCollection.remove(Json.obj("expirationTime" -> Json.obj("$lte" -> DateTime.now())))
  }
}