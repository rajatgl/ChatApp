package com.bridgelabz.chat.database.interfaces

import scala.concurrent.Future

trait ILoader[T] {
  /**
   *
   * @return sequence of objects in the database
   */
  def read(): Future[Seq[T]]
}
