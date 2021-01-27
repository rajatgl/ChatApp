package com.bridgelabz.chattest

import com.bridgelabz.chat.models.LoginRequest
import org.scalatest.wordspec.AnyWordSpec


class RoutesTest extends AnyWordSpec {

  "Routes should register a test account" in {

    val dummyUser = TestVariables.user("test@gmail.com")
    val loginRequest: LoginRequest = LoginRequest(dummyUser.email, dummyUser.password)

    //TODO: fix implicit error

    //val loginEntity: MessageEntity = Marshal(loginRequest).to[MessageEntity].futureValue
    //val request = Post("/register").withEntity(loginEntity)
  }
}
