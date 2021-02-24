package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Group

import scala.concurrent.Future

trait IGroupService {

  def saveGroup(group: Group): Future[Any]

  def updateGroup(group: Group): Future[Any]

  def getGroup(groupId: String): Future[Seq[Group]]
}
