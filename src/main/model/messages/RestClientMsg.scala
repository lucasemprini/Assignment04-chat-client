package model.messages

import akka.actor.ActorRef

final case class UserMsg(id: String)

final case class UserRes(user: User)

final case class UserChatsMsg(user: User, sender: ActorRef = null)

final case class GetChatMsg(id: String)

final case class GetNewChatId()

final case class SetUserMsg(user: User)

final case class OKSetUserMsg(user: User)

final case class ErrorUserReq(detail: String)

final case class ErrorChatsReq(detail: String)

final case class ErrorChatReq(detail: String)

final case class ErrorNewChatId(detail:String)

final case class ErrorSetUser(detail:String)
