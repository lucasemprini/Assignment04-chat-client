package model.messages

import model.ChatWrapper

final case class SendMessage(msg: String, chat: ChatWrapper)

final case class OKSendMessage(msg: String, chat: ChatWrapper)

final case class ErrorOnSendMessage()