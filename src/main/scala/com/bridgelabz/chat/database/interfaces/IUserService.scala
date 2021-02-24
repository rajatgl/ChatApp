package com.bridgelabz.chat.database.interfaces

import com.bridgelabz.chat.models.User

import scala.concurrent.Future

trait IUserService {

  def saveUser(user: User): Future[Any]
  def getUsers: Future[Seq[User]]
}
