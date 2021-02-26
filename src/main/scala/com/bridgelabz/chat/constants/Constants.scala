package com.bridgelabz.chat.constants

/**
 * Created on 2/8/2021.
 * Class: Constants.scala
 * Author: Rajat G.L.
 */
object Constants{
  val databaseName: String = System.getenv("DATABASE_NAME")
  val collectionName: String = System.getenv("COLLECTION_NAME")
  val collectionNameForChat: String = System.getenv("COLLECTION_NAME _FOR_CHAT")
  val collectionNameForGroupChat: String = System.getenv("COLLECTION_NAME_FOR_GROUP_CHAT")
  val collectionNameForGroup: String = System.getenv("COLLECTION_NAME_FOR_GROUP")
  val secretKey: String = System.getenv("SECRET_KEY")
  val tokenExpiryPeriodInDays: Int = System.getenv("TOKEN_EXPIRY_IN_DAYS").toInt
  val emailRegex: String = System.getenv("EMAIL_REGEX")
  val mongoHost: String = System.getenv("MONGO_HOST")
  val mongoPort: Int = System.getenv("MONGO_PORT").toInt
  val mailProtocol: String = System.getenv("MAIL_PROTOCOL")
  val mailStatusCode: Int = System.getenv("MAIL_STATUS_CODE").toInt
  val encryptionType: String = System.getenv("ENCRYPTION_TYPE")

  val senderEmail: String = System.getenv("SENDER_EMAIL")
  val senderPassword: String = System.getenv("SENDER_PASSWORD")

  val host: String = System.getenv("HOST")
  val port: Int = System.getenv("PORT").toInt
}
