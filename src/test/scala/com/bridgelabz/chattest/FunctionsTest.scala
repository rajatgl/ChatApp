package com.bridgelabz.chattest

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{Chat, OutputMessage, User}
import com.bridgelabz.chat.users.UserManager
import com.bridgelabz.chat.utils.Utilities.tryAwait
import org.mongodb.scala.result
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class FunctionsTest extends AnyFlatSpec {

  //Database Utils Functions
  "Save User" should "return BAD_REQUEST status code if the email has bad pattern" in {
    DatabaseUtils.saveUser(TestVariables.user()) == StatusCodes.BAD_REQUEST.intValue()
  }

  "Save User" should "return CONFLICT status code if the email already exists" in {
    DatabaseUtils.saveUser(TestVariables.user("test@gmail.com")) == StatusCodes.CONFLICT.intValue()
  }

  "Save User" should "return OK status code if the user was added" in {
    DatabaseUtils.saveUser(TestVariables.user("testingREMOVE@gmail.com")) == StatusCodes.OK.intValue()
  }

  "Check If Exists" should "return true if the email already exists" in {
    DatabaseUtils.checkIfExists("test@gmail.com")
  }

  "Check If Exists" should "return false if the email does not exist" in {
    DatabaseUtils.checkIfExists("testingREMOVE2@gmail.com")
  }

  behavior of "Get Users"
  it should "return a list of users from the database" in {
      val list = Await.result(DatabaseUtils.getUsers, 60.seconds)
      list.isInstanceOf[Seq[User]]
  }

  behavior of "Get Users with an email"
  it should "return a list of users from the database" in {
    val list = Await.result(DatabaseUtils.getUsers("testingREMOVE@gmail.com"), 60.seconds)
    list.isInstanceOf[Seq[User]]
  }

  behavior of "Verify Email"
  it should "return an update result of the operation" in {
    val updateResult = Await.result(DatabaseUtils.verifyEmail("testingREMOVE@gmail.com"), 60.seconds)
    updateResult.isInstanceOf[result.UpdateResult]
  }

  "Does Account Exists" should "return false if account does not exist" in {
    !DatabaseUtils.doesAccountExist("test@gmail.com")
  }

  "Is Successful Login" should "return false for wrong username-password combination" in {
    !DatabaseUtils.isSuccessfulLogin(TestVariables.user().email, TestVariables.user().password)
  }

  "Get Group" should "return None for bad group ID" in {
    DatabaseUtils.getGroup(TestVariables.groupId()).isEmpty
  }

  "Get Messages" should "return a list of chats" in {
    DatabaseUtils.getMessages("test@gmail.com").isInstanceOf[Seq[Chat]]
  }

  "Get Group Messages" should "return a list of chats" in {
    DatabaseUtils.getGroupMessages("test").isInstanceOf[Seq[Chat]]
  }

  //User Manager Tests
  "User Login" should "return NOT_FOUND status code if the account does not exist" in {
    (new UserManager).userLogin(TestVariables.user("testingREMOVE3@gmail.com")) == StatusCodes.NOT_FOUND.intValue()
  }

  "User Login" should "return UNAUTHORIZED status code if the email is not verified" in {
    (new UserManager).userLogin(TestVariables.user("testingREMOVE2@gmail.com")) == StatusCodes.UNAUTHORIZED.intValue()
  }

  "User Login" should "return OK status code if the user can login in successfully" in {
    (new UserManager).userLogin(TestVariables.user("testingREMOVE@gmail.com")) == StatusCodes.OK.intValue()
  }

  "Create New User" should "return OK status code if the user was added successfully" in {
    (new UserManager).createNewUser(TestVariables.user("testingREMOVE3@gmail.com")) == StatusCodes.OK.intValue()
  }

  "Send Verification Email" should "return a output message if email was sent" in {
    try {
      (new UserManager).sendVerificationEmail(TestVariables.user("test@gmail.com")).isInstanceOf[OutputMessage]
    }
    catch{
      case nullEx: NullPointerException => true
      case _ => false
    }
  }

  "Send Email" should "return a output message if email was sent" in {
    try {
      (new UserManager).sendEmail("test@gmail.com", "Hello")
      true
    }
    catch{
      case _: Throwable => false
    }
  }

  //Token Manager
  "Generate Token" should "return a string for any email" in {
    !TokenManager.generateToken("test@gmail.com").isEmpty
  }

  "Generate Login ID" should "return a string for any email" in {
    !TokenManager.generateLoginId(TestVariables.user("test@gmail.com")).isEmpty
  }

  "Is Token Expired" should "return false for valid token" in {
    val token = TokenManager.generateLoginId(TestVariables.user())
    TokenManager.isTokenExpired(token)
  }

  "Get Claims" should "return a Map[String, String] for valid jwt token" in {
    val token = TokenManager.generateToken("test@gmail.com")
    TokenManager.getClaims(token).isInstanceOf[Map[String, String]]
  }

  //Utilities
  "Try Await" should "fail if future does not return in time" in {

    implicit val system: ActorSystem = ActorSystem("FutureTest")
    implicit val executor: ExecutionContextExecutor = system.dispatcher

    val delay: Int = 100
    val future: Future[Int] = Future{Thread.sleep(delay); delay}

    tryAwait(future, 10.milliseconds).isEmpty
  }
}
