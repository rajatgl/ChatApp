package com.bridgelabz.chat

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, concat, extractUri, handleExceptions}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.bridgelabz.chat.models._
import com.bridgelabz.chat.routes.ChatRoutes._
import com.bridgelabz.chat.routes.UserRoutes._
import com.bridgelabz.chat.routes.TokenRoutes._
import com.bridgelabz.chat.routes.GroupRoutes._
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Routes extends App
  with UserJsonSupport
  with LoginRequestJsonSupport
  with CommunicateJsonSupport
  with OutputMessageJsonFormat
  with LoginMessageJsonFormat
  with GroupNameJsonFormat
  with GroupAddUserJsonFormat
  with GroupJsonFormat
  with SeqChatJsonSupport{

  //server configuration variables
  private val host = System.getenv("Host")
  private val port = System.getenv("Port").toInt
  private val logger = Logger("Routes")

  //actor system and execution context for AkkaHTTP server
  implicit val system: ActorSystem = ActorSystem("Chat")
  implicit val executor: ExecutionContext = system.dispatcher

  //catching Null Pointer Exception and other default Exceptions
  val exceptionHandler = ExceptionHandler {
    case nex: NullPointerException =>
      extractUri { _ =>
        logger.error(nex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(OutputMessage(StatusCodes.BAD_REQUEST.intValue(), "Null value found while parsing the data. Contact the admin."))
      }
    case ex: Exception =>
      extractUri { _ =>
        logger.error(ex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(OutputMessage(StatusCodes.BAD_REQUEST.intValue(), "Some error occured. Please try again later."))
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
        loginUserRoute,
        registerUserRoute,
        chatRoute,
        getChatRoute,
        createGroupRoute,
        usersGroupRoute,
        chatGroupRoute,
        getChatGroupRoute,
        verifyTokenRoute
      )
    }
  }

  //binder for the server
  val binder = Http().newServerAt(host, port).bind(route)
  binder.onComplete {
    case Success(serverBinding) => logger.info(s"Listening to ${serverBinding.localAddress}")
    case Failure(error) => logger.error(s"Error : ${error.getMessage}")
  }
}

