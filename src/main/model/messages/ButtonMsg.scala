package model.messages

import akka.actor.ActorRef
import javafx.collections.ObservableList
import javafx.stage.Stage
import model.ChatWrapper

trait ButtonMsg{}

final case class SendButtonMsg(message: String, listOfMessages: ObservableList[Message], sender: ActorRef )

final case class NewChatButtonMsg(listOfChats: ObservableList[ChatWrapper], chatName: String)

final case class RemoveChatButtonMsg(removeWho: ChatWrapper)

final case class ChatSelectedMSg(selected: ActorRef)

final case class SetupViewMsg()

final case class GUIAcknowledgeMsg(message: String, sender: ActorRef)

final case class UserSelected(userId: String, primaryStage: Stage)