package service

import models._
import scala.Left
import models.DynamicTree
import scala.Right
import org.mozilla.javascript.{NativeArray, Context}

object GraphLogic {

  /**
   * This runs the control script on a node with the given variables and returns the result
   * @param node The node that just was finished
   * @param variables The variables to be added to the script
   * @return The result as a string
   */
  def processRules(node: Node, variables: Map[String, String]): Long = {
    def isNumber(str: String): Boolean = str.matches("^\\d+$") || str.matches("^\\d+\\.\\d+$") || str.matches("^\\.\\d+$")

    // Create a script segment which contains the variables
    val variablePrefix = variables.map(data => {
      val name = data._1
      val value = if (isNumber(data._2)) data._2 else  "\""+data._2+"\""
      s"var $name = $value;\n"
    }).mkString("")

    // Run each transition rule script until one return true
    val cx = Context.enter()
    val scope = cx.initStandardObjects()
    node.transitions.find(transition =>
      cx.evaluateString(scope, variablePrefix + transition.rule, "Transition rule script", 1, null).asInstanceOf[Boolean]
    ).map(_.targetId).getOrElse(0)
  }

  /**
   *
   * @param nodePool The node pool whose selection mechanism will be called
   * @return
   */
  def executeSelectionMechanism(nodePool: NodePool): DynamicTree = {
    // Create the selection script
    var script = "var nodes = [" + nodePool.nodes.mkString(",")+"];\n"
    script += nodePool.script

    // Run the script and return a dynamic tree
    val cx = Context.enter()
    val scope = cx.initStandardObjects()
    val nodes = cx.evaluateString(scope, script, "Node pool selection mechanism", 1, null)
      .asInstanceOf[NativeArray].toArray.toList.map(n => n.asInstanceOf[Number].longValue())
    DynamicTree(
      nodes,
      0
    )
  }

  /**
   * This figures out what node is next in the graph that can be rendered. If the node is a graph or a node pool it
   * doesn't actually contain content to be displayed. This also updates the context depending on what changes are made.
   * @param state The current state (node and context stacks)
   * @return The new state which includes the new node to be rendered
   */
  def getNextRenderableNode(state: GraphState): GraphState = {
    val node = (
      if (state.current.isLeft)
        Node.findById(state.current.left.get).get
      else {
        val tree = state.current.right.get
        Node.findById(tree.nodes(tree.index)).get
      }
    )

    if (node.contentType == 'data)
      // The new node is a standard HTML page so all is good
      state
    else if (node.contentType == 'graph) {
      // The new node contains a graph so recurse into it to obtain a renderable node
      val graph = Graph.findById(node.contentId).get

      // Push the node/dynamic tree onto the context stack add add the new graph

      val newState = GraphState(
        Left(graph.startNode),
        (if (state.current.isLeft)
          Left(node.id.get)
        else
          Right(state.current.right.get)) :: state.context
      )

      getNextRenderableNode(newState)
    } else {
      // The new node contains a node pool so generate the dynamic tree
      val nodePool = NodePool.findById(node.contentId).get
      val dynamicTree = executeSelectionMechanism(nodePool)

      // Push the node onto the context stack and add the dynamic tree
      val newState = GraphState(
        Right(dynamicTree),
        Left(node.id.get) :: state.context
      )
      getNextRenderableNode(newState)
    }
  }

  def done(state: GraphState, variables: Map[String, String]): Option[GraphState] = {
    if (state.current.isLeft) {
      // In a graph, so run the node's control script to determine where to go
      val result = processRules(Node.findById(state.current.left.get).get, variables)
      processResult(result, state, variables)
    } else {
      // In a dynamic tree, so instead of running the control script just move to the next
      val tree = state.current.right.get
      val index = tree.index + 1
      if (index >= tree.nodes.size) {
        // Done with the dynamic tree, so remove it and repeat
        val newState = GraphState(state.context(0), state.context.slice(1, state.context.size))
        done(newState, variables)
      } else
        // Move along
        Some(GraphState(Right(DynamicTree(tree.nodes, index)), state.context))
    }
  }

  /**
   * This takes the result of a Node's control script and figures out which node we need to go to next. At the simplest
   * level this returns the node with the id designated by the result. However, this also checks to see if we hit the
   * end of a graph and figures out where to go based on the context.
   * @param result The number returned by processRules
   * @param state The current graph state
   * @param variables A map of variables to be added to the scope
   * @return The next node to be handled and the new context
   */
  def processResult(result: Long, state: GraphState, variables: Map[String, String]): Option[GraphState] = {
    // This is only called when in a graph so we can assume that state.current is always left
    if (result == 0) {
      // We hit the end of a graph
      if (state.context.isEmpty)
        // We're totally done
        None
      else {
        // We're done with a sub-graph. Move up
        val newState = GraphState(state.context(0), state.context.slice(1, state.context.size))
        done(newState, variables)
      }
    } else
      // Nothing special to do. Just return the new node that the result designates
      Some(GraphState(Left(result), state.context))
  }
}