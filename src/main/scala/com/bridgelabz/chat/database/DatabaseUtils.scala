package com.bridgelabz.chat.database

import akka.actor.{ActorRef, Props}
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.Routes.{executor, system}
import com.bridgelabz.chat.models.{Chat, Group, User, UserActor}
import com.bridgelabz.chat.users.EncryptionManager
import com.typesafe.scalalogging.Logger
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

/**
 * Created on 1/8/2021.
 * Class: DatabaseUtils.scala
 * Author: Rajat G.L.
 */
object DatabaseUtils {
  private val logger = Logger("DatabaseUtils")

  /**
   *
   * @param user to be inserted into the database
   * @return status message of the insertion operation
   */
  def saveUser(user: User): Int = {
    val emailRegex = "^[a-zA-Z0-9+-._]+@[a-zA-Z0-9.-]+$"
    if (user.email.matches(emailRegex)) {
      val ifUserExists: Boolean = checkIfExists(user.email)
      if (ifUserExists) {
        //A User with the same E-Mail already exists.
        logger.debug(s"${user.email} conflicted")
        StatusCodes.CONFLICT.intValue()
      }
      else {
        if (Routes.system == null) {
          //Endpoints are inactive.
          logger.debug("Endpoints inactive")
          StatusCodes.INTERNAL_SERVER_ERROR.intValue()
        }
        else {
          val passwordEnc = EncryptionManager.encrypt(user)
          val encryptedUser: User = User(user.email, passwordEnc, user.verificationComplete)
          val future = DatabaseConfig.collection.insertOne(encryptedUser).toFuture()
          Await.result(future, 60.seconds)
          logger.info(s"${user.email} is inserted into db")
          Routes.system.actorOf(Props[UserActor], user.email)

          //"Registration Successful. Please login at: http://localhost:9000/login"
          StatusCodes.OK.intValue()
        }
      }
    }
    else {
      //"E-Mail Validation Failed"
      StatusCodes.BAD_REQUEST.intValue()
    }
  }

  /**
   *
   * @param email to be checked for existence within the database
   * @return boolean result of check operation
   */
  def checkIfExists(email: String): Boolean = {
    val data = Await.result(getUsers, 10.seconds)
    var userExists: Boolean = false
    data.foreach(user => if (user.email.equalsIgnoreCase(email)) userExists = true)
    userExists
  }

  /**
   *
   * @return all user instances in the database
   */
  def getUsers: Future[Seq[User]] = {
    DatabaseConfig.collection.find().toFuture()
  }

  /**
   *
   * @param email to filter out the users associated with the given email
   * @return user instances in the database associated with the given email
   */
  def getUsers(email: String): Future[Seq[User]] = {
    DatabaseConfig.collection.find(equal("email", email)).toFuture()
  }

  /**
   *
   * @param email whos verificationComplete param needs to be updated
   * @return Updates isVerificationComplete param of User case class
   */
  def verifyEmail(email: String): Future[result.UpdateResult] = {
    DatabaseConfig.collection.updateOne(equal("email", email), set("verificationComplete", true)).toFuture()
  }

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveChat(chat: Chat): Unit = {
    val future = DatabaseConfig.collectionForChat.insertOne(chat).toFuture()
    Await.result(future, 10.seconds)
  }

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveGroupChat(chat: Chat): Unit = {

    val group = getGroup(chat.receiver)
    if (group.isDefined) {
      if (system != null) {
        for (user <- group.get.participants) {
          if (!chat.sender.equalsIgnoreCase(user)) {
            logger.info(s"Notification email scheduled for: ${user}")
            system.scheduler.scheduleOnce(500 milliseconds) {
              system.actorOf(Props[UserActor]).tell(Chat(chat.sender, user, chat.message + s"\nreceived on group ${group.get.groupName}"), ActorRef.noSender)
            }
          }
        }
      } else {
        logger.error("Endpoints inactive")
      }

      val future = DatabaseConfig.collectionForGroupChat.insertOne(chat).toFuture()
      Await.result(future, 10.seconds)
    }
  }

  /**
   *
   * @param email to check if already registered
   * @return boolean result of check operation
   */
  def doesAccountExist(email: String): Boolean = {
    val dbFuture = DatabaseConfig.collection.find(equal("email", email)).toFuture()
    val users = Await.result(dbFuture, 10.seconds)
    users.nonEmpty
  }

  /**
   *
   * @param email    to check if user is successfully logged in
   * @param password to be verified
   * @return boolean result of this check operation
   */
  def isSuccessfulLogin(email: String, password: String): Boolean = {
    val dbFuture = DatabaseConfig.collection.find(equal("email", email)).toFuture()
    val user = Await.result(dbFuture, 10.seconds).head
    EncryptionManager.verify(user, password)
  }

  /**
   *
   * @param group instance to be saved into database
   */
  def saveGroup(group: Group): Unit = {
    val groupFuture = DatabaseConfig.collectionForGroup.insertOne(group).toFuture()
    Await.result(groupFuture, 60.seconds)
  }

  /**
   *
   * @param group instance to be updated in the database
   */
  def updateGroup(group: Group): Unit = {
    val fut = DatabaseConfig.collectionForGroup.updateOne(equal("groupId", group.groupId), set("participants", group.participants)).toFuture()
    Await.result(fut, 60.seconds)
  }

  /**
   *
   * @param groupId of group being referred
   * @param users   Seq of users to be added as participant
   */
  def addParticipants(groupId: String, users: Seq[String]): Unit = {

    val groupOp = getGroup(groupId)

    if (groupOp.isDefined && users != null) {
      val group = groupOp.get
      var newGroup = Group(group.groupId, group.groupName, group.admin, group.participants)
      var participantsArray = newGroup.participants
      for (user <- users) {
        if (doesAccountExist(user) && !group.participants.contains(user)) {
          logger.info(s"${user} added to the group: ${group.groupName}")
          participantsArray = participantsArray :+ user
        }
        else {
          logger.debug(s"${user} not added to the group: ${group.groupName}")
        }
      }

      newGroup = Group(group.groupId, group.groupName, group.admin, participantsArray)

      if (newGroup.participants != null && newGroup.participants.nonEmpty) {
        updateGroup(newGroup)
      } else {
        logger.debug(s"Group:${newGroup.groupName} not updated.")
      }
    }
  }

  /**
   *
   * @param groupId associated with required group instance
   * @return group instance
   */
  def getGroup(groupId: String): Option[Group] = {
    val groupFuture = DatabaseConfig.collectionForGroup.find(equal("groupId", groupId)).toFuture()
    val group = Await.result(groupFuture, 60.seconds)

    if (group.nonEmpty) {
      Option(group.head)
    } else {
      None
    }
  }

  /**
   *
   * @param email receiver email who's messages need to be fetched
   * @return sequence of chats received by provided user
   */
  def getMessages(email: String): Seq[Chat] = {
    val chatFuture = DatabaseConfig.collectionForChat.find(equal("receiver", email)).toFuture()
    Await.result(chatFuture, 60.seconds)
  }

  /**
   *
   * @param groupId of required group
   * @return sequence of chats received by the group
   */
  def getGroupMessages(groupId: String): Seq[Chat] = {
    val groupChatFuture = DatabaseConfig.collectionForGroupChat.find(equal("receiver", groupId)).toFuture()
    Await.result(groupChatFuture, 60.seconds)
  }

}
