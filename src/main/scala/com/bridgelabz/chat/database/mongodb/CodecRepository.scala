package com.bridgelabz.chat.database.mongodb

import com.bridgelabz.chat.models.{Chat, Group, User}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros

/**
 * Created on 3/1/2021.
 * Class: CodecRepository.scala
 * Author: Rajat G.L.
 */
object CodecRepository extends Enumeration {

  type CodecNames = Value
  val USER, CHAT, GROUP = Value

  private val codecProviderForChat: CodecProvider = Macros.createCodecProvider[Chat]()
  private val codecRegistryForChat: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForChat),
    DEFAULT_CODEC_REGISTRY
  )

  private val codecProviderForUser: CodecProvider = Macros.createCodecProvider[User]()
  private val codecRegistryForUser: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForUser),
    DEFAULT_CODEC_REGISTRY
  )

  private val codecProviderForGroup: CodecProvider = Macros.createCodecProvider[Group]()
  private val codecRegistryForGroup: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromProviders(codecProviderForGroup),
    DEFAULT_CODEC_REGISTRY
  )

  def getCodecRegistry[T](codecName: CodecNames): CodecRegistry = {

    codecName match{
      case USER => codecRegistryForUser
      case CHAT => codecRegistryForChat
      case GROUP => codecRegistryForGroup
    }
  }
}
