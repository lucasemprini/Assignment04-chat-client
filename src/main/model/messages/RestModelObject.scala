package model.messages

import java.text.SimpleDateFormat

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

  val chats: mutable.Set[String] = mutable.HashSet()

  def getId: String = id

  def getName: String = name

  def addChat(chat: String): Unit = chats += chat

  def canEqual(a: Any): Boolean = a.isInstanceOf[User]

  override def equals(that: Any): Boolean =
    that match {
      case that: User => that.canEqual(this) && this.id == that.getId && this.name == that.getName && this.chats == that.chats
      case _ => false
    }

  override def toString: String = name

}

class Message(timestamp: Long,
              msg: String,
              sender: String) extends RestObject {

  def getTimestamp: Long = timestamp

  def getMsg: String = msg

  def getSender: String = sender

  override def toString: String = "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp) + "] " + sender + ": " + msg
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
