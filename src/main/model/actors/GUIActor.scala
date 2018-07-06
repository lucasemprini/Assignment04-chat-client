package model.actors


import akka.actor.{Actor, ActorRef, Props}
import com.github.plushaze.traynotification.animations.Animations
import com.github.plushaze.traynotification.notification.{Notifications, TrayNotification}
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.util.Duration
import model.ChatWrapper
import model.actors.GUIActor.image
import model.messages._
import model.utility.{Log, Utility}
import view.{LoadingDialog, MainViewController}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object GUIActor {
  val image: Image = new Image("/chat.png")
  private val LABEL_JOIN_TEXT = "Join this chat!"
}


class GUIActor(var chats: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ChatWrapper, ObservableList[Message]],
               var currentChat: ObservableList[Message], val actorLabel: Label, var currentUser: User,
               val restClient: ActorRef) extends Actor {

  private val loadingDialog: LoadingDialog = new LoadingDialog

  override def receive(): Receive = {
    case SetupViewMsg() =>
      restClient.tell(UserChatsMsg(currentUser, self), self)
      restClient ! GetAllChats()
      Utility.setUpDialog(loadingDialog)
      Utility.showDialog(loadingDialog)
    case ErrorUserReq(detail) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => Utility.createErrorAlertDialog("User", detail))
    case UserRes(user) =>
      currentUser = user

    case OKGetAllChats(chatsId) =>
      chatsId foreach (chatId => {
        restClient ! GetChatMsg(chatId)
      })

    case ErrorChatReq(detail) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case ChatRes(chatModelObject) =>
      Utility.closeDialog(loadingDialog)
      chatModelObject.actor = createChatActor(chatModelObject)
      Platform.runLater(() => {

        if (!this.chats.contains(chatModelObject)) this.chats.add(chatModelObject)
        if (!this.mapOfChats.isDefinedAt(chatModelObject)) {
          this.mapOfChats += (chatModelObject -> FXCollections.observableArrayList[Message]())
          chatModelObject.chatModel.getMessage.foreach(m => this.mapOfChats(chatModelObject).add(m))
        }
      })

    case SendButtonMsg(message, listOfMessages, sender) =>
      Utility.showDialog(loadingDialog)
      sender.actor ! SendMessage(message, sender)

    case OKSendMessage(message, chat) =>
      Log.debug("Messaggio correttamente inviato")
      Utility.closeDialog(loadingDialog)

    case ErrorOnSendMessage() =>
      println("ERROR / Impossibile inviare il messaggio")
      Utility.closeDialog(loadingDialog)

    case NewChatButtonMsg(_, chatName) =>
      Utility.showDialog(loadingDialog)
      restClient.tell(GetNewChatId(chatName), self)
    case ErrorNewChatId(detail) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case NewChatIdRes(chatId, chatName) =>
      val newChatModel = new Chat(chatId, chatName, ListBuffer.empty)
      this.restClient.tell(SetChatMsg(newChatModel, currentUser), self)
    case ErrorSetChat(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkSetChatMsg(chat) =>
      this.restClient ! AddChatToUserMsg(currentUser, chat)
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => {
        chat.actor = createChatActor(chat)
        this.mapOfChats += (chat -> FXCollections.observableArrayList[Message])
        this.chats.add(chat)

      })

    case JoinButtonMsg(toJoin) =>
      Utility.showDialog(loadingDialog)
      restClient ! AddChatToUserMsg(currentUser, toJoin)
    case ErrorAddChatToUser(detail) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkAddChatToUserMsg(_, _, chat) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => chat.members :+ currentUser)

    case RemoveChatButtonMsg(removeWho) =>
      Utility.showDialog(loadingDialog)
      this.restClient.tell(RemoveChatToUserMsg(this.currentUser.getId, removeWho), self)
    case ErrorRemoveChatToUser(detail) =>
      Utility.closeDialog(loadingDialog)
      Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkRemoveChatToUserMsg(chat, _, _) =>
      Utility.closeDialog(loadingDialog)
      currentUser.chats -= chat.chatModel.getId
      //TODO rimuovere solo dalla chat dell'utente


    case ChatSelectedMSg(selected, isMine) =>
      Platform.runLater(() => {
        this.currentChat = mapOfChats(selected)
        if(isMine) {
          this.actorLabel.setTextFill(Color.BLACK)
          this.actorLabel.setText("Write on chat \"" + selected.chatModel.getTitle + "\"!")
        } else {
          this.actorLabel.setTextFill(Color.BLACK)
          this.actorLabel.setText(GUIActor.LABEL_JOIN_TEXT)
        }
      })
    case UpdateObservable(listOfChats) => this.chats = listOfChats
  }

  def createChatActor(chat: ChatWrapper): ActorRef = {
    context.actorOf(Props(new ChatActor(chat.chatModel.getId,
      chat.chatModel.getTitle, currentUser, (msg, sender) => {
        Platform.runLater(() => {
          if (!sender.equals(currentUser.getId)) {
            var popupMsg: String = ""
            if (msg.length <= 20) {
              popupMsg = msg
            } else {
              popupMsg = msg.substring(0, 20).concat("...")
            }
            val tray = new TrayNotification("New message! Chat: "
              + chat.chatModel.getTitle,
              sender + ": " + msg, Notifications.INFORMATION)
            tray.setAnimation(Animations.POPUP)
            tray.setImage(image)
            tray.showAndDismiss(Duration.seconds(4))
          }
          mapOfChats(chat).add(new Message(System.currentTimeMillis(), msg, sender))
        })
      }, chatId => {
        mapOfChats.keys.foreach(chat => {
          if (chat.chatModel.getId.equals(chatId)) {
            Platform.runLater(() => {
              this.chats.remove(chat)
              this.currentChat.clear()
              this.mapOfChats -= chat
              context.stop(chat.actor)
              val tray = new TrayNotification("Cancellata la chat: " + chatId + "/" + chat.chatModel.getTitle,
                "Tutti gli utenti si sono ritirati, la chat è stata eliminata", Notifications.NOTICE)
              tray.setAnimation(Animations.POPUP)
              tray.showAndDismiss(Duration.seconds(4))
            })
          }
        })
      })))
  }
}
