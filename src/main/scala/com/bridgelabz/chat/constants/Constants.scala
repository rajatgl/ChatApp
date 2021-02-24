package com.bridgelabz.chat.constants

/**
 * Created on 2/8/2021.
 * Class: Constants.scala
 * Author: Rajat G.L.
 */
object Constants {
  val databaseName: String = "ChatApp"
  val collectionName: String = "Users"
  val collectionNameForChat: String = "Chats"
  val collectionNameForGroupChat: String = "GroupChat"
  val collectionNameForGroup: String = "Groups"
  val secretKey = "a$iq!@oop"
  val tokenExpiryPeriodInDays = 1
  val emailRegex = "^[a-zA-Z0-9+-._]+@[a-zA-Z0-9.-]+$"
  val mongoHost = "localhost"
  val mongoPort = "27017"
}
