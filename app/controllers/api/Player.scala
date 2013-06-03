package controllers.api

import play.api.mvc.Controller
import models.{GraphSession, Graph}
import service.GraphSimulator
import play.api.libs.json.Json

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
object Player extends Controller {

  def start = Authentication.authenticatedAction('player) {
    request =>
      authToken =>

        // Check that the request graph exists
        val graph = request.body.asFormUrlEncoded.get.get("graph").flatMap(id => Graph.findById(id(0).toLong))
        if (graph.isDefined) {

          // Start the simulation
          val session = GraphSimulator.start(graph.get, authToken.publicKey).save
          Ok(Json.obj("sessionId" -> session.id.get)).withHeaders("Access-Control-Allow-Origin" -> "*")
        } else
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def update = Authentication.authenticatedAction('player) {
    request =>
      authToken =>

        // Check that the session with matching key exists
        val session = request.body.asFormUrlEncoded.get.get("sessionId").flatMap(id => GraphSession.findById(id(0).toLong))
        if (session.isDefined && session.get.publicKey == authToken.publicKey) {

          // Check that the session is still "in progress"
          if (session.get.finished == 0) {

            // Update the simulation
            val data = request.body.asFormUrlEncoded.get.mapValues(_(0))
            val updatedSession = GraphSimulator.progress(session.get, data).save

            // Send the result
            val status = if (updatedSession.finished > 0) "done" else "continue"
            Ok(Json.obj("status" -> status)).withHeaders("Access-Control-Allow-Origin" -> "*")
          } else
            Forbidden(Json.obj("message" -> "This session is already completed"))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def render(sessionId: Long) = Authentication.authenticatedAction('player) {
    request =>
      authToken =>

        // Check that the session with matching key exists
        val session = GraphSession.findById(sessionId)
        if (session.isDefined && session.get.publicKey == authToken.publicKey) {

          // Check that the session is still "in progress"
          if (session.get.finished == 0) {
            Ok(GraphSimulator.getCurrentContent(session.get)).withHeaders("Access-Control-Allow-Origin" -> "*")
          } else
            Forbidden(Json.obj("message" -> "This session is already completed"))
              .withHeaders("Access-Control-Allow-Origin" -> "*")
        } else
          NotFound.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

}
