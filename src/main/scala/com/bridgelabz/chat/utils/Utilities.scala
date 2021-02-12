package com.bridgelabz.chat.utils

import org.scalatest.time.Span

import scala.concurrent.{Await, Future, TimeoutException}

/**
 * Created on 1/30/2021.
 * Class: Utilities.scala
 * Author: Rajat G.L.
 */
object Utilities {
  /**
   *
   * @param future Future instance to be awaited
   * @param time timeout Span
   * @tparam T generic object type to be extracted
   * @return result else None in case of exception.
   */
  def tryAwait[T](future: Future[T], time: Span): Option[T] = {
    try {
      Some(Await.result(future, time))
    }
    catch {
      case throwable: TimeoutException => throwable.printStackTrace(); None
    }
  }
}
