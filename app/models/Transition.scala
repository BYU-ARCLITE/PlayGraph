package models

import play.api.libs.json.{JsValue, Json}

case class Transition(targetId: Long, rule: String) {
  def toJson = Json.obj("targetId" -> targetId, "rule" -> rule)
}

object Transition {
  def fromJson(json: JsValue) = Transition((json \ "targetId").as[Long], (json \ "rule").as[String])
}
