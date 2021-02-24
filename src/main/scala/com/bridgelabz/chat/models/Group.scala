package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Group(groupId: String, groupName: String, admin: String, participants: Seq[String])

trait GroupJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupJsonFormat: RootJsonFormat[Group] = jsonFormat4(Group)
}
