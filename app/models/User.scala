package models

import anorm.{~, Pk}
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
case class User(id: Pk[Long], username: String, password: String, keys: List[String])
  extends SQLSavable with SQLDeletable {

  /**
   * Saves the user to the DB
   * @return The possibly modified user
   */
  def save: User = {
    if (id.isDefined) {
      update(User.tableName, 'id -> id, 'username -> username, 'password -> password, 'authKeys -> keys.mkString(","))
      this
    } else {
      val id = insert(User.tableName, 'username -> username, 'password -> password, 'authKeys -> keys.mkString(","))
      this.copy(id)
    }
  }

  /**
   * Deletes the user from the DB
   */
  def delete() {
    delete(User.tableName, id)
  }

}

object User extends SQLSelectable[User] {
  val tableName = "user"

  val simple = {
    get[Pk[Long]](tableName + ".id") ~
      get[String](tableName + ".username") ~
      get[String](tableName + ".password") ~
      get[String](tableName + ".authKeys") map {
      case id ~ username ~ password ~ keys => User(id, username, password, keys.split(",").filterNot(_.isEmpty).toList)
    }
  }

  /**
   * Finds a user by the id
   * @param id The id of the user
   * @return If a user was found, then Some[User], otherwise None
   */
  def findById(id: Long): Option[User] = findById(tableName, id, simple)

  def findByAuthInfo(username: String, password: String): Option[User] =
    DB.withConnection {
      implicit connection =>
        anorm.SQL(s"select * from $tableName where username = {username} and password = {password}")
          .on('username -> username, 'password -> password).as(simple.singleOpt)
    }

  /**
   * Lists all users
   * @return The list of users
   */
  def list: List[User] = list(tableName, simple)

}
