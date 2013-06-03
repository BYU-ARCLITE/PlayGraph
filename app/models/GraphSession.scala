package models

import anorm.{~, Pk}
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import play.api.libs.json.Json
import anorm.SqlParser._

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
case class GraphSession(id: Pk[Long], graph: Long, state: GraphState, started: Long, finished: Long, publicKey: String)
  extends SQLSavable with SQLDeletable {

  /**
   * Saves the graph session to the DB
   * @return The possibly modified graph session
   */
  def save: GraphSession = {
    if (id.isDefined) {
      update(GraphSession.tableName, 'id -> id, 'graph -> graph, 'state -> state.toJson.toString(), 'started -> started,
        'finished -> finished, 'publicKey -> publicKey)
      this
    } else {
      val id = insert(GraphSession.tableName, 'graph -> graph, 'state -> state.toJson.toString(), 'started -> started,
        'finished -> finished, 'publicKey -> publicKey)
      this.copy(id)
    }
  }

  /**
   * Deletes the graph session from the DB
   */
  def delete() {
    delete(GraphSession.tableName, id)
  }
}

object GraphSession extends SQLSelectable[GraphSession] {
  val tableName = "graphSession"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[Long](tableName + ".graph") ~
      get[String](tableName + ".state") ~
      get[Long](tableName + ".started") ~
      get[Long](tableName + ".finished") ~
      get[String](tableName + ".publicKey") map {
      case id ~ graph ~ state ~ started ~ finished ~ publicKey =>
        GraphSession(id, graph, GraphState.fromJson(Json.parse(state)), started, finished, publicKey)
    }
  }

  /**
   * Finds a graph session by the id
   * @param id The id of the graph session
   * @return If a graph session was found, then Some[GraphSession], otherwise None
   */
  def findById(id: Long): Option[GraphSession] = findById(tableName, id, simple)

  /**
   * Lists all graph sessions
   * @return The list of graph sessions
   */
  def list: List[GraphSession] = list(tableName, simple)

}

