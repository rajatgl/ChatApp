package com.bridgelabz.chattest

import com.bridgelabz.chat.models.{Chat, Group, User}

/**
 * Created on 1/26/2021.
 * Class: TestVariables.scala
 * Author: Rajat G.L.
 */
object TestVariables {

  def user(email: String = "invalid", password: String = "helloworld", verificationComplete: Boolean = false): User =
    User(email, password, verificationComplete)
  def groupId(id: String = "badGroupId"): String = id
  def group(id: String = "randomREMOVE"): Group = Group(id, "random", "random", Seq())
  def chat(sender: String = "test@gmail.com", receiver: String = "test@gmail.com", message: String = "Hello"): Chat = Chat(sender, receiver, message)
}
