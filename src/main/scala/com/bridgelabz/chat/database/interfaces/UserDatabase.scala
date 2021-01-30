package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.User

import scala.concurrent.Future

trait UserDatabase {

  def saveUser(user: User): Int
  def getUsers: Future[Seq[User]]
}
