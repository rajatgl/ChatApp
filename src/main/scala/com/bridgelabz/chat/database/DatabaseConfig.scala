package com.bridgelabz.chat.database

import com.bridgelabz.chat.constants.Constants
import com.bridgelabz.chat.models.{Chat, Group, User}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

/**
 * Created on 1/8/2021.
 * Class: DbConfig.scala
 * Author: Rajat G.L.
 */
protected trait DatabaseConfig {

  val mongoClient: MongoClient = MongoClient()
  val databaseName: String = Constants.databaseName

  //codec providers and registries
  val codecProvider: CodecProvider = Macros.createCodecProvider[User]()
  val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProvider),
    DEFAULT_CODEC_REGISTRY
  )

  val codecProviderForChat: CodecProvider = Macros.createCodecProvider[Chat]()
  val codecRegistryForChat: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForChat),
    DEFAULT_CODEC_REGISTRY
  )

  val codecProviderForGroup: CodecProvider = Macros.createCodecProvider[Group]()
  val codecRegistryForGroup: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForGroup),
    DEFAULT_CODEC_REGISTRY
  )

  //respective collections
  val database: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)
  val collectionName: String = Constants.collectionName
  val collection: MongoCollection[User] = database.getCollection(collectionName)

  val databaseForChat: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistryForChat)
  val collectionNameForChat: String = Constants.collectionNameForChat
  val collectionForChat: MongoCollection[Chat] =databaseForChat.getCollection(collectionNameForChat)

  val collectionNameForGroupChat: String = Constants.collectionNameForGroupChat
  val collectionForGroupChat: MongoCollection[Chat] =databaseForChat.getCollection(collectionNameForGroupChat)

  val databaseForGroup: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistryForGroup)
  val collectionNameForGroup: String = Constants.collectionNameForGroup
  val collectionForGroup: MongoCollection[Group] = databaseForGroup.getCollection(collectionNameForGroup)
}
