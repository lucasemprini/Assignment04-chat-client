package model.actors

import akka.actor.{Actor, ActorRef, Props}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.Label
import javafx.scene.paint.Color
import model.ChatWrapper
import model.messages._

import scala.collection.mutable

class GUIActor(val users: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ActorRef, ObservableList[String]],
               var currentChat: ObservableList[String], val actorLabel: Label, val currentUser: User,
               val restClient: ActorRef) extends Actor {

  override def receive(): Receive = {
      case SendButtonMsg(message, listOfMessages, sender) =>
        Platform.runLater(() => {
          this.mapOfChats(sender).add(currentUser.getName + ": " + message)
        })
        //TODO notifica lo User con restClient
      case NewChatButtonMsg(_, chatName) =>
        Platform.runLater(() => {
          val newChat = context.actorOf(Props(new ChatActor(chatName)), chatName)
          //TODO notifica lo User con RestClient
          this.mapOfChats += (newChat -> FXCollections.observableArrayList[String])
          this.users.add(new ChatWrapper(chatName, Seq(currentUser.getId), newChat))
        })
      case RemoveChatButtonMsg(removeWho)=> Platform.runLater(() => {
        this.users.remove(removeWho)
        this.currentChat.clear()
        this.mapOfChats -= removeWho.actor
        //TODO notifica lo User con RestClient
        context.stop(removeWho.actor)
      })
      case ChatSelectedMSg(selected) =>
        Platform.runLater(() => {
            this.currentChat = mapOfChats(selected)
            this.actorLabel.setTextFill(Color.BLACK)
            this.actorLabel.setText("Write on chat \"" + selected.path.name + "\"!")
        })
    }
}
