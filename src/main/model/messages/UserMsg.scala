package model.messages

import akka.actor.ActorRef

final case class UserMsg(id: String)

final case class UserChatsMsg(user: User, sender: ActorRef = null)
