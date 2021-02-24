package com.bridgelabz.chat.users

import java.util.Date

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.models.User
import com.typesafe.scalalogging.Logger
import org.mongodb.scala.Completed

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created on 1/8/2021.
 * Class: UserManager.scala
 * Author: Rajat G.L.
 */
class UserManager(executionContext: ExecutionContext = Routes.executor, actorSystem: ActorSystem = Routes.system) {

  private val logger = Logger("UserManager")

  implicit val executor: ExecutionContext = executionContext
  private val databaseUtils: DatabaseUtils = new DatabaseUtils(executionContext, actorSystem)
  /**
   *
   * @param user instance to be logged in
   * @return status message of login operation
   */
  def userLogin(user: User): Future[Int] = {
    databaseUtils.getUsers(user.email).map(users => { var returnStatus: Int = StatusCodes.NOT_FOUND.intValue()
        users.foreach(mainUser =>
          if (EncryptionManager.verify(mainUser, user.password)) {
            if (!mainUser.verificationComplete) {
              returnStatus = StatusCodes.UNAUTHORIZED.intValue() //user is not verified
            } else {

              logger.info(s"User login successful at ${new Date().getTime}")
              returnStatus = StatusCodes.OK.intValue() // user is verified and login successful
            }
          }
        )
        logger.info(s"User login failed- invalid email/password at ${new Date().getTime}")
        returnStatus
     })
  }

  /**
   *
   * @param user the object that is needed to be inserted into the database
   * @return status of the above insertion operation (2xx return preferable)
   */
  def createNewUser(user: User): Future[(Int, Future[Completed])] = {
    databaseUtils.saveUser(user)
  }
}
