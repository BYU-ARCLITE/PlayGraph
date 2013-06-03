package models

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 1/10/13
 * Time: 9:30 AM
 * To change this template use File | Settings | File Templates.
 */

case class DynamicTree(nodes: List[Long], index: Int) {
  def toJson = Json.obj("nodes" -> nodes, "index" -> index)
}

object DynamicTree {
  def fromJson(json: JsValue) =
    DynamicTree((json \ "nodes").as[JsArray].value.toList.map(_.as[Long]), (json \ "index").as[Int])
}