package com.bridgelabz.chat.routes

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{_symbol2NR, complete, entity, headerValueByName, parameters, path, post}
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.JsonWebToken
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager.{getClaims, isTokenExpired, secretKey}
import com.bridgelabz.chat.models._
import com.typesafe.scalalogging.Logger

/**
 * Created on 1/29/2021.
 * Class: GroupRoutes.scala
 * Author: Rajat G.L.
 */
object GroupRoutes
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
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, secretKey) =>
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                val uniqueId: String = (senderEmail + groupName.groupName).toUpperCase
                val group: Group = Group(uniqueId, groupName.groupName, senderEmail, Array[String](senderEmail))
                DatabaseUtils.saveGroup(group)
                logger.info("Group Created.")
                complete(OutputMessage(StatusCodes.OK.intValue(), "The group has been created successfully."))
            }
          }
        }
      }
    }
  }

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
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, secretKey) =>
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                val groupId: String = (senderEmail + users.groupName).toUpperCase
                DatabaseUtils.addParticipants(groupId, users.participantEmails)
                logger.info("New Participant Added.")
                val finalGroup = DatabaseUtils.getGroup(groupId)
                if (finalGroup.isEmpty) {
                  complete(OutputMessage(StatusCodes.NOT_FOUND.intValue(), "Group not found. Please create the group before adding participants."))
                }
                else {
                  complete(finalGroup.get)
                }
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
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, secretKey) =>
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                val groupId: String = message.receiver.toUpperCase
                val finalGroup = DatabaseUtils.getGroup(groupId)
                if (finalGroup.isEmpty || !finalGroup.get.participants.contains(senderEmail)) {
                  logger.error("Invalid groupID.")
                  complete(OutputMessage(StatusCodes.NOT_FOUND.intValue(),
                    "The group you mentioned either does not exist, or you are not a part of it."))
                }
                else {
                  DatabaseUtils.saveGroupChat(Chat(senderEmail, finalGroup.get.groupId, message.message))
                  logger.info("Chat Saved!")
                  complete(OutputMessage(StatusCodes.OK.intValue(), "Message has been successfully sent."))
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
  def getChatGroupRoute: Route = Directives.get{
    Directives.pathPrefix("group") {

      //fetch chats for provided groupID
      path("chat") {
        parameters('groupId.as[String]) { groupId =>
          headerValueByName("Authorization") { tokenFromUser =>

            val jwtToken = tokenFromUser.split(" ")
            jwtToken(1) match {
              case token if isTokenExpired(token) =>
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

              case token if !JsonWebToken.validate(token, secretKey) =>
                complete(OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

              case _ =>
                val senderEmail = getClaims(jwtToken(1))("user").split("!")(0)
                val group = DatabaseUtils.getGroup(groupId)
                if (group.isEmpty && group.get.participants.contains(senderEmail)) {
                  complete(SeqChat(DatabaseUtils.getGroupMessages(groupId)))
                }
                else {
                  complete(OutputMessage(StatusCodes.NOT_FOUND.intValue(), "Group not found, or you are not a member of this group."))
                }
            }
          }
        }
      }
    }
  }
}

