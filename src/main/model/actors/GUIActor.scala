package model.actors

import akka.actor.{Actor, ActorRef, Props}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.Label
import javafx.scene.paint.Color
import model.{ChatWrapper, Utility}
import model.messages._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class GUIActor(val chats: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ActorRef, ObservableList[Message]],
               var currentChat: ObservableList[Message], val actorLabel: Label, var currentUser: User,
               val restClient: ActorRef) extends Actor {

  override def receive(): Receive = {
    case SetupViewMsg() => restClient.tell(UserChatsMsg(currentUser, self), self)
    case ErrorUserReq(detail) => Utility.createErrorAlertDialog("User", detail)
    case UserRes(user) =>
      currentUser = user
      this.currentUser.chats.foreach(c => {
        this.restClient.tell(GetChatMsg(c), self)
      })
    case ErrorChatReq(detail) => Utility.createErrorAlertDialog("Chat", detail)
    case ChatRes(chatModelObject) =>
      this.chats.add(chatModelObject)
      this.mapOfChats += (chatModelObject.actor -> FXCollections.observableArrayList[Message]())
      chatModelObject.chatModel.getMessage.foreach(m => this.mapOfChats(chatModelObject.actor).add(m))

    case SendButtonMsg(message, listOfMessages, sender) =>
      Platform.runLater(() => {
        this.mapOfChats(sender).add(new Message(System.currentTimeMillis(), message, currentUser.getName))
      })

      //TODO this.restClient.nonLoSO -> Inviare un messaggio??
    case NewChatButtonMsg(_, chatName) =>
      restClient.tell(GetNewChatId(chatName), self)
    case ErrorNewChatId(detail) => Utility.createErrorAlertDialog("Chat", detail)
    case NewChatIdRes(chatId, chatName) =>
        val newChatModel = new Chat(chatId, chatName,ListBuffer.empty)
        this.restClient.tell(SetChatMsg(newChatModel), self)
    case ErrorSetChat(detail) => Utility.createErrorAlertDialog("Chat", detail)
    case OkSetChatMsg(chat) =>
      Platform.runLater(() => {
        val newChat = context.actorOf(Props(new ChatActor(chat.getTitle)), chat.getTitle)
        this.mapOfChats += (newChat -> FXCollections.observableArrayList[Message])
        this.chats.add(new ChatWrapper(chat, Seq(currentUser), newChat))

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
