package models

import anorm._
import models.sqlTraits.{SQLSelectable, SQLDeletable, SQLSavable}
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import anorm.~
import scala.util.Random
import org.apache.commons.codec.binary.Hex

/**
 * Created with IntelliJ IDEA.
 * AuthToken: camman3d
 * Date: 5/31/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
case class AuthToken(publicKey: String, secretKey: String, permission: Symbol, name: String)
  extends SQLSavable with SQLDeletable{

  /**
   * Saves the auth token to the DB
   * @return The possibly modified auth token
   */
  def save: AuthToken =
    DB.withConnection {
      implicit connection =>
        SQL("insert into " + AuthToken.tableName + " (publicKey, secretKey, permission, name) values ({publicKey}, {secretKey}, {permission}, {name})")
          .on('publicKey -> publicKey, 'secretKey -> secretKey, 'permission -> permission.name, 'name -> name)
          .executeInsert()
    this
  }

  /**
   * Deletes the auth token from the DB
   */
  def delete() {
    DB.withConnection {
      implicit connection =>
        SQL("delete from " + AuthToken.tableName + " where publicKey = {id}").on('id -> publicKey).execute()
    }
  }

}

object AuthToken extends SQLSelectable[AuthToken] {
  val tableName = "authToken"

  val simple = {
    get[String](tableName + ".publicKey") ~
      get[String](tableName + ".secretKey") ~
      get[String](tableName + ".permission") ~
      get[String](tableName + ".name") map {
      case publicKey ~ secretKey ~ permission ~ name => AuthToken(publicKey, secretKey, Symbol(permission), name)
    }
  }

  /**
   * Finds an auth token by the publicKey
   * @param publicKey The publicKey of the auth token
   * @return If a auth token was found, then Some[AuthToken], otherwise None
   */
  def findByPublicKey(publicKey: String): Option[AuthToken] =
    DB.withConnection {
      implicit connection =>
        anorm.SQL("select * from " + tableName + " where publicKey = {id}").on('id -> publicKey).as(simple.singleOpt)
    }

  /**
   * Lists all auth tokens
   * @return The list of auth tokens
   */
  def list: List[AuthToken] = list(tableName, simple)

  def randomKey: String = {
    val input = Random.nextString(32)
    val md = java.security.MessageDigest.getInstance("SHA-1")
    new String(Hex.encodeHex(md.digest(input.getBytes)))
  }

}

