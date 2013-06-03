package controllers.api

import play.api.mvc.Controller
import models.NodePool
import anorm.NotAssigned
import play.api.libs.json.Json

object NodePools extends Controller {

  def create = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Create the node pool
        val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
        val nodes = params("nodes").split(",").map(_.toLong).toSet
        val nodePool = NodePool(NotAssigned, nodes, params("script")).save
        Ok(Json.obj("success" -> true, "nodePool" -> nodePool.id.get)).withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def get(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node pool exists
        val nodePool = NodePool.findById(id)
        if (nodePool.isDefined) {
          Ok(nodePool.get.toJson).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodePool doesn't exist
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def update(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node pool exists
        val nodePool = NodePool.findById(id)
        if (nodePool.isDefined) {

          // Update the node pool
          val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
          val nodes = params.get("nodes").map(_.split(",").map(_.toLong).toSet).getOrElse(nodePool.get.nodes)
          val script = params.get("script").getOrElse(nodePool.get.script)
          val updatedNodePool = nodePool.get.copy(nodes = nodes, script = script).save
          Ok(Json.obj("success" -> true, "nodePool" -> updatedNodePool.toJson))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodePool not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def delete(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the node pool exists
        val nodePool = NodePool.findById(id)
        if (nodePool.isDefined) {

          // Delete the nodePool
          nodePool.get.delete()
          Ok(Json.obj("success" -> true)).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // NodePool not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }
}
