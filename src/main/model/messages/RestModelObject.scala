package model.messages

import io.vertx.core.json.Json

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RestObject {

  def toJsonString: String = {
    Json.encode(this)
  }

  def queryParams: Unit = {
    val map: mutable.HashMap[String, String] = mutable.HashMap()

    this.getClass.getMethods
      .filter(m => m.getName.startsWith("get"))
      .filter(m => !m.getName.equals("getClass"))
      .foreach(m => {
        val key = m.getName.replaceFirst("get", "").toLowerCase
        val value = m.invoke(this)

        println("Key: " + key + " / value: " + value)
      })
  }
}

class User(id: String,
           name: String) extends RestObject {
  def this(user: User) = this(user.getId, user.getName)

  val chats: ListBuffer[String] = ListBuffer()

  def getId: String = id

  def getName: String = name

  def addChat(chat: String): Unit = chats += chat
}

class Message(timestamp: Long,
              msg: String,
              sender: String) extends RestObject {

  def getTimestamp: Long = timestamp

  def getMsg: String = msg

  def getSender: String = sender
}

class Chat(id: String,
           messages: ListBuffer[Message]) extends RestObject {

  def getId: String = id

  def getMessage: ListBuffer[Message] = messages
}

class NewChatId(id: Int) {
  def getId: String = id.toString
}