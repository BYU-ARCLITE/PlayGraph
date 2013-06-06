package controllers

import play.api.mvc._
import models.{AuthToken, User}
import service.joshmonson.oauth.Crypto
import anorm.NotAssigned

object Application extends Controller {

  def authenticatedAction(f: Request[_] => User => Result): Action[AnyContent] = authenticatedAction(parse.anyContent)(f)

  def authenticatedAction[A](parser: BodyParser[A] = parse.anyContent)(f: Request[A] => User => Result): Action[A] = Action(parser) {
    request =>
      val user = request.session.get("userId").flatMap(id => User.findById(id.toLong))
      if (user.isDefined) {
        f(request)(user.get)
      } else
        Redirect(routes.Application.index()).flashing("error" -> "You are not logged in")
  }

  def index = Action {
    implicit request =>
      Ok(views.html.index())
  }

  def loginPage = Action {
    implicit request =>
      Ok(views.html.login())
  }

  def login = Action(parse.urlFormEncoded) {
    request =>
      val username = request.body("username")(0)
      val password = Crypto.sha1Base64(request.body("username")(0))
      val user = User.findByAuthInfo(username, password)
      if (user.isDefined)
        Redirect(routes.Application.home()).withSession("userId" -> user.get.id.get.toString)
      else
        Redirect(routes.Application.loginPage()).flashing("error" -> "Invalid credentials")
  }

  def createAccountPage = Action {
    implicit request =>
      Ok(views.html.createAccount())
  }

  def createAccount = Action(parse.urlFormEncoded) {
    request =>
      val username = request.body("username")(0)
      val password = Crypto.sha1Base64(request.body("username")(0))
      val user = User(NotAssigned, username, password, Nil).save
      Redirect(routes.Application.home()).withSession("userId" -> user.id.get.toString)
  }

  def home = authenticatedAction {
    implicit request =>
      implicit user =>
        Ok(views.html.home())
  }

  def createToken = authenticatedAction(parse.urlFormEncoded) {
    implicit request =>
      implicit user =>
        val permission = Symbol(request.body("permission")(0))
        val name = request.body("name")(0)
        val token = AuthToken(AuthToken.randomKey, AuthToken.randomKey, permission, name).save
        user.copy(keys = token.publicKey :: user.keys).save
        Redirect(routes.Application.home()).flashing("info" -> "Auth Token created.")
  }

  def deleteToken(id: String) = authenticatedAction {
    request =>
      user =>
        user.copy(keys = user.keys.filterNot(_ == id)).save
        AuthToken.findByPublicKey(id).map(_.delete())
        Redirect(routes.Application.home()).flashing("info" -> "Auth Token deleted.")
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def authorToken = authenticatedAction {
    implicit request =>
      implicit user =>
        val tokens = user.keys.map(AuthToken.findByPublicKey(_).get)
        Ok(views.html.authorTokens(tokens))
  }

  def author = authenticatedAction(parse.urlFormEncoded) {
    implicit request =>
      implicit user =>
        val authorToken = AuthToken.findByPublicKey(request.body("author")(0)).get
        val playerToken = AuthToken.findByPublicKey(request.body("player")(0))
        Ok(views.html.author(authorToken, playerToken))
  }

}