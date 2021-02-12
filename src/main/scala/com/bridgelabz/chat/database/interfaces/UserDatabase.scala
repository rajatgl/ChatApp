package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.User
import org.mongodb.scala.Completed

import scala.concurrent.Future

trait UserDatabase {

  def saveUser(user: User): (Int, Future[Completed])
  def getUsers: Future[Seq[User]]
}
