package util

import play.modules.reactivemongo.{MongoController, ReactiveMongoPlugin}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError
import scala.concurrent.Future

trait ReactiveRest extends MongoController with MongoImplicits {
  self: Controller =>

  /** Callback to be executed before delete, with id as parameter */
  def beforeDelete(id: String) {}

  // Collection to query
  def collectionName: String

  // Item reader/validator
  def itemInListReads = (
    (__ \ 'id).json.copyFrom((__ \ '_id \ '$oid).json.pick[JsString]) and
      (__ \ '_id).json.prune
    ).reduce

  def itemReads = itemInListReads

  def itemValidate: Reads[JsObject] = __.json.pickBranch

  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  def toMongoUpdate: Reads[JsObject] = (__ \ '$set).json.copyFrom(__.json.pickBranch)

  // queries and renders a list
  def queryItems(q: JsObject) = collection.find(q).cursor[JsObject].toList().map(list => Ok(Json.toJson(list.map(i => i.transform(itemInListReads).asOpt))))

  // default finder
  def defaultQueryFinder: JsObject = Json.obj()

  // vadidator/reader for query
  def queryFinderReads: Reads[JsObject] = __.json.pickBranch

  protected def processGet(id: String)(implicit successGetProcessor: (JsValue)=>JsValue = {o=>o}) : AsyncResult = {
    Async {
      collection.find(toObjectId.writes(id)).one[JsValue].map {
        case None => NotFound(Json.obj("res" -> "KO", "error" -> s"$collectionName :: $id not found"))
        case Some(p) =>
          p.transform(itemReads).map {
            jsonp =>
              Ok(successGetProcessor(jsonp))
          }.recoverTotal {
            e =>
              BadRequest(JsError.toFlatJson(e))
          }
      }
    }
  }

  private def processChange(jsVal: JsValue, preTransform: Reads[JsObject], action: (JsObject)=>Future[LastError], idGetter: (JsObject)=>String, successGetProcessor: (JsValue)=>JsValue = {o=>o}) : Result = {
    jsVal.transform(preTransform).map {jsObj =>
      Async {
        action(jsObj).map {
          err=>
            if (err.inError) {
              BadRequest(err.stringify)
            } else {
              val id = idGetter(jsObj)
              processGet(id)(successGetProcessor)
            }
        }
      }
    }.recoverTotal {
      e=>BadRequest(JsError.toFlatJson(e))
    }
  }

  protected def processSave(id: String, jsVal: JsValue)(implicit successGetProcessor: (JsValue)=>JsValue = {o=>o}) : Result = {
    processChange(jsVal, itemValidate andThen toMongoUpdate, {o=>collection.update(toObjectId.writes(id), o)}, {_ => id}, successGetProcessor)
  }

  protected def processCreate(jsVal: JsValue)(implicit successGetProcessor: (JsValue)=>JsValue = {o=>o}) : Result = {
    processChange(jsVal, itemValidate andThen addMongoId, {o=>collection.insert(o)}, {o=> o.transform(getObjectId).get.value}, successGetProcessor)
  }

  /**
   * REST GET action
   *
   * @param id
   * @return
   */
  def get(id: String) = Action {
    implicit request =>
      processGet(id)
  }

  /**
   * REST POST action for a concrete id
   *
   * @param id
   * @return
   */
  def save(id: String) = Action(parse.json) {
    implicit request =>
      processSave(id, request.body)
  }

  /**
   * REST POST action without id
   *
   * @return
   */
  def create() = Action(parse.json) {
    implicit request =>
      processCreate(request.body)
  }

  /**
   * REST GET action without id
   *
   * @param q
   * @return
   */
  def query(q: Option[String]) = Action {
    implicit request =>
      if (q.isEmpty) {
        Async {
          queryItems(defaultQueryFinder)
        }
      } else {
        Json.parse(q.get).transform(queryFinderReads).map({
          jsObj =>
            Async {
              queryItems(jsObj)
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
    implicit request =>
      Async {
        beforeDelete(id)
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
