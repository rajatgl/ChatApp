package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.interfaces.{ILoader, ISaver}
import com.bridgelabz.chat.models.User
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.{Completed, MongoCollection}

import scala.concurrent.Future

class UserDatabase(collectionName: String = Constants.collectionName,
                   databaseName: String = Constants.databaseName)
  extends MongoDbConfig[User] with ISaver[User] with ILoader[User] {

  val codecProvider: CodecProvider = Macros.createCodecProvider[User]()
  val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProvider),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[User] = getCollection(collectionName, codecRegistry, databaseName)

  /**
   *
   * @param entity object to be created in the database
   * @return any status identifier for the create operation
   */
  def create(entity: User): Future[Completed] = {
    collection.insertOne(entity).toFuture()
  }

  /**
   *
   * @return sequence of objects in the database
   */
  def read(): Future[Seq[User]] = {
    collection.find().toFuture()
  }

  /**
   *
   * @param fieldName name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be replaced/updated)
   * @param entity new object to override the old one
   * @return any status identifier for the update operation
   */
  def update(identifier: Any, entity: User, fieldName: String = "email"): Future[Any] = {
    collection.replaceOne(equal(fieldName, identifier), entity).toFuture()
  }

  /**
   *
   * @param fieldName name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be deleted)
   * @return any status identifier for the update operation
   */
  def delete(identifier: Any, fieldName: String = "email"): Future[Any] = {
    collection.deleteOne(equal(fieldName, identifier)).toFuture()
  }
}