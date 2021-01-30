package com.bridgelabz.chattest

//import akka.http.scaladsl.marshalling.Marshal
//import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes, MessageEntity, StatusCodes}
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import akka.util.ByteString
//import com.bridgelabz.chat.Routes
//import com.bridgelabz.chat.models.{LoginRequest, LoginRequestJsonSupport, OutputMessage}
//import com.bridgelabz.chat.users.UserManager
//import org.mockito.Mockito.when
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.mockito.MockitoSugar
//import org.scalatest.wordspec.AnyWordSpec
//
//
//class RoutesTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar with LoginRequestJsonSupport with ScalaFutures {
//
//  val mockUserManager: UserManager = mock[UserManager]
//
//  "The service" should {
//
//    "Routes should register a test account for a Post request to /register" in {
//
//      when(mockUserManager.createNewUser(TestVariables.user("test@gmail.com"))).thenReturn(StatusCodes.OK.intValue)
//      when(mockUserManager.sendVerificationEmail(TestVariables.user("test@gmail.com")))
//        .thenReturn(OutputMessage(StatusCodes.OK.intValue, "Verification link sent!"))
//
//      val jsonRequest = ByteString(
//        s"""
//            {
//              "email":"test@gmail.com",
//              "password": "helloworld"
//        """.stripMargin
//      )
//
//      val postRequest = HttpRequest(
//        HttpMethods.POST,
//        uri = "/register",
//        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
//      )
//
//      postRequest ~> Routes.route ~> check {
//        status.isSuccess() shouldEqual true
//      }
//    }
//  }
//}
