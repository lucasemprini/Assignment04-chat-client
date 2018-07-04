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
import model.actors.GUIActor.image
import model.messages._
import model.{ChatWrapper, Log, Utility}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object GUIActor {
  val image: Image = new Image("/chat.png")
}


class GUIActor(val chats: ObservableList[ChatWrapper], var mapOfChats: mutable.Map[ChatWrapper, ObservableList[Message]],
               var currentChat: ObservableList[Message], val actorLabel: Label, var currentUser: User,
               val restClient: ActorRef) extends Actor {

  //TODO / Ci ho messo le mani... poi ti spiego ahah
  /*if (SystemTray.isSupported) {
    val tray = SystemTray.getSystemTray

    trayIcon.setImageAutoSize(true)

    try {
      tray.add(trayIcon)
    } catch {
      case ex: Exception => println(ex.printStackTrace())
    }
  }*/

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

      //TODO / Ci ho messo le mani
      //Ho fatto in modo che all'ottenimento di una chat viene impostato il suo corrispondente chat actor a cui passo anche la funzione da esegui
      //quando quella chat riceve un messaggio
      chatModelObject.actor = context.actorOf(Props(new ChatActor(chatModelObject.chatModel.getId,
        chatModelObject.chatModel.getTitle, currentUser, (msg, sender) => {
          Platform.runLater(() => {
            var popupMsg: String = ""
            if (msg.length <= 20) {
              popupMsg = msg
            } else {
              popupMsg = msg.substring(0, 20).concat("...")
            }
            val tray = new TrayNotification("New message! Chat: " + chatModelObject.chatModel.getTitle, msg, Notifications.INFORMATION)
            tray.setAnimation(Animations.POPUP)
            tray.setImage(image)
            tray.showAndDismiss(Duration.seconds(4))
            //trayIcon.displayMessage("New message! Chat: " + chatModelObject.chatModel.getTitle, msg, TrayIcon.MessageType.NONE)
            this.mapOfChats(chatModelObject).add(new Message(System.currentTimeMillis(), msg, sender))
          })
        })))
      Platform.runLater(() => {

        if (!this.chats.contains(chatModelObject)) this.chats.add(chatModelObject)
        if (!this.mapOfChats.isDefinedAt(chatModelObject)) {
          this.mapOfChats += (chatModelObject -> FXCollections.observableArrayList[Message]())
          chatModelObject.chatModel.getMessage.foreach(m => this.mapOfChats(chatModelObject).add(m))
        }
      })

    //TODO / Ci ho messo le mani
    case SendButtonMsg(message, listOfMessages, sender) =>
      sender.actor ! SendMessage(message, sender)

    //TODO / Ci ho messo le mani
    //E' giusto non fare nulla perchè il messaggio sarà ricevuto dal subscriber che lo gestirà e lo visualizza,
    //Si avrebbe lo stesso messaggio due volte se gestito anche qui l'invio
    case OKSendMessage(message, chat) =>
      Log.debug("Messaggio correttamente inviato")


    //TODO / Ci ho messo le mani
    case ErrorOnSendMessage() =>
      println("ERROR / Impossibile inviare il messaggio")

    case NewChatButtonMsg(_, chatName) =>
      restClient.tell(GetNewChatId(chatName), self)
    case ErrorNewChatId(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case NewChatIdRes(chatId, chatName) =>
      val newChatModel = new Chat(chatId, chatName, ListBuffer.empty)
      this.restClient.tell(SetChatMsg(newChatModel), self)
    case ErrorSetChat(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkSetChatMsg(chat) =>
      this.restClient ! AddChatToUserMsg(currentUser.getId, chat.getId)
      Platform.runLater(() => {
        //TODO / Ci ho messo le mani
        //Ho modificato la creazione del chat actor
        val newChatWrapper = new ChatWrapper(chat, Seq[User](currentUser))
        newChatWrapper.actor = context.actorOf(Props(new ChatActor(chat.getId, chat.getTitle, currentUser, (msg, msgSender) => {
          Platform.runLater(() => {
            var popupMsg: String = ""
            if (msg.length <= 30) {
              popupMsg = msg
            } else {
              popupMsg = msg.substring(0, 27).concat("...")
            }
            val tray = new TrayNotification("New message! Chat: " + newChatWrapper.chatModel.getTitle, msg, Notifications.INFORMATION)
            tray.setAnimation(Animations.POPUP)
            tray.setImage(image)
            tray.showAndDismiss(Duration.seconds(4))            this.mapOfChats(newChatWrapper).add(new Message(System.currentTimeMillis(), msg, msgSender))
          })
        })))
        this.mapOfChats += (newChatWrapper -> FXCollections.observableArrayList[Message])
        this.chats.add(newChatWrapper)

      })

    case JoinButtonMsg(toJoin) =>
      //TODO NOTIFICARE IL SERVER DELL'AGGIUNTA DI UN MEMBRO
      toJoin.members :+ currentUser

    case RemoveChatButtonMsg(removeWho) => this.restClient.tell(RemoveChatToUserMsg(this.currentUser.getId, removeWho), self)
    case ErrorRemoveChatToUser(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chat", detail))
    case OkRemoveChatToUserMsg(chat, _, _) => Platform.runLater(() => {
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
