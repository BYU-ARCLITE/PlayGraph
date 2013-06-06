package controllers.api

import play.api.mvc.Controller
import models.Graph
import anorm.NotAssigned
import play.api.libs.json.Json

object Graphs extends Controller {

  def create = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Create the graph
        val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
        val graph = Graph(NotAssigned, params("startNode").toLong).save
        Ok(Json.obj("success" -> true, "graph" -> graph.toJson)).withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def get(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the graph exists
        val graph = Graph.findById(id)
        if (graph.isDefined) {
          Ok(graph.get.toJson).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Graph doesn't exist
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def update(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the graph exists
        val graph = Graph.findById(id)
        if (graph.isDefined) {

          // Update the graph
          val params = request.body.asFormUrlEncoded.get.mapValues(_(0))
          val updatedGraph = graph.get.copy(startNode = params("startNode").toLong).save
          Ok(Json.obj("success" -> true, "graph" -> updatedGraph.toJson))
            .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Graph not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def delete(id: Long) = Authentication.authenticatedAction('author) {
    request =>
      authToken =>

      // Check that the graph exists
        val graph = Graph.findById(id)
        if (graph.isDefined) {

          // Delete the graph
          graph.get.delete()
          Ok(Json.obj("success" -> true)).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else // Graph not found
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }
}
