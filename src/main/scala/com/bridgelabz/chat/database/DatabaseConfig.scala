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
protected class DatabaseConfig(uri: String = s"mongodb://${Constants.mongoHost}:${Constants.mongoPort}") {

  private val mongoClient: MongoClient = MongoClient(uri)

  //codec providers and registries
  private val codecProvider: CodecProvider = Macros.createCodecProvider[User]()
  private val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProvider),
    DEFAULT_CODEC_REGISTRY
  )

  private val codecProviderForChat: CodecProvider = Macros.createCodecProvider[Chat]()
  private val codecRegistryForChat: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForChat),
    DEFAULT_CODEC_REGISTRY
  )

  private val codecProviderForGroup: CodecProvider = Macros.createCodecProvider[Group]()
  private val codecRegistryForGroup: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForGroup),
    DEFAULT_CODEC_REGISTRY
  )

  //respective collections
  //user collection configs
  private val database: MongoDatabase = mongoClient.getDatabase(Constants.databaseName).withCodecRegistry(codecRegistry)
  private val collectionName: String = Constants.collectionName

  //chat collection configs
  private val databaseForChat: MongoDatabase = mongoClient.getDatabase(Constants.databaseName).withCodecRegistry(codecRegistryForChat)
  private val collectionNameForChat: String = Constants.collectionNameForChat

  //group chat collection configs
  private val collectionNameForGroupChat: String = Constants.collectionNameForGroupChat

  //group collection configs
  private val databaseForGroup: MongoDatabase = mongoClient.getDatabase(Constants.databaseName).withCodecRegistry(codecRegistryForGroup)
  private val collectionNameForGroup: String = Constants.collectionNameForGroup

  //Accessible to anyone who extends DatabaseConfig
  protected val collection: MongoCollection[User] = database.getCollection(collectionName)
  protected val collectionForGroup: MongoCollection[Group] = databaseForGroup.getCollection(collectionNameForGroup)
  protected val collectionForGroupChat: MongoCollection[Chat] = databaseForChat.getCollection(collectionNameForGroupChat)
  protected val collectionForChat: MongoCollection[Chat] = databaseForChat.getCollection(collectionNameForChat)
}
