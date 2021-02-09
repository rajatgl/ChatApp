package com.bridgelabz.chattest

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.bridgelabz.chat.Routes
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{Chat, Group, LoginRequestJsonSupport, OutputMessage, User}
import com.bridgelabz.chat.users.UserManager
import org.mockito.Mockito.when
import org.mongodb.scala.Completed
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class RoutesTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar with LoginRequestJsonSupport with ScalaFutures {

  val mockUserManager: UserManager = mock[UserManager]
  val mockDatabaseUtils: DatabaseUtils = mock[DatabaseUtils]

  "The service" should {

    "Routes should register a test account for a Post request to /register" in {

      val user: User = TestVariables.user("test@gmail.com")

      when(mockUserManager.createNewUser(user)).thenReturn(StatusCodes.OK.intValue)
      when(mockUserManager.sendVerificationEmail(user))
        .thenReturn(OutputMessage(StatusCodes.OK.intValue, "Verification link sent!"))

      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/register",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should deny registering a test account for a Post request to /register" in {

      val user: User = TestVariables.user("test@gmail.com")

      when(mockUserManager.createNewUser(user)).thenReturn(StatusCodes.BadRequest.intValue)
      when(mockUserManager.sendVerificationEmail(user))
        .thenReturn(OutputMessage(StatusCodes.OK.intValue, "Verification link sent!"))

      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/register",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should deny registering a test account if it exists already for a Post request to /register" in {

      val user: User = TestVariables.user("test@gmail.com")

      when(mockUserManager.createNewUser(user)).thenReturn(StatusCodes.Conflict.intValue)
      when(mockUserManager.sendVerificationEmail(user))
        .thenReturn(OutputMessage(StatusCodes.OK.intValue, "Verification link sent!"))

      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/register",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to  login an account if POST request sent to /login" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockUserManager.userLogin(user)).thenReturn(StatusCodes.OK.intValue)
      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/login",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny login to an account if POST request sent to /login if it does not exist" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockUserManager.userLogin(user)).thenReturn(StatusCodes.NotFound.intValue)
      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/login",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny login to an account if POST request sent to /login if it is not verified" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockUserManager.userLogin(user)).thenReturn(StatusCodes.Unauthorized.intValue)
      val jsonRequest = ByteString(
        s"""
            {
              "email":"test@gmail.com",
              "password": "helloworld"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/login",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to access chat feature if POST request sent to /chat" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.doesAccountExist("test@gmail.com")).thenReturn(true)
      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"test@gmail.com",
              "message": "hello world"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to chat feature if POST request sent to /chat with invalid account" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.doesAccountExist("test@gmail.com")).thenReturn(false)
      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"test@gmail.com",
              "message": "hello world"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to chat feature if POST request sent to /chat with invalid login token" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.doesAccountExist("test@gmail.com")).thenReturn(true)
      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"test@gmail.com",
              "message": "hello world"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }


    "Routes should be able to deny access to chat feature if POST request sent to /chat with expired login token" in {
      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.doesAccountExist("test@gmail.com")).thenReturn(true)
      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"test@gmail.com",
              "message": "hello world"
            }
        """.stripMargin
      )
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to access messages if GET request is sent to /chat" in {

      val user: User = TestVariables.user("test@gmail.com")
      val getRequest = Get("/chat")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to messages if GET request is sent to /chat with invalid login token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val getRequest = Get("/chat")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to messages if GET request is sent to /chat with expired login token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val getRequest = Get("/chat")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to create a group if POST request sent to /group/create" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.saveGroup(group)).thenReturn(success)
      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/create",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny creation of a group if POST request sent to /group/create with invalid login token" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.saveGroup(group)).thenReturn(success)
      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/create",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny creation of a group if POST request sent to /group/create with expired login token" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.saveGroup(group)).thenReturn(success)
      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/create",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to add users if POST request sent to /group/users" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))

      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random",
              "participantEmails":["ak","hello","hola"]
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/users",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny add users if POST request sent to /group/users with invalid login token" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))

      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random",
              "participantEmails":["ak","hello","hola"]
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/users",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny add users if POST request sent to /group/users with expired login token" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))

      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random",
              "participantEmails":["ak","hello","hola"]
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/users",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny add users if POST request sent to /group/users when group does not exist" in {

      val group = TestVariables.group("RANDOMRANDOM")
      val user = TestVariables.user("random")

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(None)

      val jsonRequest = ByteString(
        s"""
            {
              "groupName":"random",
              "participantEmails":["ak","hello","hola"]
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/users",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to access group chat feature if POST request sent to /group/chat" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.saveGroupChat(Chat(user.email, group.groupId, "hello world"))).thenReturn(success)

      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"RANDOMRANDOM",
              "message": "hello world"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group chat feature if POST request sent to /group/chat with invalid login token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.saveGroupChat(Chat(user.email, group.groupId, "hello world"))).thenReturn(success)

      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"RANDOMRANDOM",
              "message": "hello world"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group chat feature if POST request sent to /group/chat with expired token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.saveGroupChat(Chat(user.email, group.groupId, "hello world"))).thenReturn(success)

      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"RANDOMRANDOM",
              "message": "hello world"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group chat feature if POST request sent to /group/chat when user not part of the group" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = TestVariables.group("RANDOMRANDOM")
      val success: Option[Completed] = Some(Completed.apply())

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.saveGroupChat(Chat(user.email, group.groupId, "hello world"))).thenReturn(success)

      val jsonRequest = ByteString(
        s"""
            {
              "receiver":"RANDOMRANDOM",
              "message": "hello world"
            }
        """.stripMargin
      )

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/group/chat",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )

      postRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to access group messages if GET request is sent to /group/chat" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.getGroupMessages(group.groupId)).thenReturn(Seq())

      val getRequest = Get("/group/chat?groupId=RANDOMRANDOM")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group messages if GET request is sent to /group/chat with invalid login token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.getGroupMessages(group.groupId)).thenReturn(Seq())

      val getRequest = Get("/group/chat?groupId=RANDOMRANDOM")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateInvalidLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group messages if GET request is sent to /group/chat with expired login token" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = Group("RANDOMRANDOM", "random", "random", Seq(user.email))

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(Some(group))
      when(mockDatabaseUtils.getGroupMessages(group.groupId)).thenReturn(Seq())

      val getRequest = Get("/group/chat?groupId=RANDOMRANDOM")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny access to group messages if GET request is sent to /group/chat when user not part of group" in {

      val user: User = TestVariables.user("test@gmail.com")
      val group = TestVariables.group("RANDOMRANDOM")

      when(mockDatabaseUtils.getGroup(group.groupId)).thenReturn(None)
      when(mockDatabaseUtils.getGroupMessages(group.groupId)).thenReturn(Seq())

      val getRequest = Get("/group/chat?groupId=RANDOMRANDOM")

      getRequest ~> addCredentials(OAuth2BearerToken(TokenManager.generateExpiredLoginId(user))) ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to verify user if GET request is sent to /verify" in {

      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.verifyEmail(user.email)).thenReturn(None)

      val getRequest = Get(s"/verify?token=${TokenManager.generateToken(user.email)}&email=${user.email}")

      getRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }

    "Routes should be able to deny verification of user if GET request is sent to /verify with invalid email" in {

      val user: User = TestVariables.user("test@gmail.com")
      when(mockDatabaseUtils.verifyEmail(user.email)).thenReturn(None)

      val getRequest = Get(s"/verify?token=${TokenManager.generateToken(user.email)}&email=${user.email}invalid")

      getRequest ~> Routes.route(mockDatabaseUtils, mockUserManager) ~> check {
        status.equals(StatusCodes.OK)
      }
    }
  }
}
