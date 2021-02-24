package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Chat
import org.mongodb.scala.Completed

import scala.concurrent.Future

trait IChatService {
  def saveChat(chat: Chat): Future[Any]

  def getMessages(receiverId: String): Future[Seq[Chat]]

  def getSentMessages(senderId: String): Future[Seq[Chat]]
}
