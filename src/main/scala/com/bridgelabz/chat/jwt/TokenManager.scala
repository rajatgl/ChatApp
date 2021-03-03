package com.bridgelabz.chat.jwt

import java.util.concurrent.TimeUnit

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.models.User

/**
 * Created on 1/8/2021.
 * Class: TokenManager.scala
 * Author: Rajat G.L.
 */
object TokenManager {
  /**
   *
   * @param identifier string to be tokenized
   * @param tokenExpiryPeriodInDays token duration in days
   * @param header jwtHeader( encryption method)
   * @param secretKey for encryption
   * @return token
   */
  def generateToken(identifier: String,
                    tokenExpiryPeriodInDays: Int = Constants.tokenExpiryPeriodInDays,
                    header: JwtHeader = JwtHeader(Constants.encryptionType),
                    secretKey: String = Constants.secretKey): String = {

    val claimSet = JwtClaimsSet(
      Map(
        "identifier" -> identifier,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(tokenExpiryPeriodInDays))
      )
    )
    JsonWebToken(header, claimSet, secretKey)
  }

  /**
   * overloaded generateToken method to accept user object
   *
   * @param user object to be passed
   * @param tokenExpiryPeriodInDays token duration in days
   * @param header jwtHeader( encryption method)
   * @param secretKey for encryption
   * @return token
   */
  def generateUserToken(user: User,
                    tokenExpiryPeriodInDays: Int = Constants.tokenExpiryPeriodInDays,
                    header: JwtHeader = JwtHeader(Constants.encryptionType),
                    secretKey: String = Constants.secretKey): String = {

    val identifier = s"${user.email}!${user.password}"
    generateToken(identifier, tokenExpiryPeriodInDays, header, secretKey)
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
