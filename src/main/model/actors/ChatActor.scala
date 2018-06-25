package model.actors

import akka.actor.Actor

class ChatActor(val chatName: String) extends Actor{
  override def receive: Receive = ???
}
