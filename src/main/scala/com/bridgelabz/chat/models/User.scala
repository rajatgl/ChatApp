package com.bridgelabz.chat.models

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.bridgelabz.chat.database.DatabaseUtils
import com.softwaremill.session.{MultiValueSessionSerializer, SessionSerializer, SingleValueSessionSerializer}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.util.Try

final case class User(email: String, password: String, verificationComplete: Boolean)
class UserActor extends Actor {
  override def receive: Receive = {
    case Chat(sender, receiver, message) => println(s"Message ${message},was sent by ${sender} and received by ${receiver}")
      DatabaseUtils.saveChat(Chat(sender, receiver, message))
  }
}
trait UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
  implicit def serializer: SessionSerializer[User, String] =
    new MultiValueSessionSerializer(user => Map[String, String](user.email -> user.password),
      map =>
        Try {
          User(map.head._1, map.head._2, verificationComplete = true)
        })
}