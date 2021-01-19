package com.bridgelabz.chat

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{_symbol2NR, complete, concat, entity, extractUri, get, handleExceptions, headerValueByName, optionalHeaderValueByName, parameters, path, post, provide}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import authentikat.jwt.JsonWebToken
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.jwt.TokenManager.{getClaims, isTokenExpired, secretKey}
import com.bridgelabz.chat.models.{Chat, Communicate, CommunicateJsonSupport, Group, GroupAddUser, GroupAddUserJsonFormat, GroupJsonFormat, GroupName, GroupNameJsonFormat, LoginMessage, LoginMessageJsonFormat, LoginRequest, LoginRequestJsonSupport, OutputMessage, OutputMessageJsonFormat, User, UserActor, UserJsonSupport}
import com.bridgelabz.chat.users.{EncryptionManager, UserManager}
import com.nimbusds.jose.JWSObject
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object Routes extends App with UserJsonSupport with LoginRequestJsonSupport with CommunicateJsonSupport with OutputMessageJsonFormat with LoginMessageJsonFormat with GroupNameJsonFormat with GroupAddUserJsonFormat with GroupJsonFormat {

  //server configuration variables
  val host = System.getenv("Host")
  val port = System.getenv("Port").toInt
  val logger = Logger("Routes")

  //actor system and execution context for AkkaHTTP server
  implicit val system: ActorSystem = ActorSystem("Chat")
  implicit val executor: ExecutionContext = system.dispatcher

  //catching Null Pointer Exception and other default Exceptions
  val exceptionHandler = ExceptionHandler {
    case nex: NullPointerException =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        logger.error(nex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(HttpResponse(402, entity = "Null value found while parsing the data. Contact the admin."))
      }
    case ex: Exception =>
      extractUri { uri =>
        logger.error(ex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(HttpResponse(408, entity = "Some error occured. Please try again later."))
      }
  }

  /**
   * handles all the get post requests to appropriate path endings
   *
   * @return Route object needed for server for binding
   */
  def route: Route = {
    handleExceptions(exceptionHandler) {
      concat(
        post {
          concat(

            //path for creating the session for a given user (post successful login)
            path("login") {
              entity(Directives.as[LoginRequest]) { request =>
                val user: User = User(request.email, request.password, verificationComplete = false)
                val encryptedUser: User = User(request.email, EncryptionManager.encrypt(user), verificationComplete = true)
                val userLoginStatus: Int = UserManager.userLogin(user)

                if (userLoginStatus == 200) {
                  logger.info("User Login Successful.")
                  complete(LoginMessage(TokenManager.generateLoginId(encryptedUser), 200, "Logged in successfully. Happy to serve you!"))
                }
                else if (userLoginStatus == 404) {
                  logger.error("Account Not Registered.")
                  complete(OutputMessage(404, "Login failed. Your account does not seem to exist. If you did not register yet, head to: http://localhost:9000/register"))
                }
                else {
                  logger.error("Account Verification Incomplete.")
                  complete(OutputMessage(400, "Login failed. Your account is not verified. Head to http://localhost:9000/verify for the same."))
                }
              }
            },
            //creation/ insertion of a new valid user account
            path("register") {
              entity(Directives.as[LoginRequest]) { request =>
                val user: User = User(request.email, request.password, verificationComplete = false)
                val userRegisterStatus: Int = UserManager.createNewUser(user)

                if (userRegisterStatus == 215) {
                  logger.info("Verification Link Sent.")
                  complete(UserManager.sendVerificationEmail(user))
                }
                else if (userRegisterStatus == 414) {
                  logger.error("Invalid Email.")
                  complete(OutputMessage(414, "Bad email, try again with a valid entry."))
                }
                else {
                  logger.error("Email is already registered. Provide a new one.")
                  complete(OutputMessage(409, "User registration failed. E-mail is already registered."))
                }
              }
            },

            //chatting functionality- logged in user can send messages
            Directives.path("chat") {
              entity(Directives.as[Communicate]) { message =>
                var jwtAuth = headerValueByName("Authorization")
                jwtAuth { token =>
                  val jwtToken = token.split(" ")(1)
                  if (isTokenExpired(jwtToken)) {
                    logger.error("Token Expired!")
                    complete(OutputMessage(401, "Token has expired. Please login again."))
                  }
                  else if (!JsonWebToken.validate(jwtToken, secretKey)) {
                    logger.error("Token Invalid.")
                    complete(OutputMessage(401, "Invalid token, please register with us first."))
                  }
                  else {
                    val senderEmail = getClaims(jwtToken)("user").split("!")(0)
                    if (DatabaseUtils.doesAccountExist(message.receiver)) {
                      logger.info("Message Transmitted.")
                      system.scheduler.scheduleOnce(500 milliseconds) {
                        system.actorOf(Props[UserActor]).tell(Chat(senderEmail, message.receiver, message.message), ActorRef.noSender)
                      }
                      complete(OutputMessage(250, "The Message has been transmitted."))
                    }
                    else {
                      logger.error("Receiver Email isn't Registered. ")
                      complete(OutputMessage(404, "The receiver does not seem to be registered with us."))
                    }
                  }
                }
              }
            },
            //all group activities happen here
            Directives.pathPrefix("group") {
              Directives.concat(
                //endpoint to create a new group
                Directives.path("create") {
                  entity(Directives.as[GroupName]) { groupName =>
                    headerValueByName("Authorization") { tokenFromUser =>

                      val jwtToken = tokenFromUser.split(" ")
                      jwtToken(1) match {
                        case token if isTokenExpired(token) =>
                          complete(401 -> "Token has expired. Please login again.")

                        case token if !JsonWebToken.validate(token, secretKey) =>
                          complete(401 -> "Token is invalid. Please login again to generate a new one.")

                        case _ =>
                          val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                          val uniqueId: String = (senderEmail + groupName.groupName).toUpperCase
                          val group: Group = Group(uniqueId, groupName.groupName, senderEmail, Array[String](senderEmail))
                          DatabaseUtils.saveGroup(group)
                          logger.info("Group Created.")
                          complete(OutputMessage(250, "The group has been created successfully."))
                      }
                    }
                  }
                },
                //endpoint to add new users to a group- can add multiple users
                Directives.path("addUsers") {
                  entity(Directives.as[GroupAddUser]) { users =>
                    headerValueByName("Authorization") { tokenFromUser =>

                      val jwtToken = tokenFromUser.split(" ")
                      jwtToken(1) match {
                        case token if isTokenExpired(token) =>
                          complete(401 -> "Token has expired. Please login again.")

                        case token if !JsonWebToken.validate(token, secretKey) =>
                          complete(401 -> "Token is invalid. Please login again to generate a new one.")

                        case _ =>
                          val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                          val groupId: String = (senderEmail + users.groupName).toUpperCase
                          DatabaseUtils.addParticipants(groupId, users.participantEmails)
                          logger.info("New Participant Added.")
                          val finalGroup = DatabaseUtils.getGroup(groupId)
                          if (finalGroup == null) {
                            complete(OutputMessage(404, "Group not found. Please create the group before adding participants."))
                          }
                          else {
                            complete(finalGroup)
                          }
                      }
                    }
                  }
                },
                //endpoint to send a message on a group
                Directives.path("chat") {
                  entity(Directives.as[Communicate]) { message =>
                    headerValueByName("Authorization") { tokenFromUser =>

                      val jwtToken = tokenFromUser.split(" ")
                      jwtToken(1) match {
                        case token if isTokenExpired(token) =>
                          complete(401 -> "Token has expired. Please login again.")

                        case token if !JsonWebToken.validate(token, secretKey) =>
                          complete(401 -> "Token is invalid. Please login again to generate a new one.")

                        case _ =>
                          val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                          val groupId: String = message.receiver.toUpperCase
                          val finalGroup = DatabaseUtils.getGroup(groupId)
                          if (finalGroup == null || !finalGroup.participants.contains(senderEmail)) {
                            logger.error("Invalid groupID.")
                            complete(OutputMessage(404, "The group you mentioned either does not exist, or you are not a part of it."))
                          }
                          else {
                            DatabaseUtils.saveGroupChat(Chat(senderEmail, finalGroup.groupId, message.message))
                            logger.info("Chat Saved!")
                            complete(OutputMessage(250, "Message has been successfully sent."))
                          }
                      }
                    }
                  }
                }
              )
            }
          )
        },
        get {
          concat(
            //path to verify JWT token for a given user
            path("verify") {
              parameters('token.as[String], 'email.as[String]) { (token, email) =>
                val jwsObject = JWSObject.parse(token)
                val updateUserAsVerified = DatabaseUtils.verifyEmail(email)
                Await.result(updateUserAsVerified, 60.seconds)
                if (jwsObject.getPayload.toJSONObject.get("email").equals(email)) {
                  logger.info("User Verified & Registered. ")
                  complete(OutputMessage(250, "User successfully verified and registered!"))
                }
                else {
                  logger.error("User Verification Failed.")
                  complete(OutputMessage(401, "User could not be verified!"))
                }
              }
            }
          )
        }
      )
    }
  }

  //binder for the server
  val binder = Http().newServerAt(host, port).bind(route)
  binder.onComplete {
    case Success(serverBinding) => println(println(s"Listening to ${serverBinding.localAddress}"))
    case Failure(error) => println(s"Error : ${error.getMessage}")
  }
}