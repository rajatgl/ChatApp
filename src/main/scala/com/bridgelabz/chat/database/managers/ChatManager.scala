package com.bridgelabz.chat.database.managers

import akka.actor.{ActorRef, ActorSystem, Props}
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.database.interfaces.ICrud
import com.bridgelabz.chat.models.{Chat, Group, UserActor}
import com.typesafe.scalalogging.Logger
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{ExecutionContext, Future}

class ChatManager(database: ICrud[Chat],
                  executorContext: ExecutionContext = Routes.executor,
                  actorSystem: ActorSystem = Routes.system) {

  private val logger = Logger("DatabaseUtils")
  implicit val executor: ExecutionContext = executorContext
  implicit val system: ActorSystem = actorSystem

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveChat(chat: Chat): Future[Any] = {
    database.create(chat)
  }

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveGroupChat(chat: Chat, group: Group): Future[Any] = {

    for (user <- group.participants) {
      if (!chat.sender.equalsIgnoreCase(user)) {
        logger.info(s"Notification email scheduled for: $user")
        if (system != null) {
          system.scheduler.scheduleOnce(500.milliseconds) {
            system.actorOf(Props[UserActor]).tell(
              Chat(chat.sender, user, chat.message + s"\nreceived on group ${group.groupName}"), ActorRef.noSender
            )
          }
        }
      }
    }
    database.create(chat)
  }

  /**
   *
   * @param email receiver email who's messages need to be fetched
   * @return sequence of chats received by provided user
   */
  def getMessages(email: String): Future[Seq[Chat]] = {

    database.read().map(chats => {
      var finalList: Seq[Chat] = Seq()
      for (chat <- chats) {
        if (chat.receiver.equals(email)) {
          finalList = finalList :+ chat
        }
      }
      finalList
    })
  }

  /**
   *
   * @param email sender email who's sent messages need to be fetched
   * @return sequence of chats received by provided user
   */
  def getSentMessages(email: String): Future[Seq[Chat]] = {
    database.read().map(chats => {
      var finalList: Seq[Chat] = Seq()
      for (chat <- chats) {
        if (chat.sender.equals(email)) {
          finalList = finalList :+ chat
        }
      }
      finalList
    })
  }

  /**
   *
   * @param groupId of required group
   * @return sequence of chats received by the group
   */
  def getGroupMessages(groupId: String): Future[Seq[Chat]] = {
    database.read().map(chats => {
      var finalList: Seq[Chat] = Seq()
      for (chat <- chats) {
        if (chat.receiver.equals(groupId)) {
          finalList = finalList :+ chat
        }
      }
      finalList
    })
  }

}
