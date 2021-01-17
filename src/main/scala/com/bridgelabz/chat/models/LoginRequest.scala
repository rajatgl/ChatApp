package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{StringJsonFormat, jsonFormat2}
import spray.json.RootJsonFormat

final case class LoginRequest(email: String, password: String)
trait LoginRequestJsonSupport {
  implicit val loginFormat: RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest)
}

