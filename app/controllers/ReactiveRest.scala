package controllers

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import reactivemongo.bson.BSONObjectID

trait ReactiveRest extends Controller {
  val db = ReactiveMongoPlugin.db

  // Object ID formatters
  val objectIdFormat = OFormat[String](
    (__ \ "$oid").read[String],
    OWrites[String] {
      s => Json.obj("$oid" -> s)
    }
  )

  val toObjectId = OWrites[String] {
    s => Json.obj("_id" -> Json.obj("$oid" -> s))
  }
  val fromObjectId = (__ \ '_id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString])

  /** Generates a new ID and adds it to your JSON using Json extended notation for BSON */
  val generateId = (__ \ '_id \ '$oid).json.put( JsString(BSONObjectID.generate.stringify) )

  /** Updates Json by adding both ID and date */
  val addMongoId: Reads[JsObject] = __.json.update( generateId )

  // Collection to query
  def collectionName: String

  // Item reader/validator
  def itemReads: Reads[JsObject] = (
    (__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString]) and
      (__ \ '_id).json.prune
    ).reduce


  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  def toMongoUpdate: Reads[JsObject] = (__ \ '$set).json.copyFrom(__.json.pickBranch)

  // queries and renders a list
  def queryItems(q: JsObject) = collection.find(q).cursor[JsObject].toList().map(list => Ok(Json.toJson(list.map(i => i.transform(itemReads).asOpt))))

  // default finder
  def defaultQueryFinder: JsObject = Json.obj()

  // vadidator/reader for query
  def queryFinderReads: Reads[JsObject] = __.json.pickBranch

  /**
   * REST GET action
   *
   * @param id
   * @return
   */
  def get(id: String) = Action {
    Async {
      collection.find(toObjectId.writes(id)).one[JsValue].map {
        case None => NotFound(Json.obj("res" -> "KO", "error" -> s"$collectionName :: $id not found"))
        case Some(p) =>
          p.transform(itemReads).map {
            jsonp =>
              Ok(jsonp)
          }.recoverTotal {
            e =>
              BadRequest(JsError.toFlatJson(e))
          }
      }
    }
  }

  /**
   * REST POST action for a concrete id
   *
   * @param id
   * @return
   */
  def save(id: String) = Action(parse.json) {
    request =>
      Async {
        collection.update(toObjectId.writes(id), request.body.transform(toMongoUpdate).get).map(err => Ok(err.stringify))
      }
  }

  /**
   * REST POST action without id
   *
   * @return
   */
  def create() = Action(parse.json) {request =>
    println(request.body)
    request.body.transform(addMongoId).map {jsobj =>
      Async {
        println(jsobj)
        collection.insert(jsobj).map { r =>
          jsobj.transform(itemReads).map {
            j => Ok(j)
          } .recoverTotal {
            e => BadRequest(JsError.toFlatJson(e))
          }

        }
      }
    }.recoverTotal {err =>
      BadRequest(JsError.toFlatJson(err))
    }

  }

  /**
   * REST GET action without id
   *
   * @param q
   * @return
   */
  def query(q: Option[String]) = Action { implicit request =>
      if (q.isEmpty) {
        Async {
          queryItems(defaultQueryFinder)
        }
      } else {
        Json.parse(q.get).transform(queryFinderReads).map({
          jsobj =>
            Async {
              queryItems(jsobj)
            }
        }).recoverTotal({
          case e =>
            BadRequest(Json.obj("Cannot parse request" -> e.toString))
        })
      }
  }

  /**
   * REST DELETE action for id
   *
   * @param id
   * @return
   */
  def delete(id: String) = Action {
    Async {
      collection.remove[JsValue](toObjectId.writes(id)).map {
        lastError =>
          if (lastError.ok)
            Ok(Json.obj("msg" -> s"$collectionName :: $id deleted"))
          else
            InternalServerError(Json.obj("error" -> "error %s".format(lastError.stringify)))
      }
    }
  }
}
