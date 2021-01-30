package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Chat
import org.mongodb.scala.Completed

trait ChatDatabase {

  def saveChat(chat: Chat): Option[Completed]
  def saveGroupChat(chat: Chat): Option[Completed]
  def getMessages(receiverId: String): Seq[Chat]
  def getSentMessages(senderId: String): Seq[Chat]
  def getGroupMessages(groupId: String): Seq[Chat]
}
