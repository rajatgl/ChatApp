package com.bridgelabz.chattest

import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.models.{Chat, User}
import org.mongodb.scala.result
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.Await

class FunctionsTest extends AnyFlatSpec with MockFactory {

  //Database Utils Functions
  "Save User" should "return BAD_REQUEST status code if the email has bad pattern" in {
    DatabaseUtils.saveUser(TestVariables.user()) == (StatusCodes.BAD_REQUEST.intValue())
  }

  "Save User" should "return CONFLICT status code if the email already exists" in {
    DatabaseUtils.saveUser(TestVariables.user("test@gmail.com")) == (StatusCodes.CONFLICT.intValue())
  }

  "Check If Exists" should "return true if the email already exists" in {
    DatabaseUtils.checkIfExists("test@gmail.com")
  }

  behavior of "Get Users"
  it should "return a list of users from the database" in {
    val list = Await.result(DatabaseUtils.getUsers, 60.seconds)
    list.isInstanceOf[Seq[User]]
  }

  behavior of "Get Users with an email"
  it should "return a list of users from the database" in {
    val list = Await.result(DatabaseUtils.getUsers("test@gmail.com"), 60.seconds)
    list.isInstanceOf[Seq[User]]
  }

  behavior of "Verify Email"
  it should "return an update result of the operation" in {
    val updateResult = Await.result(DatabaseUtils.verifyEmail("test@gmail.com"), 60.seconds)
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
}
