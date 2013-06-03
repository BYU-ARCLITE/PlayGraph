package controllers.api

import play.api.mvc._
import play.api.mvc.Results._
import models.{GraphSession, AuthToken, User}
import service.joshmonson.oauth.{OAuthValues, OAuthKey, OAuthRequest}
import java.net.URLEncoder
import play.core.parsers.FormUrlEncodedParser

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/31/13
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */
object Authentication extends Controller {

  def authenticatedAction(permission: Symbol)(f: Request[AnyContent] => AuthToken => Result) = Action(BodyParsers.parse.tolerantText) {
    implicit request =>

      // Create the OAuth request wrapper
      val oauthRequest = OAuthRequest(request.headers.get("Authorization"), request.headers.get("Content-Type"),
        request.host, request.rawQueryString, request.body, request.method, request.path)

      // Get the auth token from the oauth package
      val token = oauthRequest.collectParameters.get("oauth_consumer_key").flatMap(AuthToken.findByPublicKey(_))

      // Check that the token is for the right permission level
      if (token.isDefined && token.get.permission == permission) {

        // Now check the OAuth signature
        val oauthKey = OAuthKey(token.get.publicKey, token.get.secretKey, "", "")
        if (oauthRequest.verify(oauthKey)) {

          // Turn the request into something more usable (not a string body) if a post
          val body =
            if (request.contentType.isDefined && request.contentType.get == OAuthValues.urlEncodedContentType)
              AnyContentAsFormUrlEncoded(FormUrlEncodedParser.parse(request.body, "utf-8"))
            else
              AnyContentAsText(request.body)
          val newRequest = Request(request.asInstanceOf[RequestHeader], body)

          f(newRequest)(token.get)
        } else
          Unauthorized.withHeaders("Access-Control-Allow-Origin" -> "*")
      } else
        Unauthorized.withHeaders("Access-Control-Allow-Origin" -> "*")
  }

  def options(path: String) = Action {
    implicit request =>
      Ok.withHeaders(
        //        "Access-Control-Allow-Origin" -> request.host,
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
        "Access-Control-Allow-Headers" -> "Authorization, Content-Type"
      )
  }
}
