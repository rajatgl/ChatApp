package com.bridgelabz.chat.users

import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.User
import courier.{Envelope, Mailer, Text}
import javax.mail.internet.InternetAddress

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created on 1/8/2021.
 * Class: UserManager.scala
 * Author: Rajat G.L.
 */
object UserManager {
  /**
   *
   * @param user instance to be logged in
   * @return status message of login operation
   */
  def userLogin(user: User): Int = {

    val users = Await.result(DatabaseUtils.getUsers(user.email), 60.seconds)
    users.foreach(user =>
      if (user.password.equals(user.password)) {
        if(!user.verificationComplete)
          return 400
        return 200
      }
    )
    404
  }

  def createNewUser(user: User): Int = {
    val statusMessage = DatabaseUtils.saveUser(user)
    statusMessage match {
      case "Saving User Failed" => 410
      case "Validation Failed" => 414
      case _ => 215
    }
  }

  def sendVerificationEmail(user: User): String = {
    println("Username: " + System.getenv("SENDER_EMAIL") + ", Password: " + System.getenv("SENDER_PASSWORD"))
    val token: String = TokenManager.generateToken(user.email)
    "Head to http://localhost:9000/verify?token=" + token + "&email=" + user.email + " to verify the email."
  }
}
