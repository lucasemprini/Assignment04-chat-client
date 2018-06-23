package model.messages

import akka.actor.ActorRef
import javafx.collections.ObservableList

trait ButtonMsg{}

//TODO whoSends dovrebbe essere RestClient????
final case class SendButtonMsg(message: String, listOfMessages: ObservableList[String], sender: ActorRef )

final case class NewChatButtonMsg(chatName: String)

final case class RemoveChatButtonMsg(removeWho: ActorRef) //TODO Altra struttura dati per la Chat!!!

final case class GUIShowMsg(msg: String, sender: ActorRef, prefix: String) //TODO sender dovrebbe essere RestClient????

final case class ChatSelectedMSg(selected: ActorRef) //TODO Altra struttura dati per la Chat!!!