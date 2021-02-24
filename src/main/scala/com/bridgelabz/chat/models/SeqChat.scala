package com.bridgelabz.chat.models

import com.bridgelabz.chat.Routes.{jsonFormat1, seqFormat}
import spray.json.RootJsonFormat

case class SeqChat(seqChat: Seq[Chat])

trait SeqChatJsonSupport extends ChatJsonSupport {
  implicit val seqChatFormat: RootJsonFormat[SeqChat] = jsonFormat1(SeqChat)
}