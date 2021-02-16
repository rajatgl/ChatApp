package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Group
import org.mongodb.scala.{Completed, result}

import scala.concurrent.Future

trait IGroupService {

  def saveGroup(group: Group): Future[Completed]
  def updateGroup(group: Group): Future[result.UpdateResult]
  def getGroup(groupId: String): Future[Seq[Group]]
}
