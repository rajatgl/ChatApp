package com.bridgelabz.chat.database.managers

import java.util.Date

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.constants.Constants.emailRegex
import com.bridgelabz.chat.database.interfaces.ICrud
import com.bridgelabz.chat.models.User
import com.bridgelabz.chat.users.EncryptionManager
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

class UserManager(database: ICrud[User],
                  executorContext: ExecutionContext = Routes.executor,
                  actorSystem: ActorSystem = Routes.system) {

  private val logger = Logger("UserManager")
  implicit val executor: ExecutionContext = executorContext
  implicit val system: ActorSystem = actorSystem

  /**
   *
   * @param user to be inserted into the database
   * @return status message of the insertion operation
   */
  def saveUser(user: User): Future[(Int, Future[Any])] = {
    if (user.email.matches(emailRegex)) {
      val ifUserExists: Future[Boolean] = checkIfExists(user.email)
      ifUserExists.map(ifUserExists => {
        if (ifUserExists) {
          //A User with the same E-Mail already exists.
          (StatusCodes.CONFLICT.intValue(), Future.failed(new Exception(s"Email already exists: ${user.email}")))
        }
        else {
          val passwordEnc = EncryptionManager.encrypt(user)
          val encryptedUser: User = User(user.email, passwordEnc, user.verificationComplete)
          val future = database.create(encryptedUser)
          logger.info(s"New user is inserted into db")
          //"Registration Successful. Please login at: http://localhost:9000/login"
          (StatusCodes.OK.intValue(), future)
        }
      })
    }
    else {
      //"E-Mail Validation Failed"
      logger.error("Bad pattern in email")
      Future((StatusCodes.BAD_REQUEST.intValue(), Future.failed(new Exception("Bad pattern in email"))))
    }
  }

  /**
   *
   * @param user instance to be logged in
   * @return status message of login operation
   */
  def userLogin(user: User): Future[Int] = {
    getUsers(user.email).map(users => { var returnStatus: Int = StatusCodes.NOT_FOUND.intValue()
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
   * @param email to check if already registered
   * @return boolean result of check operation
   */
  def doesAccountExist(email: String): Future[Boolean] = {
    val dbFuture = getUsers(email)
    dbFuture.map(users => users.nonEmpty)
  }

  /**
   *
   * @param email to be checked for existence within the database
   * @return boolean result of check operation
   */
  def checkIfExists(email: String): Future[Boolean] = {
    val users = getUsers
    users.map(seq => {
      var userExists: Boolean = false
      if (seq.nonEmpty) {
        seq.foreach(user => if (user.email.equalsIgnoreCase(email)) userExists = true)
      }
      userExists
    })
  }

  /**
   *
   * @return all user instances in the database
   */
  def getUsers: Future[Seq[User]] = {
    database.read()
  }

  /**
   *
   * @param email to filter out the users associated with the given email
   * @return user instances in the database associated with the given email
   */
  def getUsers(email: String): Future[Seq[User]] = {

    database.read().map(users => {
      var finalList: Seq[User] = Seq()
      for(user <- users){
        if(user.email.equals(email)) {
          finalList = finalList :+ user
        }
      }
      finalList
    })
  }

  /**
   *
   * @param email whos verificationComplete param needs to be updated
   * @return Updates isVerificationComplete param of User case class
   */
  def verifyEmail(email: String): Future[Boolean] = {

    val user = getUsers(email)
    user.map(users => {
      if(users.nonEmpty){

        val newUser = User(users.head.email, users.head.password, verificationComplete = true)
        database.update(email, newUser, "email")
        true
      }
      else{
        false
      }
    })
  }
}
