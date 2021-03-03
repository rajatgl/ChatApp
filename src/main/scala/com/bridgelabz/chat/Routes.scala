package com.bridgelabz.chat

import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, concat, extractUri, handleExceptions}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.managers.{ChatManager, GroupManager, UserManager}
import com.bridgelabz.chat.database.mongodb.{CodecRepository, DatabaseCollection}
import com.bridgelabz.chat.models._
import com.bridgelabz.chat.routes.{ChatRoutes, GroupRoutes, TokenRoutes, UserRoutes}
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

  // $COVERAGE-OFF$
  //server configuration variables
  protected val host: String = Constants.host
  protected val port: Int = Constants.port

  //actor system and execution context for AkkaHTTP server
  implicit val system: ActorSystem = ActorSystem("Chat")
  implicit val executor: ExecutionContext = system.dispatcher

  private val logger = Logger("Routes")

  val userManager: UserManager = new UserManager(new DatabaseCollection[User](Constants.collectionName, CodecRepository.USER))
  val chatManager: ChatManager = new ChatManager(new DatabaseCollection[Chat](Constants.collectionNameForChat, CodecRepository.CHAT))
  val groupManager: GroupManager = new GroupManager(new DatabaseCollection[Group](Constants.collectionNameForGroup, CodecRepository.GROUP))

  //catching Null Pointer Exception and other default Exceptions
  val exceptionHandler = ExceptionHandler {
    case nex: NullPointerException =>
      extractUri { _ =>
        logger.error(nex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() ->
          OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Null value found while parsing the data. Contact the admin."))
      }
    case ex: Exception =>
      extractUri { _ =>
        logger.error(ex.getStackTrace.mkString("Array(", ", ", ")"))
        complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() ->
          OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Some error occured. Please try again later."))
      }
  }

  /**
   * handles all the get post requests to appropriate path endings
   *
   * @return Route object needed for server for binding
   */

  // $COVERAGE-ON$
  def route(userManager: UserManager, chatManager: ChatManager, groupManager: GroupManager): Route = {

    val userRoutes = new UserRoutes(userManager)
    val chatRoutes = new ChatRoutes(chatManager,userManager)
    val groupRoutes = new GroupRoutes(groupManager,chatManager, userManager)
    val tokenRoutes = new TokenRoutes(userManager)

    handleExceptions(exceptionHandler) {
      concat(

        //user routes
        userRoutes.loginUserRoute,
        userRoutes.registerUserRoute,

        //chat routes
        chatRoutes.chatRoute,
        chatRoutes.getChatRoute,

        //group routes
        groupRoutes.createGroupRoute,
        groupRoutes.usersGroupRoute,
        groupRoutes.chatGroupRoute,
        groupRoutes.getChatGroupRoute,

        //token route
        tokenRoutes.verifyTokenRoute
      )
    }
  }

  // $COVERAGE-OFF$
  //binder for the server
  val binder = Http().newServerAt(host, port).bind(route(userManager,chatManager,groupManager))
  binder.onComplete {
    case Success(serverBinding) => logger.info(s"Listening to ${serverBinding.localAddress}")
    case Failure(error) => logger.error(s"Error : ${error.getMessage}")
  }
}
