package com.bridgelabz.chat.models

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.bridgelabz.chat.Routes.executor
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.users.EmailManager
import com.typesafe.scalalogging.Logger
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.{Failure, Success}

final case class User(email: String, password: String, verificationComplete: Boolean)
class UserActor(databaseUtils: DatabaseUtils = new DatabaseUtils) extends Actor {

  var logger: Logger = Logger("UserActor")
  val emailManager: EmailManager = new EmailManager

  override def receive: Receive = {
    case Chat(sender, receiver, message) =>
      logger.debug(s"Message $message,was sent by $sender and received by $receiver")
      val futureChat = databaseUtils.saveChat(Chat(sender, receiver, message))

      futureChat.onComplete{
        case Success(_) => emailManager.sendEmail(receiver,"You have received a new message.",
            s"Your message reads: $message\n\nThis was sent by $sender")
        case Failure(exception) => logger.error(exception.getMessage)
      }
  }
}
trait UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
}
