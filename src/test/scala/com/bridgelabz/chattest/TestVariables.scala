package com.bridgelabz.chattest

import com.bridgelabz.chat.models.User

/**
 * Created on 1/26/2021.
 * Class: TestVariables.scala
 * Author: Rajat G.L.
 */
object TestVariables {

  def user(email: String = "invalidemail", password: String = "helloworld", verificationComplete: Boolean = false): User = {
    User(email, password, verificationComplete)
  }

  def groupId(id: String = "badGroupId"): String ={
    id
  }
}
