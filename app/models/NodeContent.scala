package models

import anorm.{Id, ~, Pk}
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import anorm.SqlParser._
import play.api.libs.json.{JsValue, JsArray, Json}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 9:57 AM
 * To change this template use File | Settings | File Templates.
 */
case class NodeContent(id: Pk[Long], content: String) extends SQLSavable with SQLDeletable {

  /**
   * Saves the node content to the DB
   * @return The possibly modified node content
   */
  def save: NodeContent = {
    if (id.isDefined) {
      update(NodeContent.tableName, 'id -> id, 'content -> content)
      this
    } else {
      val id = insert(NodeContent.tableName, 'content -> content)
      this.copy(id)
    }
  }

  /**
   * Deletes the node content from the DB
   */
  def delete() {
    delete(NodeContent.tableName, id)
  }

  def toJson = Json.obj(
    "id" -> id.get,
    "content" -> content
  )

}

object NodeContent extends SQLSelectable[NodeContent] {
  val tableName = "nodeContent"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[String](tableName + ".content") map {
      case id ~ content => NodeContent(id, content)
    }
  }

  /**
   * Finds a node content by the id
   * @param id The id of the node content
   * @return If a node content was found, then Some[NodeContent], otherwise None
   */
  def findById(id: Long): Option[NodeContent] = findById(tableName, id, simple)

  /**
   * Lists all node contents
   * @return The list of node contents
   */
  def list: List[NodeContent] = list(tableName, simple)

  def fromJson(json: JsValue) = NodeContent(Id((json \ "id").as[Long]), (json \ "content").as[String])
}
