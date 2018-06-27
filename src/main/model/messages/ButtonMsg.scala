package model.messages

import akka.actor.ActorRef
import javafx.collections.ObservableList
import javafx.stage.Stage
import model.ChatWrapper
import view.MainViewController

trait ButtonMsg{}

//TODO whoSends dovrebbe essere RestClient????
final case class SendButtonMsg(message: String, listOfMessages: ObservableList[Message], sender: ActorRef )

final case class NewChatButtonMsg(listOfChats: ObservableList[ChatWrapper], chatName: String)

final case class RemoveChatButtonMsg(removeWho: ChatWrapper)

final case class GUIShowMsg(msg: String, sender: ActorRef, prefix: String) //TODO sender dovrebbe essere RestClient????

final case class ChatSelectedMSg(selected: ActorRef) //TODO Altra struttura dati per la Chat!!!

final case class SetupViewMsg()

final case class GUIAcknowledgeMsg(message: String, sender: ActorRef)

final case class UserSelected(userId: String, primaryStage: Stage)