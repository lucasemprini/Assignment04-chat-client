package model.actors

import akka.actor.{Actor, ActorRef, Props}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.Label
import javafx.scene.paint.Color
import model.{ChatWrapper, Utility}
import model.messages._

import scala.collection.mutable

class GUIActor(val chats: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ActorRef, ObservableList[String]],
               var currentChat: ObservableList[String], val actorLabel: Label, var currentUser: User,
               val restClient: ActorRef) extends Actor {

  override def receive(): Receive = {
    case SetupViewMsg() => restClient.tell(UserChatsMsg(currentUser, self), self)
    case UserRes(user) => {
      currentUser = user
      //TODO this.chats.addAll(user.chats)
      this.chats.forEach(c => {
        this.restClient.tell(GetChatMsg(c.chatName), self)
      })
    }
    case ChatRes(chatModelObject) =>
      /* TODO
      * this.chats.add(chatModelObject)
      * this.mapOfChats += (chatModelObject -> FXCollections.checkedObservableList(chatModelObject.getMessage))
      */
    case ErrorChatReq(detail) => Utility.createErrorAlertDialog("Chat", detail)
    case SendButtonMsg(message, listOfMessages, sender) =>
      Platform.runLater(() => {
        this.mapOfChats(sender).add(currentUser.getName + ": " + message)
      })
      //TODO this.restClient.nonLoSO -> Inviare un messaggio??
    case NewChatButtonMsg(_, chatName) =>
      Platform.runLater(() => {
        val newChat = context.actorOf(Props(new ChatActor(chatName)), chatName)
        //TODO notifica lo User con RestClient -> Creazione di una nuova Chat??
        this.mapOfChats += (newChat -> FXCollections.observableArrayList[String])
        this.chats.add(new ChatWrapper(chatName, Seq(currentUser.getId), newChat))
      })
    case RemoveChatButtonMsg(removeWho)=> Platform.runLater(() => {
      this.chats.remove(removeWho)
      this.currentChat.clear()
      this.mapOfChats -= removeWho.actor
      //TODO notifica lo User con RestClient -> Rimuovere una Chat??
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
