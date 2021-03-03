package com.bridgelabz.chat.routes

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{_symbol2NR, complete, entity, headerValueByName, onComplete, parameters, path, pathPrefix, post}
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.JsonWebToken
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.managers.{ChatManager, GroupManager, UserManager}
import com.bridgelabz.chat.jwt.TokenManager.{getClaims, isTokenExpired}
import com.bridgelabz.chat.models._
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

/**
 * Created on 1/29/2021.
 * Class: GroupRoutes.scala
 * Author: Rajat G.L.
 */
class GroupRoutes(groupManager: GroupManager, chatManager: ChatManager, userManager: UserManager)
  extends GroupNameJsonFormat
    with OutputMessageJsonFormat
    with GroupAddUserJsonFormat
    with GroupJsonFormat
    with CommunicateJsonSupport
    with SeqChatJsonSupport {

  val logger: Logger = Logger("GroupRoutes")

  /**
   *
   * @return route for handling creation of a group
   */
  def createGroupRoute: Route = post {
    Directives.pathPrefix("group") {
      Directives.path("create") {
        entity(Directives.as[GroupName]) { groupName =>
          headerValueByName("Authorization") { tokenFromUser =>

            val jwtToken = tokenFromUser.split(" ")
            jwtToken(1) match {
              case token if isTokenExpired(token) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, Constants.secretKey) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("identifier").split("!")(0)
                val uniqueId: String = (senderEmail + groupName.groupName).toUpperCase
                val group: Group = Group(uniqueId, groupName.groupName, senderEmail, Array[String](senderEmail))
                groupManager.saveGroup(group)
                logger.info("Group Created.")
                complete(StatusCodes.OK.intValue() ->
                  OutputMessage(StatusCodes.OK.intValue(), "The group has been created successfully."))
            }
          }
        }
      }
    }
  }

  // Cyclomatic Complexity of 12 can't be avoided due to Async Directives
  //scalastyle:off
  /**
   *
   * @return route for handling adding users to a group
   */
  def usersGroupRoute: Route = post {
    Directives.pathPrefix("group") {
      Directives.path("users") {
        entity(Directives.as[GroupAddUser]) { users =>
          headerValueByName("Authorization") { tokenFromUser =>

            val jwtToken = tokenFromUser.split(" ")
            jwtToken(1) match {
              case token if isTokenExpired(token) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, Constants.secretKey) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("identifier").split("!")(0)
                val groupId: String = (senderEmail + users.groupName).toUpperCase

                for(userEmail <- users.participantEmails){

                  onComplete(userManager.doesAccountExist(userEmail)){
                    case Success(value) if !value => complete(
                      StatusCodes.BAD_REQUEST.intValue() ->
                      OutputMessage(
                        StatusCodes.BAD_REQUEST.intValue(),
                        s"$userEmail is not registered with us."
                      )
                    )
                  }
                }

                groupManager.addParticipants(groupId, users.participantEmails)

                complete(StatusCodes.OK.intValue() ->
                  OutputMessage(StatusCodes.OK.intValue(), "Users added to your group."))
            }
          }
        }
      }
    }
  }

  /**
   *
   * @return route for handling chatting on a group
   */
  def chatGroupRoute: Route = post {
    Directives.pathPrefix("group") {
      Directives.path("chat") {
        entity(Directives.as[Communicate]) { message =>
          headerValueByName("Authorization") { tokenFromUser =>

            val jwtToken = tokenFromUser.split(" ")
            jwtToken(1) match {
              case token if isTokenExpired(token) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, Constants.secretKey) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("identifier").split("!")(0)
                val groupId: String = message.receiver.toUpperCase
                val finalGroup = groupManager.getGroup(groupId)

                onComplete(finalGroup) {
                  case Success(value) if value.nonEmpty =>
                    val saveChatFuture = chatManager.saveGroupChat(Chat(senderEmail, value.head.groupId, message.message), value.head)
                    onComplete(saveChatFuture) {
                      case Success(_) =>
                        logger.info("Chat Saved!")
                        complete(StatusCodes.OK.intValue() -> OutputMessage(StatusCodes.OK.intValue(), "Message has been successfully sent."))
                      case Failure(exception) =>
                        logger.error(exception.getMessage)
                        complete(StatusCodes.NOT_FOUND.intValue() -> OutputMessage(StatusCodes.NOT_FOUND.intValue(),
                          "The group you mentioned either does not exist, or you are not a part of it."))
                    }
                  case Failure(exception) => logger.error(s"Invalid groupID: ${exception.getMessage}")
                    complete(StatusCodes.NOT_FOUND.intValue() -> OutputMessage(StatusCodes.NOT_FOUND.intValue(),
                      "The group you mentioned either does not exist, or you are not a part of it."))

                  case _ => logger.info("Group not found.")
                    complete(StatusCodes.NOT_FOUND.intValue() -> OutputMessage(StatusCodes.NOT_FOUND.intValue(),
                      "The group you mentioned either does not exist."))
                }
            }
          }
        }
      }
    }
  }

  /**
   *
   * @return route for fetching a list of chats on a group
   */

  def getChatGroupRoute: Route = Directives.get {
    //fetch chats for provided groupID
    pathPrefix("group") {
      path("chat") {
        parameters('groupId.as[String]) { groupId =>
          headerValueByName("Authorization") { tokenFromUser =>
            val jwtToken = tokenFromUser.split(" ")
            jwtToken(1) match {
              case token if isTokenExpired(token) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() -> OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))
              case token if !JsonWebToken.validate(token, Constants.secretKey) =>
                complete(StatusCodes.UNAUTHORIZED.intValue() ->
                  OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))
              case _ =>
                val senderEmail = getClaims(jwtToken(1))("identifier").split("!")(0)
                val group = groupManager.getGroup(groupId)
                onComplete(group) {
                  case Success(value) =>
                    if (value.nonEmpty && value.head.participants.contains(senderEmail)) {
                      onComplete(chatManager.getGroupMessages(groupId)) {
                        case Success(value) =>
                          complete(SeqChat(value))
                        case Failure(exception) =>
                          logger.error(exception.getMessage)
                          complete(OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Could not load the requested messages"))
                      }
                    } else {
                      complete(StatusCodes.FORBIDDEN.intValue() ->
                        OutputMessage(StatusCodes.FORBIDDEN.intValue(), "You are not a member of this group."))
                    }
                  case Failure(exception) =>
                    logger.error(exception.getMessage)
                    complete(StatusCodes.NOT_FOUND.intValue() ->
                      OutputMessage(StatusCodes.NOT_FOUND.intValue(), "Group not found."))
                }
            }
          }
        }
      }
    }
  }
}

