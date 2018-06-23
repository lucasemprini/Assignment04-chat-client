package model.actors

import java.awt.Label

import akka.actor.{AbstractActor, ActorRef}
import javafx.collections.ObservableList
import model.messages._

class GUIActor(users: ObservableList[ActorRef], currentChat: ObservableList[String],
               registry: ActorRef, mapOfChats: Map[ActorRef, ObservableList[String]],
               actorLabel: Label) extends AbstractActor {

  override def createReceive(): Receive = {
      case SendButtonMsg(message, listOfMessages, sender) => //TODO invio del messaggio. Come gestirlo?
      case NewChatButtonMsg(chatName) => //TODO nuova chat creata. Come gestirlo?
      case RemoveChatButtonMsg(removeWho)=> //TODO rimozione chat. Come gestirlo?
      case GUIShowMsg(msg, sender, prefix) => //TODO aggiungere alla view il nuovo messaggio. Come gestirlo?
      case ChatSelectedMSg(selected) => //TODO chat selezionata. Come gestirlo?
    }
}
