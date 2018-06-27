package model.messages

import akka.actor.ActorRef
import model.ChatWrapper

final case class UserMsg(id: String)

final case class UserRes(user: User)

final case class ChatRes(chat: ChatWrapper)

final case class NewChatIdRes(chatId: String, chatName: String)

final case class UserChatsMsg(user: User, sender: ActorRef = null)

final case class AddChatToUserMsg(userId: String, chatId: String)

final case class OkAddChatToUserMsg()

final case class RemoveChatToUserMsg(userId: String, chatId: String)

final case class OkRemoveChatToUserMsg()

final case class GetChatMsg(id: String)

final case class ChatMsgRes(chat: Chat)

final case class GetChat(id: String)

final case class SetChatMsg(chat: Chat)

final case class OkSetChatMsg()

final case class GetNewChatId(chatName: String)

final case class ChatIdRes(chatId: String)

final case class SetUserMsg(user: User)

final case class OKSetUserMsg(user: User)

final case class ErrorUserReq(detail: String)

final case class ErrorChatsReq(detail: String)

final case class ErrorChatReq(detail: String)

final case class ErrorNewChatId(detail: String)

final case class ErrorSetUser(detail: String)

final case class ErrorSetChat(detail: String)

final case class ErrorAddChatToUser(detail: String)

final case class ErrorRemoveChatToUser(detail: String)
