package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class OutputMessage(statusCode: Int, message: String)

trait OutputMessageJsonFormat extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val outputJsonFormat: RootJsonFormat[OutputMessage] = jsonFormat2(OutputMessage)
}

case class LoginMessage(token: String, statusCode: Int, message: String)

trait LoginMessageJsonFormat extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val loginJsonFormat: RootJsonFormat[LoginMessage] = jsonFormat3(LoginMessage)
}