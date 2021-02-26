package com.bridgelabz.chat.routes

import akka.actor.{ActorRef, Props}
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, entity, headerValueByName, onComplete, path, post}
import akka.http.scaladsl.server.{Directives, Route}
import authentikat.jwt.JsonWebToken
import com.bridgelabz.chat.Routes.{executor, system}
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.jwt.TokenManager.{getClaims, isTokenExpired}
import com.bridgelabz.chat.models._
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created on 1/29/2021.
 * Class: ChatRoutes.scala
 * Author: Rajat G.L.
 */
class ChatRoutes(databaseUtils: DatabaseUtils) extends CommunicateJsonSupport with OutputMessageJsonFormat with SeqChatJsonSupport {

  val logger: Logger = Logger("ChatRoute")
  /**
   *
   * @return route for handling one-to-one chatting
   */
  def chatRoute: Route = post {
    //chatting functionality- user with valid token can send messages
    Directives.path("chat") {
      entity(Directives.as[Communicate]) { message =>
        val jwtAuth = headerValueByName("Authorization")
        jwtAuth { token =>

          val jwtToken = token.split(" ")
          jwtToken(1) match {
            case token if isTokenExpired(token) =>
              complete(StatusCodes.UNAUTHORIZED.intValue() ->
                OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token has expired. Please login again."))

            case token if !JsonWebToken.validate(token, Constants.secretKey) =>
              complete(StatusCodes.UNAUTHORIZED.intValue() ->
                OutputMessage(StatusCodes.UNAUTHORIZED.intValue(), "Token is invalid. Please login again to generate a new one."))

            case _ =>
              val senderEmail = getClaims(jwtToken(1))("identifier").split("!")(0)
              onComplete(databaseUtils.doesAccountExist(message.receiver)){
                case Success(value) => if (value) {
                  logger.info("Message Transmitted.")
                  system.scheduler.scheduleOnce(500.milliseconds) {
                    system.actorOf(Props(new UserActor())).tell(Chat(senderEmail, message.receiver, message.message), ActorRef.noSender)
                  }
                  complete(StatusCodes.OK.intValue() ->
                    OutputMessage(StatusCodes.OK.intValue(), "The Message has been transmitted."))
                }
                else {
                  logger.error("Receiver Email isn't Registered. ")
                  complete(StatusCodes.NOT_FOUND.intValue() ->
                    OutputMessage(StatusCodes.NOT_FOUND.intValue(), "The receiver does not seem to be registered with us."))
                }
                case Failure(exception) => logger.error(exception.getMessage)
                  complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() -> OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(),"Some error occurred"))
              }
          }
        }
      }
    }
  }

  /**
   *
   * @return route for fetching a list of chats a user received
   */
  def getChatRoute: Route = Directives.get {
    //end-point for one on one chat
    path("chat") {
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
            onComplete(databaseUtils.getMessages(senderEmail)){
              case Success(value) =>
                complete(StatusCodes.OK.intValue() -> SeqChat(value))
              case Failure(exception) =>
                logger.error(exception.getMessage)
                complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() -> OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(),"Internal Server Error"))
            }
        }
      }
    }
  }
}
