package com.bridgelabz.chat

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.bridgelabz.chat.database.DatabaseUtils
import com.bridgelabz.chat.users.UserManager

import scala.concurrent.ExecutionContext

trait IAkkaRouteService {

  //server configuration variables
  protected val host: String = System.getenv("Host")
  protected val port: Int = System.getenv("Port").toInt

  //actor system and execution context for AkkaHTTP server
  implicit val system: ActorSystem = ActorSystem("Chat")
  implicit val executor: ExecutionContext = system.dispatcher

  def route(databaseUtils: DatabaseUtils, userManager: UserManager): Route
}
