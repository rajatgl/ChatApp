package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.Group
import org.mongodb.scala.{Completed, result}

trait GroupDatabase {

  def saveGroup(group: Group): Option[Completed]
  def updateGroup(group: Group): Option[result.UpdateResult]
  def getGroup(groupId: String): Option[Group]
}
