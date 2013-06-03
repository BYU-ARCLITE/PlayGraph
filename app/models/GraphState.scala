package models

import play.api.libs.json._
import scala.{Right, Left, Either}
import play.api.libs.json.JsArray
import play.api.libs.json.JsUndefined

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 9:47 AM
 * To change this template use File | Settings | File Templates.
 */
case class GraphState(current: Either[Long, DynamicTree], context: List[Either[Long, DynamicTree]]) {

  def toJson = Json.obj(
    "current" -> GraphState.eitherToJson(current),
    "context" -> context.map(GraphState.eitherToJson(_))
  )
}

object GraphState {

  private def eitherToJson(either: Either[Long, DynamicTree]) =
    if (either.isLeft)
      Json.obj("left" -> either.left.get)
    else
      Json.obj("right" -> either.right.get.toJson)

  private def jsonToEither(json: JsValue) =
    if (!(json \ "left").isInstanceOf[JsUndefined]) {
      Left((json \ "left").as[Long])
    } else
      Right(DynamicTree.fromJson(json \ "right"))

  def fromJson(json: JsValue) = GraphState(
    jsonToEither(json \ "current"),
    (json \ "context").as[JsArray].value.toList.map(jsonToEither(_))
  )

}

