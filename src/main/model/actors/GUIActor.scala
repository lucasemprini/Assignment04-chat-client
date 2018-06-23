package model.actors

import akka.actor.{Actor, ActorRef}
import javafx.collections.ObservableList
import javafx.scene.control.Label
import model.messages._

class GUIActor(val users: ObservableList[ActorRef], val mapOfChats: Map[ActorRef, ObservableList[String]],
               var currentChat: ObservableList[String], val actorLabel: Label) extends Actor {

  override def receive(): Receive = {
      case SendButtonMsg(message, listOfMessages, sender) => //TODO invio del messaggio. Come gestirlo?
      case NewChatButtonMsg(listOfChats, chatName) => //TODO nuova chat creata. Come gestirlo?
      case RemoveChatButtonMsg(removeWho)=> //TODO rimozione chat. Come gestirlo?
      case GUIShowMsg(msg, sender, prefix) => //TODO aggiungere alla view il nuovo messaggio. Come gestirlo?
      case ChatSelectedMSg(selected) => //TODO chat selezionata. Come gestirlo?
    }
}
