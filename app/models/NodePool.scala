package models

import anorm.{Id, ~, Pk}
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import anorm.SqlParser._
import play.api.libs.json.{JsValue, JsArray, Json}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 10:01 AM
 * To change this template use File | Settings | File Templates.
 */
case class NodePool(id: Pk[Long], nodes: Set[Long], script: String) extends SQLSavable with SQLDeletable {

  /**
   * Saves the node pool to the DB
   * @return The possibly modified node pool
   */
  def save: NodePool = {
    if (id.isDefined) {
      update(NodePool.tableName, 'id -> id, 'nodes -> nodes.mkString(","), 'script -> script)
      this
    } else {
      val id = insert(NodePool.tableName, 'nodes -> nodes.mkString(","), 'script -> script)
      this.copy(id)
    }
  }

  /**
   * Deletes the node pool from the DB
   */
  def delete() {
    delete(NodePool.tableName, id)
  }

  def toJson = Json.obj(
    "id" -> id.get,
    "nodes" -> nodes,
    "script" -> script
  )

}

object NodePool extends SQLSelectable[NodePool] {
  val tableName = "nodePool"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[String](tableName + ".nodes") ~
      get[String](tableName + ".script") map {
      case id ~ nodes ~ script => NodePool(id, nodes.split(",").map(_.toLong).toSet, script)
    }
  }

  /**
   * Finds a node pool by the id
   * @param id The id of the node pool
   * @return If a node pool was found, then Some[NodePool], otherwise None
   */
  def findById(id: Long): Option[NodePool] = findById(tableName, id, simple)

  /**
   * Lists all node pools
   * @return The list of node pools
   */
  def list: List[NodePool] = list(tableName, simple)

  def fromJson(json: JsValue) = NodePool(
    Id((json \ "id").as[Long]),
    (json \ "nodes").as[JsArray].value.map(_.as[Long]).toSet,
    (json \ "script").as[String]
  )
}
