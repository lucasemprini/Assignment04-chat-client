package model.messages

import io.vertx.core.json.Json

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RestObject {

  def toJsonString: String = {
    Json.encode(this)
  }

  def queryParams: Map[String, String] = {
    val map: mutable.HashMap[String, String] = mutable.HashMap()

    this.getClass.getMethods
      .filter(m => m.getName.startsWith("get"))
      .filter(m => !m.getName.equals("getClass"))
      .filter(m => !m.getName.equals("getId"))
      .foreach(m => {
        val key = m.getName.replaceFirst("get", "").toLowerCase
        val value = m.invoke(this)
        map.put(key, value.toString)
      })

    map.toMap
  }
}

class User(id: String,
           name: String) extends RestObject {
  def this(user: User) = this(user.getId, user.getName)

  val chats: ListBuffer[String] = ListBuffer()

  def getId: String = id

  def getName: String = name

  def addChat(chat: String): Unit = chats += chat

  override def toString: String = name
}

class Message(timestamp: Long,
              msg: String,
              sender: String) extends RestObject {

  def getTimestamp: Long = timestamp

  def getMsg: String = msg

  def getSender: String = sender
}

class Chat(id: String,
           title: String,
           messages: ListBuffer[Message]) extends RestObject {

  def getId: String = id

  def getTitle: String = title

  def getMessage: ListBuffer[Message] = messages

  override def queryParams: Map[String, String] = {
    val map: mutable.HashMap[String, String] = mutable.HashMap()

    map.put("title", getTitle)
    map.toMap
  }
}
