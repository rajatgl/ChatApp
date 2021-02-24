package com.bridgelabz.chat.users

import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.Routes.executor
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{OutputMessage, User}
import com.typesafe.scalalogging.Logger
import courier.{Envelope, Mailer, Text}
import javax.mail.internet.InternetAddress

import scala.util.{Failure, Success}

/**
 * Created on 2/23/2021.
 * Class: EmailManager.scala
 * Author: Rajat G.L.
 */
class EmailManager {

  private val logger = Logger("EmailManager")
  private val smtpCode = 587

  /**
   *
   * @param user contains the email to which a verification link is to be sent
   * @return status message as a string to be printed for the user
   */
  def sendVerificationEmail(user: User): OutputMessage = {
    val token: String = TokenManager.generateToken(user.email)
    val longUrl = "http://localhost:9000/verify?token=" + token + "&email=" + user.email

    val mailer = Mailer("smtp.gmail.com", smtpCode)
      .auth(true)
      .as(System.getenv("SENDER_EMAIL"), System.getenv("SENDER_PASSWORD"))
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(System.getenv("SENDER_EMAIL")))
      .to(new InternetAddress(user.email))
      .subject("Token")
      .content(Text(s"Click on this link to verify your email address: $longUrl. Happy to serve you!")))
      .onComplete {
        case Success(_) =>
          logger.info(s"Verification email sent to ${user.email}")
          OutputMessage(StatusCodes.OK.intValue(), "Verification link sent!")
        case Failure(exception) =>
          logger.error(s"Failed to send verification email: ${exception.getMessage}")
          OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Failed to verify user!")
      }

    OutputMessage(StatusCodes.OK.intValue(), "Verification link sent!") //guaranteed return
  }

  /**
   *
   * @param email of the recipient
   * @param body  of the email to be sent
   */
  def sendEmail(email: String, body: String): Unit = {
    val mailer = Mailer("smtp.gmail.com", smtpCode)
      .auth(true)
      .as(System.getenv("SENDER_EMAIL"), System.getenv("SENDER_PASSWORD"))
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(System.getenv("SENDER_EMAIL")))
      .to(new InternetAddress(email))
      .subject("You have received a message")
      .content(Text(s"$body\nHappy to serve you!")))
      .onComplete {
        case Success(_) => logger.info(s"Notification email sent to $email")
        case Failure(exception) => logger.error(s"Email could not be sent: ${exception.getMessage}")
      }
  }
}
