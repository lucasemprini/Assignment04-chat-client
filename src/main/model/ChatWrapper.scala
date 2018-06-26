package model

import akka.actor.ActorRef

class ChatWrapper(val chatName: String, val members: Seq[String], val actor:ActorRef) {

}
