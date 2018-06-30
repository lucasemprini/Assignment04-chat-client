package model.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.WebClient
import model.actors.RestClient._
import model.messages._
import model.{ChatWrapper, Log}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object RestClient {
  def props(): Props = Props(new RestClient)

  val URL_PREFIX = "https://assignment04-chat-server.herokuapp.com"
  val URL = "localhost"//assignment04-chat-server.herokuapp.com"
  val PORT = 4700
  val RESULT = "result"
  val DETAILS = "details"
  val CHAT: String = "chat"
  val TIMESTAMP: String = "timestamp"
  val MSG: String = "msg"
  val SENDER: String = "sender"
  val ID: String = "id"
  val TITLE: String = "title"
  val MEMBERS: String = "members"
  val NAME: String = "name"
}


class RestClient extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val execeutionContext: ExecutionContextExecutor = system.dispatcher

  /**
    * Risponde a:
    *   - UserMsg(id)
    * Esegue una GET /user/:id
    * POS: Non risponde direttamente ma manda un messaggio a se stesso in ricerca delle chat dell'utente trovato
    * NEG: Risponde con un ErrorUserReq(details) fornendo i dettagli che hanno causato l'errore
    *
    *   - UserChatsMsg(msgUser, sender)
    * Esegue una GET /user/:id/chats
    * POS: Risponde con un messaggio UserRes(user) contenente l'utente compilato con i propri dati e le sue chats
    * NEG: Risponde con un ErrorUserReq o ErrorChatsReq in base che chi ha chiamato la ricerca cercasse l'utente o la sua lista di chat.
    *
    *   - GetChatMsg(chatId)
    * Esegue una GET /chats/:id
    * POS: Risponde con un messaggio Chat contente l'id della chat e una lista di messaggio
    * NEG: Risponde con un messaggio ErrorChatReq(details)
    *
    *   - SetUserMsg(user)
    * Esegue un POST /user/:id
    * POS: Risponde con un messaggio OkSetUserMsg, ciò significa che è andato tutto bene
    * NEG: Risponde con un messaggio ErrorSetUser(details)
    *
    * @return
    */
  override def receive: Receive = {
    //USER
    case UserMsg(id) =>
      val actSender: ActorRef = sender()

      GETReq("/user/" + id, resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        val data = new JsonObject(body)

        if (data.getBoolean(RESULT)) {
          val jsonUser = data.getJsonObject("user")
          val user = new User(id, jsonUser.getString("name"))
          self ! UserChatsMsg(user, actSender)
        } else {
          actSender ! ErrorUserReq(data.getString(DETAILS))
        }
      }, failRes => {
        actSender ! ErrorUserReq("Errore nella comunicazione con il server: " + failRes.getMessage)
      })

    case UserChatsMsg(msgUser, sender) =>
      val fromUser: Boolean = if (sender == null) false else true
      val actSender: ActorRef = if (sender == null) self else sender
      val user: User = new User(msgUser)
      GETReq("/user/" + user.getId + "/chats", resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        val data = Json.fromObjectString(body)
        if (data.getBoolean(RESULT)) {
          data.getJsonArray("chats").getList.forEach(e => user.addChat(e.toString))
          actSender ! UserRes(user)
        } else {
          if (fromUser) {
            actSender ! ErrorUserReq(data.getString(DETAILS))
          } else {
            actSender ! ErrorChatsReq(data.getString(DETAILS))
          }
        }
      }, failRes => {
        if (fromUser) {
          actSender ! ErrorUserReq("Errore nella comunicazione con il server: " + failRes.getMessage)
        } else {
          actSender ! ErrorChatsReq("Errore nella comunicazione con il server: " + failRes.getMessage)
        }
      })

    case SetUserMsg(user) =>
      val actSender: ActorRef = sender()
      POSTReq("/user/" + user.getId, user.queryParams, resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        if (Json.fromObjectString(body).getBoolean(RESULT)) {
          actSender ! OKSetUserMsg
        } else {
          actSender ! ErrorSetUser("Errore durante il salvataggio dei dati dell'utente: " + user.getId)
        }
      }, cause => {
        cause.printStackTrace()
        actSender ! ErrorSetUser("Errore durante il salvataggio dei dati dell'utente: " + user.getId)
      })

    case AddChatToUserMsg(userId, chatId) =>
      val actSender: ActorRef = sender()
      val params = new mutable.HashMap[String, String]()
      params.put("chat", chatId)
      POSTReq("/user/" + userId + "/chats", params.toMap, resBody => {
        val body = Json.fromObjectString(resBody.bodyAsString().getOrElse(""))
        var addMemberDetails: String = ""
        try {
          if (!body.getBoolean(RESULT + "_chats")) {
            addMemberDetails = body.getString(DETAILS + "_chats")
          }
        } catch {
          case _: Exception => addMemberDetails = ""
        }

        var addChatDetails: String = ""
        if (!body.getBoolean(RESULT)) {
          addChatDetails = body.getString(DETAILS)
        }

        actSender ! OkAddChatToUserMsg(addChatDetails, addMemberDetails)
      }, _ => {
        actSender ! ErrorAddChatToUser("Impossibile associare la chat: " + chatId + " all'utente: " + userId)
      })

    case RemoveChatToUserMsg(userId, chat) =>
      val actSender: ActorRef = sender()
      val params = new mutable.HashMap[String, String]()
      params.put("chat", chat.chatModel.getId)
      POSTReq("/user/" + userId + "/removeChats", params.toMap, resBody => {
        val body = Json.fromObjectString(resBody.bodyAsString().getOrElse(""))

        var remMemberDetails: String = ""
        try {
          if (!body.getBoolean(RESULT + "_chats")) {
            remMemberDetails = body.getString(DETAILS + "_chats")
          }
        } catch {
          case _: Exception => remMemberDetails = ""
        }

        var remChatDetails: String = ""
        if (!body.getBoolean(RESULT)) {
          remChatDetails = body.getString(DETAILS)
        }

        actSender ! OkRemoveChatToUserMsg(chat, remChatDetails, remMemberDetails)
      }, _ => {
        actSender ! ErrorRemoveChatToUser("Impossibile ciao associare la chat: " + chat.chatModel.getId + " all'utente: " + userId)
      })

    //CHAT
    case GetChatMsg(chatId) =>
      val actSender: ActorRef = sender()
      val messages: ListBuffer[Message] = ListBuffer()
      var users: Seq[User] = Seq()

      GETReq("/chats/" + chatId, resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        val data: JsonObject = Json.fromObjectString(body)
        if (data.getBoolean(RESULT)) {
          val title = data.getString(TITLE)
          data.getJsonArray(MEMBERS) forEach (jsonUser => {
            val user: JsonObject = Json.fromObjectString(jsonUser.toString)
            users = users :+ new User(user.getString(ID), user.getString(NAME))
          })
          data.getJsonArray(CHAT) forEach (jsonMsg => {
            val msg: JsonObject = Json.fromObjectString(jsonMsg.toString)
            messages += new Message(msg.getLong(TIMESTAMP), msg.getString(MSG), msg.getString(SENDER))
          })
          actSender ! ChatRes(new ChatWrapper(new Chat(chatId, title, messages), users))
        } else {
          actSender ! ErrorChatReq(data.getString(DETAILS))
        }
      }, failRes => {
        actSender ! ErrorChatReq("Errore nella comunicazione con il server: " + failRes.getMessage)
      })

    case GetNewChatId(chatName) =>
      val actSender: ActorRef = sender()
      GETReq("/chats/new/", resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        val id: String = Json.fromObjectString(body).getString(ID)
        self ! SetChatMsg(new Chat(id, chatName, ListBuffer.empty))
        actSender ! NewChatIdRes(id, chatName)
      }, failRes => {
        actSender ! ErrorNewChatId("Errore nella comunicazione con il server: " + failRes.getMessage)
      })

    case SetChatMsg(chat) =>
      val actSender: ActorRef = sender()
      POSTReq("/chats/" + chat.getId + "/head", chat.queryParams, resBody => {
        val body = resBody.bodyAsString().getOrElse("")
        if (Json.fromObjectString(body).getBoolean(RESULT)) {
          actSender ! OkSetChatMsg(chat)
        } else {
          actSender ! ErrorSetChat("Errore durante il salvataggio dei dati della chat con id: " + chat.getId)
        }
      }, _ => {
        actSender ! ErrorSetChat("Errore durante il salvataggio dei dati della chat con id: " + chat.getId)
      })


  }

  /**
    *
    * @param future
    * @param onSuccess
    * @param onFail
    * @return
    */
  @deprecated
  private def handleResponse(future: Future[HttpResponse], onSuccess: Future[String] => Unit, onFail: HttpResponse => Unit) = {
    future.map {
      case response@HttpResponse(akka.http.scaladsl.model.StatusCodes.OK, _, _, _) => onSuccess(Unmarshal(response.entity).to[String])
      case failRes@_ => onFail(failRes)
    }
  }


  private def POSTReq(uri: String, params: Map[String, String], onSuccess: io.vertx.scala.ext.web.client.HttpResponse[Buffer] => Unit, onFail: Throwable => Unit): Unit = {
    val client = WebClient.create(Vertx.vertx())
    val complexUri = new StringBuffer(uri)
    var first: Boolean = true
    params foreach { case (k, v) =>
      if (first) {
        complexUri.append("?" + k + "=" + v)
        first = false
      } else {
        complexUri.append("&" + k + "=" + v)
      }
    }


    Log.debug("Start -> POST: " + URL + complexUri.toString)
    val future = if (PORT != 0) client.post(PORT, URL, complexUri.toString) else client.post(URL, complexUri.toString)
    future.sendFuture().onComplete {
      case Success(result) =>
        Log.debug("Stop <- POST: " +  URL + complexUri.toString)
        onSuccess(result)
      case Failure(cause) =>
        Log.debug("Stop /w FAIL <- POST: " +  URL + complexUri.toString)
        cause.printStackTrace()
        onFail(cause)
    }
  }

  private def GETReq(uri: String, onSuccess: io.vertx.scala.ext.web.client.HttpResponse[Buffer] => Unit, onFail: Throwable => Unit, params: Map[String, String] = null): Unit = {
    val client = WebClient.create(Vertx.vertx())
    val complexUri = new StringBuffer(uri)
    var first: Boolean = true
    if (params != null) {
      params foreach { case (k, v) =>
        if (first) {
          complexUri.append("?" + k + "=" + v)
          first = false
        } else {
          complexUri.append("&" + k + "=" + v)
        }
      }
    }

    Log.debug("Start -> GET: " + URL + complexUri.toString)
    val future = if (PORT != 0) client.get(PORT, URL, complexUri.toString) else client.get(URL, complexUri.toString)
    future.sendFuture().onComplete {
      case Success(result) =>
        Log.debug("Stop <- GET: " +  URL + complexUri.toString)
        onSuccess(result)
      case Failure(cause) =>
        Log.debug("Stop /w FAIL <- GET: " +  URL + complexUri.toString)
        cause.printStackTrace();
        onFail(cause)
    }
  }
}
