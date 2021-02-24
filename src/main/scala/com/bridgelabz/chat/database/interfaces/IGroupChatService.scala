package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Chat

import scala.concurrent.Future

trait IGroupChatService {
  def saveGroupChat(chat: Chat): Future[Any]

  def getGroupMessages(groupId: String): Future[Seq[Chat]]
}
