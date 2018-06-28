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

class GUIActor(val chats: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ChatWrapper, ObservableList[Message]],
               var currentChat: ObservableList[Message], val actorLabel: Label, var currentUser: User,
               val restClient: ActorRef) extends Actor {

  override def receive(): Receive = {
    case SetupViewMsg() => restClient.tell(UserChatsMsg(currentUser, self), self)
    case ErrorUserReq(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("User", detail))
    case UserRes(user) =>
      currentUser = user
      this.currentUser.chats.foreach(c => {
        this.restClient.tell(GetChatMsg(c), self)
      })
    case ErrorChatReq(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case ChatRes(chatModelObject) =>
      Platform.runLater(() => {
        println("Chat presa da heroku! Membri: " + chatModelObject.members
          + chatModelObject.chatModel.getMessage)
        if(!this.chats.contains(chatModelObject)) this.chats.add(chatModelObject)
        if(!this.mapOfChats.isDefinedAt(chatModelObject)) {
          this.mapOfChats += (chatModelObject -> FXCollections.observableArrayList[Message]())
          chatModelObject.chatModel.getMessage.foreach(m => this.mapOfChats(chatModelObject).add(m))
        }
      })

    case SendButtonMsg(message, listOfMessages, sender) =>
      Platform.runLater(() => {
        this.mapOfChats(sender).add(new Message(System.currentTimeMillis(), message, currentUser.getName))
      })
    //TODO this.restClient.nonLoSO -> Inviare un messaggio??

    case NewChatButtonMsg(_, chatName) =>
      restClient.tell(GetNewChatId(chatName), self)
    case ErrorNewChatId(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case NewChatIdRes(chatId, chatName) =>
        val newChatModel = new Chat(chatId, chatName,ListBuffer.empty)
        this.restClient.tell(SetChatMsg(newChatModel), self)
    case ErrorSetChat(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkSetChatMsg(chat) =>
      Platform.runLater(() => {
        val newChatWrapper = new ChatWrapper(
          chat, Seq[User](currentUser), context.actorOf(Props(new ChatActor(chat.getTitle)), chat.getTitle))
        this.mapOfChats += (newChatWrapper -> FXCollections.observableArrayList[Message])
        this.chats.add(newChatWrapper)

      })

    case RemoveChatButtonMsg(removeWho)=> this.restClient.tell(RemoveChatToUserMsg(this.currentUser.getId, removeWho), self)
    case ErrorRemoveChatToUser(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkRemoveChatToUserMsg(chat) => Platform.runLater(() => {
      this.chats.remove(chat)
      this.currentChat.clear()
      this.mapOfChats -= chat
      //context.stop(removeWho.actor)
    })

    case ChatSelectedMSg(selected) =>
      Platform.runLater(() => {
          this.currentChat = mapOfChats(selected)
          this.actorLabel.setTextFill(Color.BLACK)
          this.actorLabel.setText("Write on chat \"" + selected.chatModel.getTitle + "\"!")
      })
    }
}
