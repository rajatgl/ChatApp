package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.interfaces.{ICrud, ILoader}
import com.bridgelabz.chat.database.mongodb.CodecRepository.CodecNames
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.Future

class DatabaseCollection[T: scala.reflect.ClassTag](collectionName: String,
                                                    codecName: CodecNames,
                                                    databaseName: String = Constants.databaseName,
                                                    mongoDbConfig: MongoDbConfig = new MongoDbConfig())
  extends ICrud[T] {

  def collection(): MongoCollection[T] = {

    val codecRegistry = CodecRepository.getCodecRegistry(codecName)
    mongoDbConfig.getCollection[T](collectionName, codecRegistry, databaseName)
  }

  /**
   *
   * @param entity object to be created in the database
   * @return any status identifier for the create operation
   */
  override def create(entity: T): Future[Any] = collection().insertOne(entity).toFuture()

  /**
   *
   * @param identifier parameter of the (object in the database to be replaced/updated)
   * @param entity     new object to override the old one
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @return any status identifier for the update operation
   */
  override def update(identifier: Any, entity: T, fieldName: String): Future[Any] = collection().replaceOne(equal(fieldName, identifier), entity).toFuture()

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be deleted)
   * @return any status identifier for the update operation
   */
  override def delete(identifier: Any, fieldName: String): Future[Any] = collection().deleteOne(equal(fieldName, identifier)).toFuture()

  /**
   *
   * @return sequence of objects in the database
   */
  override def read(): Future[Seq[T]] = collection().find().toFuture()
}
