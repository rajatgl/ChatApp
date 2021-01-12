package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{StringJsonFormat, jsonFormat2}
import spray.json.RootJsonFormat

trait LoginRequestJsonSupport {
  implicit val loginFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
}
