package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.User
import org.mongodb.scala.Completed

import scala.concurrent.Future

trait IUserService {

  def saveUser(user: User): Future[(Int, Future[Completed])]
  def getUsers: Future[Seq[User]]
}
