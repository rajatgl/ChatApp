package com.bridgelabz.chat.database.interfaces

import scala.concurrent.Future

trait ICrud[T] {
  /**
   *
   * @param entity object to be created in the database
   * @return any status identifier for the create operation
   */
  def create(entity: T): Future[Any]

  /**
   *
   * @return sequence of objects in the database
   */
  def read(): Future[Seq[T]]

  /**
   *
   * @param identifier parameter of the (object in the database to be replaced/updated)
   * @param entity     new object to override the old one
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @return any status identifier for the update operation
   */
  def update(identifier: Any, entity: T, fieldName: String): Future[Any]

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be deleted)
   * @return any status identifier for the update operation
   */
  def delete(identifier: Any, fieldName: String): Future[Any]

}
