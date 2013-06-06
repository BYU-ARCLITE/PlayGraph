package controllers.api

import play.api.mvc.Controller
import models.NodeContent
import anorm.NotAssigned
import play.api.libs.json.Json

object NodeContents extends Controller {

  def create = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Create the node content
        val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
        val nodeContent = NodeContent(NotAssigned, params("content")).save
        Ok(Json.obj("success" -> true, "nodeContent" -> nodeContent.toJson)).withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def get(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node content exists
        val nodeContent = NodeContent.findById(id)
        if (nodeContent.isDefined) {
          Ok(nodeContent.get.toJson).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodeContent doesn't exist
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def update(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node content exists
        val nodeContent = NodeContent.findById(id)
        if (nodeContent.isDefined) {

          // Update the node content
          val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
          val updatedNodeContent = nodeContent.get.copy(content = params("content")).save
          Ok(Json.obj("success" -> true, "nodeContent" -> updatedNodeContent.toJson))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodeContent not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def delete(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node content exists
        val nodeContent = NodeContent.findById(id)
        if (nodeContent.isDefined) {

          // Delete the nodeContent
          nodeContent.get.delete()
          Ok(Json.obj("success" -> true)).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodeContent not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }
}
