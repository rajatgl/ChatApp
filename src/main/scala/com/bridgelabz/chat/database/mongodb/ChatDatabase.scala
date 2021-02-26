package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.interfaces.{ILoader, ISaver}
import com.bridgelabz.chat.models.Chat
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.{Completed, MongoCollection}

import scala.concurrent.Future

class ChatDatabase(collectionName: String = Constants.collectionNameForChat,
                   databaseName: String = Constants.databaseName)

  extends MongoDbConfig[Chat] with ISaver[Chat] with ILoader[Chat] {

  val codecProvider: CodecProvider = Macros.createCodecProvider[Chat]()
  val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProvider),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[Chat] = getCollection(collectionName, codecRegistry, databaseName)

  /**
   *
   * @param entity object to be created in the database
   * @return any status identifier for the create operation
   */
  override def create(entity: Chat): Future[Completed] = {
    collection.insertOne(entity).toFuture()
  }

  /**
   *
   * @return sequence of objects in the database
   */
  override def read(): Future[Seq[Chat]] = {
    collection.find().toFuture()
  }

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be replaced/updated)
   * @param entity     new object to override the old one
   * @return any status identifier for the update operation
   */
  override def update(identifier: Any, entity: Chat, fieldName: String): Future[Any] = {
    collection.replaceOne(equal(fieldName, identifier), entity).toFuture()
  }

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be deleted)
   * @return any status identifier for the update operation
   */
  override def delete(identifier: Any, fieldName: String): Future[Any] = {
    collection.deleteOne(equal(fieldName, identifier)).toFuture()
  }
}
