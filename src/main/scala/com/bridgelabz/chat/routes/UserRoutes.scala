package com.bridgelabz.chat.routes

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{complete, entity, onComplete, path, post, respondWithHeaders}
import akka.http.scaladsl.server.{Directives, Route}
import com.bridgelabz.chat.database.managers.UserManager
import com.bridgelabz.chat.jwt.TokenManager
import com.bridgelabz.chat.models.{LoginRequest, LoginRequestJsonSupport, OutputMessage, OutputMessageJsonFormat, User}
import com.bridgelabz.chat.users.{EmailManager, EncryptionManager}
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created on 1/29/2021.
 * Class: UserRoutes.scala
 * Author: Rajat G.L.
 */
class UserRoutes(userManager: UserManager) extends LoginRequestJsonSupport with OutputMessageJsonFormat {

  val loginLogger: Logger = Logger("loginRoute")
  val registerLogger: Logger = Logger("registerRoute")
  val emailManager: EmailManager = new EmailManager

  /**
   *
   * @return route for handling logging in of a user (and token generation)
   */
  def loginUserRoute: Route = post {
    //allow users to login after respective checks
    path("login") {
      entity(Directives.as[LoginRequest]) { request =>

        //check if the user login was successful
        val user: User = User(request.email, request.password, verificationComplete = false)
        val encryptedUser: User = User(request.email, EncryptionManager.encrypt(user), verificationComplete = true)
        val userLoginStatusFuture: Future[Int] = userManager.userLogin(user)

        onComplete(userLoginStatusFuture) {
          case Success(userLoginStatus) =>
            if (userLoginStatus == StatusCodes.OK.intValue()) {
              loginLogger.info("User Login Successful.")
              respondWithHeaders(RawHeader("Token", TokenManager.generateUserToken(encryptedUser))) {
                complete(userLoginStatus -> OutputMessage(userLoginStatus, "Logged in successfully. Happy to serve you!"))
              }
            }
            else if (userLoginStatus == StatusCodes.NOT_FOUND.intValue()) {
              loginLogger.error("Account Not Registered.")
              complete(userLoginStatus ->
                OutputMessage(
                  userLoginStatus,
                  "Login failed. Your account does not seem to exist. If you did not register yet, head to: http://localhost:9000/register"
                )
              )
            }
            else {
              loginLogger.error("Account Verification Incomplete.")
              complete(userLoginStatus ->
                OutputMessage(userLoginStatus, "Login failed. Your account is not verified. Head to http://localhost:9000/verify for the same."))
            }

          case Failure(exception) =>

            loginLogger.error(exception.getMessage)
            complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() ->
              OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Login failed. Contact the admin."))
        }
      }
    }
  }

  /**
   *
   * @return route for handling registration of a user
   */
  def registerUserRoute: Route = post {
    //allow users to login after respective checks
    path("register") {
      entity(Directives.as[LoginRequest]) { request =>
        val user: User = User(request.email, request.password, verificationComplete = false)
        val userRegisterStatus: Future[(Int, Future[Any])] = userManager.saveUser(user)

        onComplete(userRegisterStatus) {
          case Success(userRegisterStatus) =>
            if (userRegisterStatus._1 == StatusCodes.OK.intValue()) {
              registerLogger.info(s"Email verification started for ${request.email}.")

              onComplete(userRegisterStatus._2) {
                case Success(_) => emailManager.sendVerificationEmail(user)
                  complete(OutputMessage(StatusCodes.OK.intValue(), "Verification link sent!"))
                case Failure(_) => complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() ->
                  OutputMessage(StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "We encountered on error while registering you.")
                )
              }

            }
            else if (userRegisterStatus._1 == StatusCodes.BAD_REQUEST.intValue()) {
              registerLogger.error("Invalid Email.")
              complete(userRegisterStatus._1 -> OutputMessage(userRegisterStatus._1, "Bad email, try again with a valid entry."))
            }
            else {
              registerLogger.error("Email is already registered. Provide a new one.")
              complete(userRegisterStatus._1 -> OutputMessage(userRegisterStatus._1, "User registration failed. E-mail is already registered."))
            }

          case Failure(exception) =>
            registerLogger.error(exception.getMessage)
            complete(StatusCodes.INTERNAL_SERVER_ERROR.intValue() ->
              OutputMessage(
                StatusCodes.INTERNAL_SERVER_ERROR.intValue(), "Server error, contact the admin."
              )
            )
        }
      }
    }
  }
}
