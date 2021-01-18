package com.bridgelabz.chat.jwt

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, headerValueByName, optionalHeaderValueByName, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.bridgelabz.chat.models.{OutputMessage, OutputMessageJsonFormat, User}

/**
 * Created on 1/8/2021.
 * Class: TokenManager.scala
 * Author: Rajat G.L.
 */
object TokenManager {

  val secretKey = "a$iq!@oop"
  val header = JwtHeader("HS256", "JWT")
   val tokenExpiryPeriodInDays = 1

  /**
   *
   * @param email of user instance to generate a unique token
   * @return token
   */
  def generateToken(email: String): String = {

    val claimSet = JwtClaimsSet(
      Map(
        "email" -> email,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(tokenExpiryPeriodInDays))
      )
    )
    JsonWebToken(header, claimSet, secretKey)
  }

  def generateLoginId(user: User): String = {
    val claimSet = JwtClaimsSet(
      Map(
        "user" -> (user.email + "!" + user.password),
        "expiredAt" -> (System.currentTimeMillis() + (24*60*60*1000))
      )
    )
    JsonWebToken(header, claimSet, secretKey)
  }

  /**
   *
   * @return authentication status message
   */
  def authenticated(outputMessage: OutputMessage): Unit = {

    headerValueByName("Authorization") { tokenFromUser =>

      val jwtToken = tokenFromUser.split(" ")
      jwtToken(1) match {
        case token if isTokenExpired(token) =>
          complete(401 -> "Token has expired. Please login again.")

        case token if !JsonWebToken.validate(token, secretKey) =>
          complete(401 -> "Token is invalid. Please login again to generate a new one.")
      }
    }
  }

  /**
   *
   * @param token to check if its expired
   * @return boolean result of the same
   */
  def isTokenExpired(token: String): Boolean =
    getClaims(token).get("expiredAt").exists(_.toLong < System.currentTimeMillis())

  /**
   *
   * @param token to be claimed
   * @return if all tokens claimed, return an empty map else return the tokens/claims remaining
   */
  def getClaims(token: String): Map[String, String] =
    JsonWebToken.unapply(token) match {
      case Some(value) => value._2.asSimpleMap.getOrElse(Map.empty[String, String])
      case None => Map.empty[String, String]
    }
}
