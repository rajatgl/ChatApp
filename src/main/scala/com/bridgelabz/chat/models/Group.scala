package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Group(groupId: String, groupName: String, admin: String, participants: Seq[String])

trait GroupJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupJsonFormat: RootJsonFormat[Group] = jsonFormat4(Group)
}

case class GroupName(groupName: String)

trait GroupNameJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupNameJsonFormat: RootJsonFormat[GroupName] = jsonFormat1(GroupName)
}

case class GroupAddUser(groupName: String, participantEmails: Seq[String])

trait GroupAddUserJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupAddUserJsonFormat: RootJsonFormat[GroupAddUser] = jsonFormat2(GroupAddUser)
}
