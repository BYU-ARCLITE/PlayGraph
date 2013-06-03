import anorm.{Id, NotAssigned}
import models._
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import service.GraphSimulator

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 1/14/13
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
class GraphSimulationSpecification extends Specification {

  "The graph simulation engine" should {
    "support simple linear graphs" in new WithApplication {

      val basicGraph_c3 = NodeContent(NotAssigned, "Node 3").save
      val basicGraph_n3 = Node(NotAssigned, basicGraph_c3.id.get, 'data).save
      val basicGraph_c2 = NodeContent(NotAssigned, "Node 2").save
      val basicGraph_n2 = Node(NotAssigned, basicGraph_c2.id.get, 'data, List(Transition(basicGraph_n3.id.get, "true"))).save
      val basicGraph_c1 = NodeContent(NotAssigned, "Node 1").save
      val basicGraph_n1 = Node(NotAssigned, basicGraph_c1.id.get, 'data, List(Transition(basicGraph_n2.id.get, "true"))).save
      val basicGraph = Graph(NotAssigned, basicGraph_n1.id.get).save

      var session = GraphSimulator.start(basicGraph, "test")

      val content1 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content2 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content3 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      // Cleanup
      basicGraph_c3.delete()
      basicGraph_n3.delete()
      basicGraph_c2.delete()
      basicGraph_n2.delete()
      basicGraph_c1.delete()
      basicGraph_n1.delete()
      basicGraph.delete()

      session.finished must beGreaterThan(0l)
      content1 must beEqualTo("Node 1")
      content2 must beEqualTo("Node 2")
      content3 must beEqualTo("Node 3")
    }

    "support simple branching graphs" in new WithApplication {

      val basicGraph_c3 = NodeContent(NotAssigned, "Node 3").save
      val basicGraph_n3 = Node(NotAssigned, basicGraph_c3.id.get, 'data).save
      val basicGraph_c2 = NodeContent(NotAssigned, "Node 2").save
      val basicGraph_n2 = Node(NotAssigned, basicGraph_c2.id.get, 'data).save
      val basicGraph_c1 = NodeContent(NotAssigned, "Node 1").save
      val basicGraph_n1 = Node(NotAssigned, basicGraph_c1.id.get, 'data, List(Transition(basicGraph_n2.id.get, "next === 2;"), Transition(basicGraph_n3.id.get, "next === 3;"))).save
      val basicGraph = Graph(NotAssigned, basicGraph_n1.id.get).save

      // First time
      var session1 = GraphSimulator.start(basicGraph, "test")
      session1 = GraphSimulator.progress(session1, Map("next" -> "2"))
      val content1 = GraphSimulator.getCurrentContent(session1)
      session1 = GraphSimulator.progress(session1, Map())

      // First time
      var session2 = GraphSimulator.start(basicGraph, "test")
      session2 = GraphSimulator.progress(session2, Map("next" -> "3"))
      val content2 = GraphSimulator.getCurrentContent(session2)
      session2 = GraphSimulator.progress(session2, Map())

      // Cleanup
      basicGraph_c3.delete()
      basicGraph_n3.delete()
      basicGraph_c2.delete()
      basicGraph_n2.delete()
      basicGraph_c1.delete()
      basicGraph_n1.delete()
      basicGraph.delete()

      session1.finished must beGreaterThan(0l)
      session2.finished must beGreaterThan(0l)
      content1 must beEqualTo("Node 2")
      content2 must beEqualTo("Node 3")
    }

    "support nested graphs" in new WithApplication {

      val graph1_c2 = NodeContent(NotAssigned, "Graph 1: Node 2").save
      val graph1_n2 = Node(NotAssigned, graph1_c2.id.get, 'data).save
      val graph1_c1 = NodeContent(NotAssigned, "Graph 1: Node 1").save
      val graph1_n1 = Node(NotAssigned, graph1_c1.id.get, 'data, List(Transition(graph1_n2.id.get, "true"))).save
      val graph1 = Graph(NotAssigned, graph1_n1.id.get).save

      val graph2_c3 = NodeContent(NotAssigned, "Graph 2: Node 3").save
      val graph2_n3 = Node(NotAssigned, graph2_c3.id.get, 'data).save
      val graph2_n2 = Node(NotAssigned, graph1.id.get, 'graph, List(Transition(graph2_n3.id.get, "true"))).save
      val graph2_c1 = NodeContent(NotAssigned, "Graph 2: Node 1").save
      val graph2_n1 = Node(NotAssigned, graph2_c1.id.get, 'data, List(Transition(graph2_n2.id.get, "true"))).save
      val graph2 = Graph(NotAssigned, graph2_n1.id.get).save

      var session = GraphSimulator.start(graph2, "test")

      val content1 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content2 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content3 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content4 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      // Cleanup
      graph1_c2.delete()
      graph1_n2.delete()
      graph1_c1.delete()
      graph1_n1.delete()
      graph1.delete()
      graph2_c3.delete()
      graph2_n3.delete()
      graph2_n2.delete()
      graph2_c1.delete()
      graph2_n1.delete()
      graph2.delete()

      session.finished must beGreaterThan(0l)
      content1 must beEqualTo("Graph 2: Node 1")
      content2 must beEqualTo("Graph 1: Node 1")
      content3 must beEqualTo("Graph 1: Node 2")
      content4 must beEqualTo("Graph 2: Node 3")
    }

    "support node pools" in new WithApplication {

      val graph_c3 = NodeContent(NotAssigned, "Node 3").save
      val graph_n3 = Node(NotAssigned, graph_c3.id.get, 'data).save
      val graph_np_c1 = NodeContent(NotAssigned, "NP: 1").save
      val graph_np_n1 = Node(NotAssigned, graph_np_c1.id.get, 'data).save
      val graph_np_c2 = NodeContent(NotAssigned, "NP: 2").save
      val graph_np_n2 = Node(NotAssigned, graph_np_c2.id.get, 'data).save
      val graph_np_c3 = NodeContent(NotAssigned, "NP: 3").save
      val graph_np_n3 = Node(NotAssigned, graph_np_c3.id.get, 'data).save
      val graph_np = NodePool(NotAssigned, Set(graph_np_n1.id.get, graph_np_n2.id.get, graph_np_n3.id.get), "[nodes[0], nodes[1], nodes[2]];").save
      val graph_n2 = Node(NotAssigned, graph_np.id.get, 'nodepool, List(Transition(graph_n3.id.get, "true"))).save
      val graph_c1 = NodeContent(NotAssigned, "Node 1").save
      val graph_n1 = Node(NotAssigned, graph_c1.id.get, 'data, List(Transition(graph_n2.id.get, "true"))).save
      val graph = Graph(NotAssigned, graph_n1.id.get).save

      var session = GraphSimulator.start(graph, "test")

      val content1 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content2 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content3 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content4 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      val content5 = GraphSimulator.getCurrentContent(session)
      session = GraphSimulator.progress(session, Map())

      // Cleanup
      graph_c3.delete()
      graph_n3.delete()
      graph_np_c1.delete()
      graph_np_n1.delete()
      graph_np_c2.delete()
      graph_np_n2.delete()
      graph_np_c3.delete()
      graph_np_n3.delete()
      graph_np.delete()
      graph_n2.delete()
      graph_c1.delete()
      graph_n1.delete()
      graph.delete()

      session.finished must beGreaterThan(0l)
      content1 must beEqualTo("Node 1")
      Set("NP: 1", "NP: 2", "NP: 3") must containAllOf(Seq(content2, content3, content4))
      content5 must beEqualTo("Node 3")
    }
  }
}
