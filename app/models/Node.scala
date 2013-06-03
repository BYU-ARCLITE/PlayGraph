package models

import anorm.{~, Pk}
import anorm.SqlParser._
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import play.api.libs.json.{JsArray, Json}


case class Node(id: Pk[Long], contentId: Long, contentType: Symbol, transitions: List[Transition] = Nil)
  extends SQLSavable with SQLDeletable {

  /**
   * Saves the node to the DB
   * @return The possibly modified node
   */
  def save: Node = {
    if (id.isDefined) {
      update(Node.tableName, 'id -> id, 'contentId -> contentId, 'contentType -> contentType.name,
        'transitions -> transitions.map(_.toJson).mkString("[", ",", "]"))
      this
    } else {
      val id = insert(Node.tableName, 'contentId -> contentId, 'contentType -> contentType.name,
        'transitions -> transitions.map(_.toJson).mkString("[", ",", "]"))
      this.copy(id)
    }
  }

  /**
   * Deletes the node from the DB
   */
  def delete() {
    delete(Node.tableName, id)
  }

  def addTransition(transition: Transition) = copy(transitions = transition :: transitions)

  def toJson = Json.obj(
    "id" -> id.get,
    "contentId" -> contentId,
    "contentType" -> contentType.name,
    "transitions" -> transitions.map(_.toJson)
  )

}

object Node extends SQLSelectable[Node] {
  val tableName = "node"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[Long](tableName + ".contentId") ~
      get[String](tableName + ".contentType") ~
      get[String](tableName + ".transitions") map {
      case id ~ contentId ~ contentType ~ transitions =>
        Node(id, contentId, Symbol(contentType),
          Json.parse(transitions).as[JsArray].value.map(j => Transition.fromJson(j)).toList)
    }
  }

  /**
   * Finds a node by the id
   * @param id The id of the node
   * @return If a node was found, then Some[Node], otherwise None
   */
  def findById(id: Long): Option[Node] = findById(tableName, id, simple)

   /**
   * Lists all nodes
   * @return The list of nodes
   */
  def list: List[Node] = list(tableName, simple)
}