package models

import anorm.{~, Pk}
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import anorm.SqlParser._
import play.api.libs.json.Json
import play.api.db.DB
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/30/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
case class Graph(id: Pk[Long], startNode: Long, authorId: Long) extends SQLSavable with SQLDeletable {

  /**
   * Saves the graph to the DB
   * @return The possibly modified graph
   */
  def save: Graph = {
    if (id.isDefined) {
      update(Graph.tableName, 'id -> id, 'startNode -> startNode, 'authorId -> authorId)
      this
    } else {
      val id = insert(Graph.tableName, 'startNode -> startNode, 'authorId -> authorId)
      this.copy(id)
    }
  }

  /**
   * Deletes the graph from the DB
   */
  def delete() {
    delete(Graph.tableName, id)
  }

  def toJson = Json.obj(
    "id" -> id.get,
    "startNode" -> startNode,
    "authorId" -> authorId
  )
}

object Graph extends SQLSelectable[Graph] {
  val tableName = "graph"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[Long](tableName + ".startNode") ~
      get[Long](tableName + ".authorId") map {
      case id ~ startNode ~ authorId => Graph(id, startNode, authorId)
    }
  }

  /**
   * Finds a graph by the id
   * @param id The id of the graph
   * @return If a graph was found, then Some[Graph], otherwise None
   */
  def findById(id: Long): Option[Graph] = findById(tableName, id, simple)

  /**
   * Lists all graphs
   * @return The list of graphs
   */
  def list: List[Graph] = list(tableName, simple)

  def listByAuthorId(authorId: Long): List[Graph] = {
    DB.withConnection {
      implicit connection =>
        anorm.SQL("select * from " + tableName + " where authorId = {id}").on('id -> authorId).as(simple *)
    }
  }

  //  def fromJson(json: JsValue) = Graph(
  //    Id((json \ "id").as[Long]),
  //    (json \ "startNode").as[Long],
  //    (json \ "startNode").as[Long]
  //  )
}
