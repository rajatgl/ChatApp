package com.bridgelabz.chat.database

import akka.actor.{ActorRef, Props}
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.Routes.{executor, system}
import com.bridgelabz.chat.models.{Chat, Group, User, UserActor}
import com.bridgelabz.chat.users.EncryptionManager
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
        410
      }
      else {
        if (Routes.system == null) {
          //Endpoints are inactive.
          425
        }
        else {
          val passwordEnc = EncryptionManager.encrypt(user)
          val encryptedUser: User = User(user.email, passwordEnc, user.verificationComplete)
          val future = DatabaseConfig.collection.insertOne(encryptedUser).toFuture()
          Await.result(future, 60.seconds)
          Routes.system.actorOf(Props[UserActor], user.email)

          //"Registration Successful. Please login at: http://localhost:9000/login"
          215
        }
      }
    }
    else {
      //"E-Mail Validation Failed"
      414
    }
  }

  /**
   *
   * @param email to be checked for existence within the database
   * @return boolean result of check operation
   */
  def checkIfExists(email: String): Boolean = {
    val data = Await.result(getUsers, 10.seconds)
    data.foreach(user => if (user.email.equalsIgnoreCase(email)) return true)
    false
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

    val group: Group = getGroup(chat.receiver)
    if (system != null)
      for (user <- group.participants) {
        if (!chat.sender.equalsIgnoreCase(user))
          system.scheduler.scheduleOnce(500 milliseconds) {
            system.actorOf(Props[UserActor]).tell(Chat(chat.sender, user, chat.message + s"\nreceived on group ${group.groupName}"), ActorRef.noSender)
          }
      }

    val future = DatabaseConfig.collectionForGroupChat.insertOne(chat).toFuture()
    Await.result(future, 10.seconds)
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

    val group = getGroup(groupId)
    var newGroup = Group(group.groupId, group.groupName, group.admin, group.participants)
    if (group != null && users != null) {
      var participantsArray = newGroup.participants
      for (user <- users) {
        if (doesAccountExist(user) && !group.participants.contains(user)) {
          participantsArray = participantsArray :+ user
        }
      }

      newGroup = Group(group.groupId, group.groupName, group.admin, participantsArray)

      if (newGroup.participants != null && newGroup.participants.nonEmpty)
        updateGroup(newGroup)
    }
  }

  /**
   *
   * @param groupId associated with required group instance
   * @return group instance
   */
  def getGroup(groupId: String): Group = {
    val groupFuture = DatabaseConfig.collectionForGroup.find(equal("groupId", groupId)).toFuture()
    val group = Await.result(groupFuture, 60.seconds)

    if (group.nonEmpty)
      group.head
    else
      null
  }
}
