package model.messages

import akka.actor.ActorRef
import javafx.collections.ObservableList
import model.Chat

trait ButtonMsg{}

//TODO whoSends dovrebbe essere RestClient????
final case class SendButtonMsg(message: String, listOfMessages: ObservableList[String], sender: ActorRef )

final case class NewChatButtonMsg(listOfChats: ObservableList[Chat], chatName: String)

final case class RemoveChatButtonMsg(removeWho: Chat)

final case class GUIShowMsg(msg: String, sender: ActorRef, prefix: String) //TODO sender dovrebbe essere RestClient????

final case class ChatSelectedMSg(selected: ActorRef) //TODO Altra struttura dati per la Chat!!!

final case class CanExit(removeWho: Chat)

final case class GUIAcknowledgeMsg(message: String, sender: ActorRef)