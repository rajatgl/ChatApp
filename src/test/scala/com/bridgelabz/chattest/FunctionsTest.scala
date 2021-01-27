package com.bridgelabz.chattest

import akka.http.javadsl.model.StatusCodes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.models.User
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


}
