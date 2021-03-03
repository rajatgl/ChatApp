package com.bridgelabz.chat.database.managers

import akka.actor.ActorSystem
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.database.interfaces.ICrud
import com.bridgelabz.chat.models.Group
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GroupManager(database: ICrud[Group],
                   executorContext: ExecutionContext = Routes.executor,
                   actorSystem: ActorSystem = Routes.system) {

  private val logger = Logger("GroupManager")
  implicit val executor: ExecutionContext = executorContext
  implicit val system: ActorSystem = actorSystem

  /**
   *
   * @param group instance to be saved into database
   */
  def saveGroup(group: Group): Future[Any] = {
    database.create(group)
  }

  /**
   *
   * @param groupId associated with required group instance
   * @return group instance
   */
  def getGroup(groupId: String): Future[Seq[Group]] = {
    database.read().map(groups => {
      var finalList: Seq[Group] = Seq()
      for (group <- groups) {
        if (group.groupId.equals(groupId)) {
          finalList = finalList :+ group
        }
      }
      finalList
    })
  }

  /**
   *
   * @param groupId of group being referred
   * @param users   Seq of users to be added as participant
   */
  def addParticipants(groupId: String, users: Seq[String]): Unit = {

    implicit val executor: ExecutionContext = executorContext
    val groupOp = getGroup(groupId)
    groupOp andThen {
      case Success(seqGroup) =>
        val group = seqGroup.head
        var newGroup = Group(group.groupId, group.groupName, group.admin, group.participants)
        var participantsArray = newGroup.participants
        for (user <- users) {
          if (!group.participants.contains(user)) {
            logger.info(s"$user added to the group: ${group.groupName}")
            participantsArray = participantsArray :+ user
          }
          else {
            logger.debug(s"$user not added to the group: ${group.groupName}")
          }
          newGroup = Group(group.groupId, group.groupName, group.admin, participantsArray)
          if (newGroup.participants != null && newGroup.participants.nonEmpty) {
            database.update(newGroup.groupId, newGroup, "groupId")
          } else {
            logger.debug(s"Group:${newGroup.groupName} not updated.")
          }
        }
      case Failure(exception) =>
        logger.error(exception.getMessage)
    }
  }
}
