package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{StringJsonFormat, jsonFormat3}
import spray.json.RootJsonFormat

trait ChatJsonSupport {
  implicit val loginFormat: RootJsonFormat[Chat] = jsonFormat3(Chat)
}
