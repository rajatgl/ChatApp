package com.bridgelabz.chat.utils

import org.scalatest.time.Span
import scala.concurrent.{Await, Future}

/**
 * Created on 1/30/2021.
 * Class: Utilities.scala
 * Author: Rajat G.L.
 */
object Utilities {
  def tryAwait[T](future: Future[T], time: Span): Option[T] = {
    try {
      Some(Await.result(future, time))
    }
    catch {
      case error: Throwable => error.printStackTrace()
        None
    }
  }
}
