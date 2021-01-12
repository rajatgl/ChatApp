package com.bridgelabz.chat.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
 * Created on 12/22/2020.
 * Class: UserJsonSupport.scala
 * Author: Rajat G.L.
 */
//For Marshalling the data
trait UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
}
