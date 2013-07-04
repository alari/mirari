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

  val Timeout = Duration.create(1, "second")

  def find(id: UserId): Option[User] = {
    Await.result(User.findByUserId(id), Timeout)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[User] = {
    Await.result(User.findByEmailAndProvider(email, providerId), Timeout)
  }

  def save(user: Identity): User = {
    Await.ready(User.insert(user).map(lastError => Logger.error(lastError.stringify)), Timeout)
    find(user.id).get
  }



  var db: DefaultDB = _

  def tokensCollection: JSONCollection = db.collection[JSONCollection]("user.tokens")

  override def onStart() {
    db = ReactiveMongoPlugin.db

    tokensCollection.indexesManager.ensure(Index(Seq("uuid"->IndexType.Descending), unique = true, dropDups = true))
    tokensCollection.indexesManager.ensure(Index(Seq("expirationDate"->IndexType.Descending)))

    super.onStart()
  }

  implicit val tokenFormat = Json.format[Token]

  def save(token: Token) {
    tokensCollection.insert(token)
  }

  def findToken(token: String): Option[Token] = {
    Await.result(tokensCollection.find(Json.obj("uuid"->token)).one[Token], Timeout)
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