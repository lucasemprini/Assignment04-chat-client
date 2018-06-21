package model.messages

import scala.collection.mutable.ListBuffer

trait RestObject {}

class User(id: String,
                name: String) extends RestObject {
  def this(user: User) = this(user.getId(), user.getName())
  val chats: ListBuffer[String] = ListBuffer()

  def getId() = id

  def getName() = name

  def addChat(chat: String) = chats += chat
}
