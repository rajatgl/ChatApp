package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class GroupName(groupName: String)

trait GroupNameJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val groupNameJsonFormat: RootJsonFormat[GroupName] = jsonFormat1(GroupName)
}