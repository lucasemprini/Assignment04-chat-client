package model

import akka.actor.ActorRef
import model.messages.{Chat, User}

import scala.util.Random

class ChatWrapper(val chatModel: Chat, var members: Seq[User], var actor: ActorRef, val debug: Boolean) {

  def this(chatModel: Chat, members: Seq[User], actorRef: ActorRef) =
    this(chatModel, members, actorRef, false)

  def this(chatModel: Chat, members: Seq[User]) = this(chatModel, members, ActorRef.noSender, false)

  def this(chatModel: Chat) = this(chatModel, Seq[User](), ActorRef.noSender, false)

  def addMember(user: User): Unit = members :+= user
}
