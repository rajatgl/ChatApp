package com.bridgelabz.chat.users

import com.bridgelabz.chat.Routes.executor
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{OutputMessage, User}
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
    users.foreach(mainUser =>
      if (EncryptionManager.verify(mainUser, user.password)) {
        if(!mainUser.verificationComplete)
          return 400
        return 200
      }
    )
    404
  }

  /**
   *
   * @param user the object that is needed to be inserted into the database
   * @return status of the above insertion operation (2xx return preferable)
   */
  def createNewUser(user: User): Int = {
    DatabaseUtils.saveUser(user)
  }

  /**
   *
   * @param user contains the email to which a verification link is to be sent
   * @return status message as a string to be printed for the user
   */
  def sendVerificationEmail(user: User): OutputMessage = {
    val token: String = TokenManager.generateToken(user.email)
    val longUrl = "http://localhost:9000/verify?token=" + token + "&email=" + user.email

    val mailer = Mailer("smtp.gmail.com", 587)
      .auth(true)
      .as(System.getenv("SENDER_EMAIL"),System.getenv("SENDER_PASSWORD"))
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(System.getenv("SENDER_EMAIL")))
      .to(new InternetAddress(user.email))
      .subject("Token")
      .content(Text(s"Click on this link to verify your email address: ${longUrl}. Happy to serve you!")))
      .onComplete {
        case Success(_) => return OutputMessage(220, "Verification link sent!")
        case Failure(_) => return OutputMessage(440, "Failed to verify user!")
      }

    OutputMessage(440, "Failed to verify user!") //guaranteed return
  }
}
