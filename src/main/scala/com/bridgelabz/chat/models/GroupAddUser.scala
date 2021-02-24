package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class GroupAddUser(groupName: String, participantEmails: Seq[String])

trait GroupAddUserJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupAddUserJsonFormat: RootJsonFormat[GroupAddUser] = jsonFormat2(GroupAddUser)
}
