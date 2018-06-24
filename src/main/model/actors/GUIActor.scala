package model.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.Label
import javafx.scene.paint.Color
import model.messages._

import scala.collection.mutable

class GUIActor(val users: ObservableList[ActorRef], var mapOfChats: mutable.Map[ActorRef, ObservableList[String]],
               var currentChat: ObservableList[String], val actorLabel: Label) extends Actor {

  override def receive(): Receive = {
      case SendButtonMsg(message, listOfMessages, sender) => //TODO invio del messaggio. Come gestirlo?
      case NewChatButtonMsg(_, chatName) =>
        Platform.runLater(() => {
            val newChat = context.actorOf(Props(new ChatActor(chatName)), chatName)
            //registry.tell(new NewChatButtonMsg(chatName), newChat)
            //newActor.tell(new StartChatMsg(registry, getSelf), ActorRef.noSender)

            this.mapOfChats += (newChat -> FXCollections.observableArrayList[String])

            val start = users.size == 0
            this.users.add(newChat)
            //if (start) newChat.tell(new TakeToken(0), ActorRef.noSender)
            //TODO GESTIONE DEL REGISTRY???
        })
      case RemoveChatButtonMsg(removeWho)=> //TODO rimozione chat. Come gestirlo?
      case CanExit(removeWho) => Platform.runLater(() => {
          this.users.remove(removeWho)
          this.currentChat.clear()
          this.mapOfChats -= removeWho
          context.stop(removeWho)
      })
      case GUIShowMsg(msg, sender, prefix) => //TODO aggiungere alla view il nuovo messaggio. Come gestirlo?
      case ChatSelectedMSg(selected) =>
        Platform.runLater(() => {
            this.currentChat = mapOfChats(selected)
            this.actorLabel.setTextFill(Color.BLACK)
            this.actorLabel.setText("Write on chat " + selected.path.name + "!")
        })
    }
}
