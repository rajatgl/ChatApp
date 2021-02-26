package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.database.interfaces.{ILoader, ISaver}
import com.bridgelabz.chat.models.Group
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.{Completed, MongoCollection}

import scala.concurrent.Future

class GroupDatabase(collectionName: String = Constants.collectionNameForGroup,
                    databaseName: String = Constants.databaseName)

  extends MongoDbConfig[Group] with ISaver[Group] with ILoader[Group] {

  val codecProvider: CodecProvider = Macros.createCodecProvider[Group]()
  val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProvider),
    DEFAULT_CODEC_REGISTRY
  )

  val collection: MongoCollection[Group] = getCollection(collectionName, codecRegistry, databaseName)

  /**
   *
   * @param entity object to be created in the database
   * @return any status identifier for the create operation
   */
  override def create(entity: Group): Future[Completed] = {
    collection.insertOne(entity).toFuture()
  }

  /**
   *
   * @return sequence of objects in the database
   */
  override def read(): Future[Seq[Group]] = {
    collection.find().toFuture()
  }

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be replaced/updated)
   * @param entity     new object to override the old one
   * @return any status identifier for the update operation
   */
  override def update(identifier: Any, entity: Group, fieldName: String = "groupId"): Future[Any] = {
    collection.replaceOne(equal(fieldName, identifier), entity).toFuture()
  }

  /**
   *
   * @param fieldName  name of the parameter in the object defined by the identifier
   * @param identifier parameter of the (object in the database to be deleted)
   * @return any status identifier for the update operation
   */
  override def delete(identifier: Any, fieldName: String = "groupId"): Future[Any] = {
    collection.deleteOne(equal(fieldName, identifier)).toFuture()
  }
}
