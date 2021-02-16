package com.bridgelabz.chattest

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{Chat, OutputMessage, User}
import com.bridgelabz.chat.users.{EncryptionManager, UserManager}
import com.bridgelabz.chat.utils.Utilities.tryAwait
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class FunctionsTest extends AnyFlatSpec {


  implicit val system: ActorSystem = ActorSystem("Chat-App-Test")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  private val databaseUtils = new DatabaseUtils(executor, system)

  //Database Utils Functions
  "Save User" should "return BAD_REQUEST status code if the email has bad pattern" in {
    databaseUtils.saveUser(TestVariables.user()).onComplete{
      case Success(value) => if(value._1 != StatusCodes.BAD_REQUEST.intValue()) throw new Exception
      case Failure(exception) => throw new Exception
    }
  }

  "Save User" should "return CONFLICT status code if the email already exists" in {
    databaseUtils.saveUser(TestVariables.user()).onComplete{
      case Success(value) => if(value._1 != StatusCodes.CONFLICT.intValue()) throw new Exception
      case Failure(exception) => throw new Exception
    }
  }

  "Save User" should "return OK status code if the user was added" in {
    databaseUtils.saveUser(TestVariables.user()).onComplete{
      case Success(value) => if(value._1 != StatusCodes.OK.intValue()) throw new Exception
      case Failure(exception) => throw new Exception
    }
  }

  "Check If Exists" should "return true if the email already exists" in {
    databaseUtils.checkIfExists("test@gmail.com")
  }

  "Check If Exists" should "return false if the email does not exist" in {
    databaseUtils.checkIfExists("testingREMOVE2@gmail.com")
  }

  behavior of "Get Users"
  it should "return a list of users from the database" in {
    val list = Await.result(databaseUtils.getUsers, 60.seconds)
    list.isInstanceOf[Seq[User]]
  }

  behavior of "Get Users with an email"
  it should "return a list of users from the database" in {
    val list = Await.result(databaseUtils.getUsers("testingREMOVE@gmail.com"), 60.seconds)
    list.isInstanceOf[Seq[User]]
  }

  behavior of "Verify Email"
  it should "return an update result of the operation" in {
    val updateResult = databaseUtils.verifyEmail("testingREMOVE@gmail.com")
    updateResult.onComplete {
      case Success(value) => true
      case Failure(exception) => false
    }
  }

  "Does Account Exists" should "return false if account does not exist" in {
    databaseUtils.doesAccountExist("test@gmail.com").onComplete{
      case Success(value) => if(value) throw new Exception
      case Failure(exception) => throw new Exception
    }
  }


  "Get Group" should "return exception for bad group ID" in {
    databaseUtils.getGroup(TestVariables.groupId()).onComplete {
      case Success(_) => throw new Exception("Test case expects future to fail")
      case Failure(exception) => true
    }
  }

  "Get Messages" should "return a list of chats" in {
    databaseUtils.getMessages("test@gmail.com").onComplete {
      case Success(_) => true
      case Failure(exception) => throw exception
    }
  }

  "Get Group Messages" should "return a list of chats" in {
    databaseUtils.getGroupMessages("test").onComplete {
      case Success(_) => true
      case Failure(exception) => throw exception
    }
  }

  "Save Chat" should "return future of complete" in {
    databaseUtils.saveChat(TestVariables.chat()).onComplete {
      case Success(_) => true
      case Failure(exception) => throw exception
    }
  }

  "Save Group" should "return optional complete" in {
    databaseUtils.saveGroup(TestVariables.group()).onComplete {
      case Success(value) => true
      case Failure(exception) => throw exception
    }
  }

  "Save Group Chat" should "return complete when group exists" in {

    databaseUtils.saveGroupChat(TestVariables.chat(receiver = "randomREMOVE")).onComplete {
      case Success(value) => true
      case Failure(exception) => throw exception
    }
  }

  "Save Group Chat" should "return failed future when group is not created" in {

    databaseUtils.saveGroupChat(TestVariables.chat(receiver = "random")).onComplete {
      case Success(value) => throw new Exception("Test case expects future to fail")
      case Failure(exception) => true
    }
  }

  "Add Participants" should "execute without exceptions" in {
    databaseUtils.addParticipants("randomREMOVE", Seq("randomUser"))
  }

  "Get Sent Messages" should "return a sequence of chats" in {
    databaseUtils.getSentMessages("test@gmail.com").onComplete {
      case Success(value) => true
      case Failure(exception) => throw exception
    }
  }

  "Create New User" should "return OK status code if the user was added successfully" in {
    (new UserManager(executor, system)).createNewUser(TestVariables.user("testingREMOVE3@gmail.com")).onComplete{
      case Success(value) => if(value._1 != StatusCodes.OK.intValue()) throw new Exception
      case Failure(exception) => throw exception
    }
  }

  "Send Verification Email" should "return a output message if email was sent" in {
    try {
      (new UserManager).sendVerificationEmail(TestVariables.user("test@gmail.com")).isInstanceOf[OutputMessage]
    }
    catch {
      case _: NullPointerException => true
      case _: Throwable => false
    }
  }

  "Send Email" should "return a output message if email was sent" in {
    try {
      (new UserManager).sendEmail("test@gmail.com", "Hello")
      true
    }
    catch {
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

  //Encryption Manager
  "Encrypt" should "return an encrypted string for a user object" in {
    !EncryptionManager.encrypt(TestVariables.user()).isEmpty
  }

  "Verify" should "handle null user cases and return false" in {
    //noinspection ScalaStyle
    !EncryptionManager.verify(null, "hello")
  }

  //Utilities
  "Try Await" should "fail if future does not return in time" in {

    val delay: Int = 100
    val future: Future[Int] = Future {
      Thread.sleep(delay); delay
    }

    tryAwait(future, 10.milliseconds).isEmpty
  }
}
