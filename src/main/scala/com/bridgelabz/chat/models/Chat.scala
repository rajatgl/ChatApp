package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{StringJsonFormat, jsonFormat1, jsonFormat3, seqFormat}
import spray.json.RootJsonFormat

final case class Chat(sender: String, receiver: String, message: String)
trait ChatJsonSupport {
  implicit val chatFormat: RootJsonFormat[Chat] = jsonFormat3(Chat)
}

case class SeqChat(seqChat: Seq[Chat])
trait SeqChatJsonSupport extends ChatJsonSupport {
  implicit val seqChatFormat: RootJsonFormat[SeqChat] = jsonFormat1(SeqChat)
}
