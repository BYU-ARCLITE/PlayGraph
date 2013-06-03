package controllers.api

import play.api.mvc.{Action, Controller}
import models.{Transition, Node}
import anorm.NotAssigned
import play.api.libs.json.{JsArray, JsBoolean, JsObject, Json}

object Nodes extends Controller {
  def create = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Create the node
        val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
        val contentId = params("contentId").toLong
        val contentType = Symbol(params("contentType"))
        val transitions = Json.parse(params("transitions")).as[JsArray].value.toList.map(Transition.fromJson(_))
        val node = Node(NotAssigned, contentId, contentType, transitions).save
        Ok(Json.obj("success" -> true, "node" -> node.id.get)).withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def get(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node exists
        val node = Node.findById(id)
        if (node.isDefined) {
          Ok(node.get.toJson).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Node doesn't exist
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def update(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node exists
        val node = Node.findById(id)
        if (node.isDefined) {

          // Update the node
          val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
          val contentId = params.get("contentId").map(_.toLong).getOrElse(node.get.contentId)
          val contentType = params.get("contentType").map(Symbol(_)).getOrElse(node.get.contentType)
          val transitions = params.get("transitions")
            .map(Json.parse(_).as[JsArray].value.toList.map(Transition.fromJson(_)))
            .getOrElse(node.get.transitions)
          val updatedNode = node.get.copy(contentId = contentId, contentType = contentType, transitions = transitions)
            .save
          Ok(Json.obj("success" -> true, "node" -> updatedNode.toJson))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Node not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def delete(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node exists
        val node = Node.findById(id)
        if (node.isDefined) {

          // Delete the node
          node.get.delete()
          Ok(Json.obj("success" -> true)).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Node not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }
}
