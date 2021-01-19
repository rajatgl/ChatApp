package com.bridgelabz.chat.models

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.users.UserManager
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class User(email: String, password: String, verificationComplete: Boolean)
class UserActor extends Actor {
  override def receive: Receive = {
    case Chat(sender, receiver, message) => println(s"Message ${message},was sent by ${sender} and received by ${receiver}")
      DatabaseUtils.saveChat(Chat(sender, receiver, message))
      UserManager.sendEmail(receiver, s"Your message reads: ${message}\n\nThis was sent by ${sender}")
  }
}
trait UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
}