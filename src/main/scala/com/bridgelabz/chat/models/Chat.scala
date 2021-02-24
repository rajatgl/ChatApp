package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{StringJsonFormat, jsonFormat3}
import spray.json.RootJsonFormat

final case class Chat(sender: String, receiver: String, message: String)

trait ChatJsonSupport {
  implicit val chatFormat: RootJsonFormat[Chat] = jsonFormat3(Chat)
}
