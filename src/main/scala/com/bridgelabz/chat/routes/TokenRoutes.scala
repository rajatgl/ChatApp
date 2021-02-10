package com.bridgelabz.chat.routes

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{_symbol2NR, complete, get, parameters, path}
import akka.http.scaladsl.server.Route
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.models.{OutputMessage, OutputMessageJsonFormat}
import com.bridgelabz.chat.utils.Utilities.tryAwait
import com.nimbusds.jose.JWSObject
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration.DurationInt

/**
 * Created on 1/29/2021.
 * Class: TokenRoutes.scala
 * Author: Rajat G.L.
 */
class TokenRoutes(databaseUtils: DatabaseUtils) extends OutputMessageJsonFormat {

  val logger: Logger = Logger("TokenRoutes")

  /**
   *
   * @return route for handling verification of a user (using JWT)
   */
  def verifyTokenRoute: Route = get {
    //path to verify JWT token for a given user
    path("verify") {
      parameters('token.as[String], 'email.as[String]) { (token, email) =>
        val jwsObject = JWSObject.parse(token)
        if (jwsObject.getPayload.toJSONObject.get("email").equals(email)) {
          logger.info("User Verified & Registered. ")
          val updateUserAsVerified = databaseUtils.verifyEmail(email)
          complete(StatusCodes.OK.intValue() ->
            OutputMessage(StatusCodes.OK.intValue(), "User successfully verified and registered!"))
        }
        else {
          logger.error(s"User Verification Failed- Email used: $email and token used: $token.")
          complete(StatusCodes.NOT_ACCEPTABLE.intValue() ->
            OutputMessage(StatusCodes.NOT_ACCEPTABLE.intValue(), "User could not be verified- this token seems to be invalid!"))
        }
      }
    }
  }
}
