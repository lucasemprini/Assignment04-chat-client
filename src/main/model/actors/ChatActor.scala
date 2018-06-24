package model.actors

import akka.actor.Actor

class ChatActor(val chatName: String, val members: Seq[String]) extends Actor{
  override def receive: Receive = ???
}
