package com.bridgelabz.chat.database.interfaces

import akka.actor.ActorSystem
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.models.Chat
import org.mongodb.scala.Completed

import scala.concurrent.{ExecutionContext, Future}

trait ChatDatabase {

  def saveChat(chat: Chat): Future[Completed]
  def saveGroupChat(chat: Chat, executor: ExecutionContext, system: ActorSystem): Future[Completed]
  def getMessages(receiverId: String): Future[Seq[Chat]]
  def getSentMessages(senderId: String): Future[Seq[Chat]]
  def getGroupMessages(groupId: String): Future[Seq[Chat]]
}
