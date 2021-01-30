package com.bridgelabz.chat.routes

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{complete, entity, path, post, respondWithHeaders}
import akka.http.scaladsl.server.{Directives, Route}
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{LoginRequest, LoginRequestJsonSupport, OutputMessage, OutputMessageJsonFormat, User}
import com.bridgelabz.chat.users.{EncryptionManager, UserManager}
import com.typesafe.scalalogging.Logger

/**
 * Created on 1/29/2021.
 * Class: UserRoutes.scala
 * Author: Rajat G.L.
 */
object UserRoutes extends LoginRequestJsonSupport with OutputMessageJsonFormat {

  val loginLogger: Logger = Logger("loginRoute")
  val registerLogger: Logger = Logger("registerRoute")
  val userManager: UserManager = new UserManager

  /**
   *
   * @return route for handling logging in of a user (and token generation)
   */
  def loginUserRoute: Route = {
    post {
      //allow users to login after respective checks
      path("login") {
        entity(Directives.as[LoginRequest]) { request =>

          //check if the user login was successful
          val user: User = User(request.email, request.password, verificationComplete = false)
          val encryptedUser: User = User(request.email, EncryptionManager.encrypt(user), verificationComplete = true)
          val userLoginStatus: Int = userManager.userLogin(user)

          if (userLoginStatus == StatusCodes.OK.intValue()) {
            loginLogger.info("User Login Successful.")
            respondWithHeaders(RawHeader("Token", TokenManager.generateLoginId(encryptedUser))) {
              complete(OutputMessage(userLoginStatus, "Logged in successfully. Happy to serve you!"))
            }
          }
          else if (userLoginStatus == StatusCodes.NOT_FOUND.intValue()) {
            loginLogger.error("Account Not Registered.")
            complete(OutputMessage(userLoginStatus,
              "Login failed. Your account does not seem to exist. If you did not register yet, head to: http://localhost:9000/register"))
          }
          else {
            loginLogger.error("Account Verification Incomplete.")
            complete(OutputMessage(userLoginStatus, "Login failed. Your account is not verified. Head to http://localhost:9000/verify for the same."))
          }
        }
      }
    }
  }

  /**
   *
   * @return route for handling registration of a user
   */
  def registerUserRoute: Route = {
    post {
      //allow users to login after respective checks
      path("register") {
        entity(Directives.as[LoginRequest]) { request =>
          val user: User = User(request.email, request.password, verificationComplete = false)
          val userRegisterStatus: Int = userManager.createNewUser(user)

          if (userRegisterStatus == StatusCodes.OK.intValue()) {
            registerLogger.info(s"Email verification started for ${request.email}.")
            complete(userManager.sendVerificationEmail(user))
          }
          else if (userRegisterStatus == StatusCodes.BAD_REQUEST.intValue()) {
            registerLogger.error("Invalid Email.")
            complete(OutputMessage(userRegisterStatus, "Bad email, try again with a valid entry."))
          }
          else {
            registerLogger.error("Email is already registered. Provide a new one.")
            complete(OutputMessage(userRegisterStatus, "User registration failed. E-mail is already registered."))
          }
        }
      }
    }
  }
}
