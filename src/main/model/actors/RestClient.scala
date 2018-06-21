package model.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.vertx.lang.scala.json.{Json, JsonObject}
import model.actors.RestClient._
import model.messages.{User, UserChatsMsg, UserMsg}

import scala.concurrent.Future


object RestClient {
  def props(): Props = Props(new RestClient)

  val URL_PREFIX = "https://assignment04-chat-server.herokuapp.com"
}


class RestClient extends Actor {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val execeutionContext = system.dispatcher


  override def receive: Receive = {
    case UserMsg(id) =>
      println("User request start")
      val actSender: ActorRef = sender()
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = URL_PREFIX + "/user/" + id))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          val data = new JsonObject(body)

          if (data.getBoolean("result")) {
            val jsonUser = data.getJsonObject("user")
            val user = new User(id, jsonUser.getString("name"))
            println("USER: " + user)
            self ! UserChatsMsg(user, actSender)
          } else {
            println("Details: " + data.getString("details"))
            actSender ! "Error: " + data.getString("details")
          }


        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
        actSender ! "hello"
      })

    case UserChatsMsg(msgUser, sender) =>

      val actSender: ActorRef = if (sender == null) self else sender
      val user: User = new User(msgUser)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = URL_PREFIX + "/user/" + user.getId() + "/chats"))

      handleResponse(responseFuture, resBody => {
        resBody.map(body => {
          val data = Json.fromObjectString(body)

          if (data.getBoolean("result")) {
            data.getJsonArray("chats").getList.forEach(e => user.addChat(e.toString) )
            println("User / name: " + user.getName() )
            user.chats foreach (e => println("chat: " + e))
            actSender ! user
          }


        })
      }, failRes => {
        println("fail, status code: " + failRes.status)
      })
  }


  private def handleResponse(future: Future[HttpResponse], onSuccess: Future[String] => Unit, onFail: HttpResponse => Unit) = {
    future.map {
      case response@HttpResponse(StatusCodes.OK, _, _, _) => onSuccess(Unmarshal(response.entity).to[String])
      case failRes@_ => onFail(failRes)
    }
  }

}
