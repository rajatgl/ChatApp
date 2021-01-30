package com.bridgelabz.chat.jwt

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives.{complete, headerValueByName}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.bridgelabz.chat.models.{OutputMessage, User}

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
