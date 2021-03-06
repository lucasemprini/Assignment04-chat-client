package model.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import io.vertx.lang.scala.json.Json
import model.ChatWrapper
import model.actors.ChatActor.{HOST, PASSWORD, PORT}
import model.actors.RestClient.{MSG, SENDER}
import model.messages.{ErrorOnSendMessage, OKSendMessage, SendMessage, User}
import redis.RedisClient
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}

import scala.concurrent.ExecutionContext.Implicits.global

object ChatActor {
  var HOST: String = ""
  var PORT: Int = 0
  var PASSWORD: Option[String] = Some("")
}

class ChatActor(val chatId: String, val chatName: String, val user: User,
                val onMessageReceived: (String, String) => Unit,
                val onChatDeleted: String => Unit) extends Actor {

  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()

  val chatChannel: String = "chat." + chatId
  val deletedChat: String = "chatDeleted." + chatId
  val channels = Seq(chatChannel, deletedChat)
  val patterns = Seq()


  HOST = System.getenv("REDIS_HOST")
  PORT = System.getenv("REDIS_PORT").toInt
  PASSWORD = Some(System.getenv("REDIS_PW"))

  akkaSystem.actorOf(Props(classOf[SubscribeActor], channels, patterns, onMessageReceived, onChatDeleted))

  override def receive: Receive = {
    case SendMessage(msg: String, chat: ChatWrapper) =>
      val redis = RedisClient(HOST, PORT, PASSWORD)

      try {
        val complexMsg = Json.emptyObj()
        complexMsg.put(SENDER, user.getId)
        complexMsg.put(MSG, msg)
        redis.publish(chatChannel, complexMsg.encode()).map(_ => {
          redis.quit().map(_ => {
            redis.stop()
          })
        })
        sender() ! OKSendMessage(msg, chat)
      }
      catch {
        case ex: Throwable =>
          println(ex.getMessage)
          sender() ! ErrorOnSendMessage()
      }

  }
}


class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil,
                     onMessageReceived: (String, String) => Unit,
                     onChatDeleted: String => Unit)
  extends RedisSubscriberActor(
    new InetSocketAddress(HOST, PORT),
    channels,
    patterns,
    PASSWORD,
    onConnectStatus = connected => {
      println(s"connected: $connected")
    }) {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()


  def onMessage(message: Message) {

    if (message.channel.startsWith("chatDeleted")) {
      onChatDeleted(message.data.utf8String)
    } else {
      val complexMsg = Json.fromObjectString(message.data.utf8String)
      var sender = complexMsg.getString(SENDER)
      if (sender == null) sender = "unknown"
      val msg = complexMsg.getString(MSG)

      onMessageReceived(msg, sender)
    }


  }

  def onPMessage(pmessage: PMessage) {
    println("ERROR / Why received pattern msg? -> " + pmessage.data.utf8String)
  }


}

