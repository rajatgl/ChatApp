package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class Communicate(receiver: String, message: String)

trait CommunicateJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val communicateFormat: RootJsonFormat[Communicate] = jsonFormat2(Communicate)
}
