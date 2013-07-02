package talk

import java.util.Date
import play.api.libs.json.Json
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author alari
 * @since 6/17/13 12:52 AM
 */
case class TalkChain(id: String, participants: Seq[String], title: String, first: Date, last: Date)

object TalkChain {
  implicit val format = Json.format[TalkChain]

  val db = ReactiveMongoPlugin.db
  val collectionName = "talk.chain"

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  def list(userId:String) = {
    collection.find(Json.obj("userId" -> userId)).sort(Json.obj("last" -> -1)).cursor[TalkChain].toList()
  }
}

case class Message(id: String, userId: String, talkId: String, unread: Seq[String], date: Date, text: String)

object Message {
  implicit val format = Json.format[Message]

  val db = ReactiveMongoPlugin.db
  val collectionName = "talk.message"

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)
}