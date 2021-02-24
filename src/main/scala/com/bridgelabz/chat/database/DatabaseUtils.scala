package com.bridgelabz.chat.database

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.constants.Constants.emailRegex
import com.bridgelabz.chat.database.interfaces.{IChatService, IGroupChatService, IGroupService, IUserService}
import com.bridgelabz.chat.models.{Chat, Group, User, UserActor}
import com.bridgelabz.chat.users.EncryptionManager
import com.typesafe.scalalogging.Logger
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.{Completed, result}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Created on 1/8/2021.
 * Class: DatabaseUtils.scala
 * Author: Rajat G.L.
 */
class DatabaseUtils(executorContext: ExecutionContext = Routes.executor,
                    actorSystem: ActorSystem = Routes.system,
                    uri: String = s"mongodb://${Constants.mongoHost}:${Constants.mongoPort}")
  extends DatabaseConfig(uri)
  with IChatService
  with IUserService
  with IGroupService
  with IGroupChatService {

  private val logger = Logger("DatabaseUtils")
  implicit val executor: ExecutionContext = executorContext
  implicit val system: ActorSystem = actorSystem

  /**
   *
   * @param user to be inserted into the database
   * @return status message of the insertion operation
   */
  def saveUser(user: User): Future[(Int, Future[Completed])] = {
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
          val future = collection.insertOne(encryptedUser).toFuture()
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
    collection.find().toFuture()
  }

  /**
   *
   * @param email to filter out the users associated with the given email
   * @return user instances in the database associated with the given email
   */
  def getUsers(email: String): Future[Seq[User]] = {
    collection.find(equal("email", email)).toFuture()
  }

  /**
   *
   * @param email whos verificationComplete param needs to be updated
   * @return Updates isVerificationComplete param of User case class
   */
  def verifyEmail(email: String): Future[result.UpdateResult] = {
    collection.updateOne(equal("email", email), set("verificationComplete", true)).toFuture()
  }

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveChat(chat: Chat): Future[Completed] = {
    collectionForChat.insertOne(chat).toFuture()
  }

  /**
   *
   * @param chat instance to be saved into database
   */
  def saveGroupChat(chat: Chat): Future[Future[Completed]] = {

    val group = getGroup(chat.receiver)
    implicit val executor: ExecutionContext = executorContext

    group.map(groupSeq => {
      if(groupSeq.nonEmpty){
        for (user <- groupSeq.head.participants) {
          if (!chat.sender.equalsIgnoreCase(user)) {
            logger.info(s"Notification email scheduled for: $user")
            if (system != null) {
              system.scheduler.scheduleOnce(500.milliseconds) {
                system.actorOf(Props[UserActor]).tell(
                  Chat(chat.sender, user, chat.message + s"\nreceived on group ${groupSeq.head.groupName}"), ActorRef.noSender
                )
              }
            }
          }
        }
        collectionForGroupChat.insertOne(chat).toFuture()
      }
      else{
        Future.failed[Completed](new Exception())
      }
    })
  }

  /**
   *
   * @param email to check if already registered
   * @return boolean result of check operation
   */
  def doesAccountExist(email: String): Future[Boolean] = {
    val dbFuture = collection.find(equal("email", email)).toFuture()
    dbFuture.map(users => users.nonEmpty)
  }

  /**
   *
   * @param group instance to be saved into database
   */
  def saveGroup(group: Group): Future[Completed] = {
    collectionForGroup.insertOne(group).toFuture()
  }

  /**
   *
   * @param group instance to be updated in the database
   */
  def updateGroup(group: Group): Future[result.UpdateResult] = {
    collectionForGroup.updateOne(equal("groupId", group.groupId), set("participants", group.participants)).toFuture()
  }

  /**
   *
   * @param groupId of group being referred
   * @param users   Seq of users to be added as participant
   */
  def addParticipants(groupId: String, users: Seq[String]): Unit = {

    implicit val executor: ExecutionContext = executorContext
    val groupOp = getGroup(groupId)

    groupOp andThen {
      case Success(seqGroup) =>
        val group = seqGroup.head
        var newGroup = Group(group.groupId, group.groupName, group.admin, group.participants)
        var participantsArray = newGroup.participants
        for (user <- users) {
          doesAccountExist(user) andThen {
            case Success(value) =>
              if (value && !group.participants.contains(user)) {
                logger.info(s"$user added to the group: ${group.groupName}")
                participantsArray = participantsArray :+ user
              }
              else {
                logger.debug(s"$user not added to the group: ${group.groupName}")
              }
              newGroup = Group(group.groupId, group.groupName, group.admin, participantsArray)
              if (newGroup.participants != null && newGroup.participants.nonEmpty) {
                updateGroup(newGroup)
              } else {
                logger.debug(s"Group:${newGroup.groupName} not updated.")
              }
            case Failure(exception) => logger.error(exception.getMessage)
          }
        }
      case Failure(exception) =>
        logger.error(exception.getMessage)
    }
  }

  /**
   *
   * @param groupId associated with required group instance
   * @return group instance
   */
  def getGroup(groupId: String): Future[Seq[Group]] = {
    collectionForGroup.find(equal("groupId", groupId)).toFuture()
  }

  /**
   *
   * @param email receiver email who's messages need to be fetched
   * @return sequence of chats received by provided user
   */
  def getMessages(email: String): Future[Seq[Chat]] = {
    collectionForChat.find(equal("receiver", email)).toFuture()
  }

  /**
   *
   * @param email sender email who's sent messages need to be fetched
   * @return sequence of chats received by provided user
   */
  def getSentMessages(email: String): Future[Seq[Chat]] = {
    collectionForChat.find(equal("sender", email)).toFuture()
  }

  /**
   *
   * @param groupId of required group
   * @return sequence of chats received by the group
   */
  def getGroupMessages(groupId: String): Future[Seq[Chat]] = {
    collectionForGroupChat.find(equal("receiver", groupId)).toFuture()
  }
}
