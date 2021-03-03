package com.bridgelabz.chat.users

import com.bridgelabz.chat.Routes.executor
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.User
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

  /**
   *
   * @param email of the recipient
   * @param body  of the email to be sent
   */
  def sendEmail(email: String,
                subject: String,
                body: String,
                mailProtocol: String = Constants.mailProtocol,
                mailStatusCode: Int = Constants.mailStatusCode): Unit = {

    val mailer = Mailer(mailProtocol, mailStatusCode)
      .auth(true)
      .as(Constants.senderEmail, Constants.senderPassword)
      .startTls(true)()
    mailer(Envelope.from(new InternetAddress(Constants.senderEmail))
      .to(new InternetAddress(email))
      .subject(subject)
      .content(Text(s"$body\nHappy to serve you!")))
      .onComplete {
        case Success(_) => logger.info(s"Notification email sent to $email")
        case Failure(exception) => logger.error(s"Email could not be sent: ${exception.getMessage}")
      }
  }

  /**
   *
   * @param user contains the email to which a verification link is to be sent
   * @return status message as a string to be printed for the user
   */
  def sendVerificationEmail(user: User,
                            mailProtocol: String = Constants.mailProtocol,
                            mailStatusCode: Int = Constants.mailStatusCode): Unit = {
    val token: String = TokenManager.generateToken(user.email)
    val longUrl = s"http://localhost:9000/verify?token=$token&email=${user.email}"

    sendEmail(user.email,
      "Token for Chat-App",
      s"Click on this link to verify your email address: $longUrl.",
      mailProtocol,
      mailStatusCode)
  }
}
