package model

import akka.actor.ActorRef
import model.messages.{Chat, User}

import scala.util.Random

class ChatWrapper(val chatName: String, val chatModel: Chat, val members: Seq[User], val actor: ActorRef, val debug: Boolean) {

  def this(chatName: String, chatModel: Chat, members: Seq[User], actorRef: ActorRef) =
    this(chatName, chatModel, members, actorRef, false)

  def this(chatName: String, chatModel: Chat, members: Seq[User]) = this(chatName, chatModel, members, ActorRef.noSender, false)

  def this(chatModel: Chat, members: Seq[User]) = this("NewChat" + Random.nextInt(), chatModel, members, ActorRef.noSender, false)

  def this(chatName: String, chatModel: Chat) = this(chatName, chatModel, Seq[User](), ActorRef.noSender, false)

  def this(chatModel: Chat) = this("NewChat" + Random.nextInt(), chatModel, Seq[User](), ActorRef.noSender, false)


}
