package service

import models._
import anorm.NotAssigned
import java.util.Date
import scala.Left


object GraphSimulator {
  /**
   * This starts a new graph session.
   * @param graph The ID of the graph that is to be traversed
   * @param key The auth token public key
   */
  def start(graph: Graph, key: String): GraphSession = {
    // Create the graph state
    val state = GraphLogic.getNextRenderableNode(GraphState(Left(graph.startNode), List()))

    // Start a new graph session
    GraphSession(NotAssigned, graph.id.get, state, new Date().getTime, 0, key)
  }

  /**
   * Given a current session, return the content which is to be displayed
   * @param session The graph session
   * @return The current content
   */
  def getCurrentContent(session: GraphSession): String = {
    // Figure out which node we want
    val nodeId =
      if (session.state.current.isLeft) // We're at a node
        session.state.current.left.get
      else {
        // We're in a dynamic tree
        val dynamicTree = session.state.current.right.get
        dynamicTree.nodes(dynamicTree.index)
      }

    // Get that node and return its contents
    val node = Node.findById(nodeId).get
    NodeContent.findById(node.contentId).get.content
  }

  /**
   * Given a current session, return the settings of the currently displayed node
   * @param session The graph session
   * @return The current content
   */
  def getCurrentSettings(session: GraphSession): String = {
    // Figure out which node we want
    val nodeId =
      if (session.state.current.isLeft) // We're at a node
        session.state.current.left.get
      else {
        // We're in a dynamic tree
        val dynamicTree = session.state.current.right.get
        dynamicTree.nodes(dynamicTree.index)
      }

    // Get that node and return its contents
    val node = Node.findById(nodeId).get
    node.settings
  }

  def progress(session: GraphSession, data: Map[String, String]): GraphSession = {
    // Process the results
    val state = GraphLogic.done(session.state, data)

    // Check to see if we finished
    if (state.isEmpty)
    // We are, so save in the session that we're done
      session.copy(state = GraphState(Left(0), List()), finished = new Date().getTime)
    else {
      // Not done yet, so figure out the displayable node and save the updated session
      val newState = GraphLogic.getNextRenderableNode(state.get)
      session.copy(state = newState)
    }
  }
}
