package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.constants.Constants
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

/**
 * Created on 1/8/2021.
 * Class: DbConfig.scala
 * Author: Rajat G.L.
 */
protected class MongoDbConfig(uri: String = s"mongodb://${Constants.mongoHost}:${Constants.mongoPort}") {

  //Add any other configurations here
  private val mongoClient: MongoClient = MongoClient(uri)

  def getCollection[T: scala.reflect.ClassTag](collectionName: String,
                    codecRegistry: CodecRegistry,
                    databaseName: String):
  MongoCollection[T] = {

    val database: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
    database.getCollection[T](collectionName)
  }
}
