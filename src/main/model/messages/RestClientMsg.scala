package model.messages

import akka.actor.ActorRef
import model.ChatWrapper

final case class UserMsg(id: String)

final case class UserRes(user: User)

final case class ChatRes(chat: ChatWrapper)

final case class NewChatIdRes(chatId: String, chatName: String)

final case class UserChatsMsg(user: User, sender: ActorRef = null)

final case class AddChatToUserMsg(user: User, chat: ChatWrapper, joining: Boolean)

final case class OkAddChatToUserMsg(addChatToUserDetails: String, addUserToChatDetails: String, chat: ChatWrapper)

final case class RemoveChatToUserMsg(userId: String, chat: ChatWrapper)

final case class OkRemoveChatToUserMsg(chat: ChatWrapper, remChatToUserDetails: String, remUserToChatDetails: String)

final case class GetChatMsg(id: String)

final case class ChatMsgRes(chat: Chat)

final case class GetChat(id: String)

final case class SetChatMsg(chat: Chat, user: User)

final case class OkSetChatMsg(chat: ChatWrapper)

final case class GetNewChatId(chatName: String)

final case class ChatIdRes(chatId: String)

final case class GetAllChats()

final case class OKGetAllChats(chatsId: Seq[String])

final case class SetUserMsg(user: User)

final case class OKSetUserMsg(user: User)

trait Error {
  def detail: String
}

final case class ErrorUserReq(override val detail: String) extends Error

final case class ErrorChatsReq(override val detail: String) extends Error

final case class ErrorChatReq(override val detail: String) extends Error

final case class ErrorNewChatId(override val detail: String) extends Error

final case class ErrorGetAllChats(override val detail: String) extends Error

final case class ErrorSetUser(override val detail: String) extends Error

final case class ErrorSetChat(override val detail: String) extends Error

final case class ErrorAddChatToUser(override val detail: String) extends Error

final case class ErrorRemoveChatToUser(override val detail: String) extends Error
