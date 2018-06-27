package model.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.vertx.lang.scala.json.{Json, JsonObject}
import model.actors.RestClient._
import model.messages._

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future


object RestClient {
  def props(): Props = Props(new RestClient)

  val URL_PREFIX = "https://assignment04-chat-server.herokuapp.com"
  val RESULT = "result"
  val DETAILS = "details"
  val CHAT: String = "chat"
  val TIMESTAMP: String = "timestamp"
  val MSG: String = "msg"
  val SENDER: String = "sender"
  val ID: String = "id"
}


class RestClient extends Actor {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val execeutionContext = system.dispatcher

  /**
    * Risponde a:
    *   - UserMsg(id)
    *       Esegue una GET /user/:id
    *       POS: Non risponde direttamente ma manda un messaggio a se stesso in ricerca delle chat dell'utente trovato
    *       NEG: Risponde con un ErrorUserReq(details) fornendo i dettagli che hanno causato l'errore
    *
    *   - UserChatsMsg(msgUser, sender)
    *       Esegue una GET /user/:id/chats
    *       POS: Risponde con un messaggio UserRes(user) contenente l'utente compilato con i propri dati e le sue chats
    *       NEG: Risponde con un ErrorUserReq o ErrorChatsReq in base che chi ha chiamato la ricerca cercasse l'utente o la sua lista di chat.
    *
    *   - GetChatMsg(chatId)
    *       Esegue una GET /chats/:id
    *       POS: Risponde con un messaggio Chat contente l'id della chat e una lista di messaggio
    *       NEG: Risponde con un messaggio ErrorChatReq(details)
    *
    *   - SetUserMsg(user)
    *       Esegue un POST /user/:id
    *         POS: Risponde con un messaggio OkSetUserMsg, ciò significa che è andato tutto bene
    *         NEG: Risponde con un messaggio ErrorSetUser(details)
    * @return
    */
  override def receive: Receive = {
    case UserMsg(id) =>
      val actSender: ActorRef = sender()
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = URL_PREFIX + "/user/" + id))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          val data = new JsonObject(body)

          if (data.getBoolean(RESULT)) {
            val jsonUser = data.getJsonObject("user")
            val user = new User(id, jsonUser.getString("name"))
            self ! UserChatsMsg(user, actSender)
          } else {
            actSender ! ErrorUserReq(data.getString(DETAILS))
          }
        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        actSender ! ErrorUserReq("Errore nella comunicazione con il server: " + failRes.entity.toString)
      })

    case UserChatsMsg(msgUser, sender) =>
      val fromUser: Boolean = if (sender == null) false else true
      val actSender: ActorRef = if (sender == null) self else sender
      val user: User = new User(msgUser)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = URL_PREFIX + "/user/" + user.getId + "/chats"))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
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
        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        if (fromUser) {
          actSender ! ErrorUserReq("Errore nella comunicazione con il server: " + failRes.entity.toString)
        } else {
          actSender ! ErrorChatsReq("Errore nella comunicazione con il server: " + failRes.entity.toString)
        }
      })

    case GetChatMsg(chatId) =>
      val actSender: ActorRef = sender()
      val messages: ListBuffer[Message] = ListBuffer()
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = URL_PREFIX + "/chats/" + chatId))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          val data: JsonObject = Json.fromObjectString(body)
          if (data.getBoolean(RESULT)) {
            data.getJsonArray(CHAT) forEach (jsonMsg => {
              val msg: JsonObject = Json.fromObjectString(jsonMsg.toString)
              messages += new Message(msg.getLong(TIMESTAMP), msg.getString(MSG), msg.getString(SENDER))
            })
            actSender ! new Chat(chatId, messages)
          } else {
            actSender ! ErrorChatReq(data.getString(DETAILS))
          }
        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        actSender ! ErrorChatReq("Errore nella comunicazione con il server: " + failRes.entity.toString)
      })

    case GetNewChatId() =>
      val actSender: ActorRef = sender()
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri= URL_PREFIX + "/chats/new/"))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          actSender ! new NewChatId(Json.fromObjectString(body).getInteger(ID))
        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        actSender ! ErrorNewChatId("Errore nella comunicazione con il server: " + failRes.entity.toString)
      })

    case SetUserMsg(user) =>
      val actSender: ActorRef = sender()
      val responseFuture: Future[HttpResponse] = Http().singleRequest(
                                                                  HttpRequest(method = HttpMethods.POST,
                                                                    uri = Uri(URL_PREFIX + "/user/" + user.getId),
                                                                    entity = FormData(user.queryParams).toEntity(HttpCharsets.`UTF-8`)))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          if (Json.fromObjectString(body).getBoolean(RESULT)) {
            println("EHILLAAAAA")
            actSender ! OKSetUserMsg(user)
          } else {
            println("SAD")
            actSender ! ErrorSetUser("Errore durante il salvataggio dei dati dell'utente: " + user.getId)
          }
        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        actSender ! ErrorSetUser("Errore durante il salvataggio dei dati dell'utente: " + user.getId)
      })

  }


  private def handleResponse(future: Future[HttpResponse], onSuccess: Future[String] => Unit, onFail: HttpResponse => Unit) = {
    future.map {
      case response@HttpResponse(StatusCodes.OK, _, _, _) => onSuccess(Unmarshal(response.entity).to[String])
      case failRes@_ => onFail(failRes)
    }
  }

}
