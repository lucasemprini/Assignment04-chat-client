package model.actors

import akka.actor.{Actor, ActorSystem, Props}
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import model.messages._
import view.MainViewController

class PreGUIActor extends Actor {

  private val preActor = ActorSystem.create("MySystem").actorOf(Props(new RestClient()))
  private var layoutController: MainViewController = _

  override def receive: Receive = {
    case CreateMainViewMsg(userId, lc) =>
      this.layoutController = lc
      this.preActor.tell(UserMsg(userId), self)
    case UserRes (user) => layoutController.setUser(user)
    case ErrorUserReq(detail) => Platform.runLater(() => createAlertDialog("User", detail))
    case ErrorChatsReq(detail) => Platform.runLater(() =>createAlertDialog("Chats", detail))
  }

  def createAlertDialog(what: String, detail: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("ERROR")
    alert.setHeaderText("Error getting the " + what + "!")
    alert.setContentText(detail)
    alert.showAndWait()
  }

}
