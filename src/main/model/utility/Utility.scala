package model.utility

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import model.ChatWrapper
import model.messages.User
import view.LoadingDialog

object Utility {
  def createErrorAlertDialog(what: String, detail: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("ERROR")
    alert.setHeaderText("Error getting the " + what + "!")
    alert.setContentText(detail)
    alert.showAndWait()
  }

  def setUpDialog(loadingDialog: LoadingDialog): Unit = Platform.runLater(() => loadingDialog.setupDialog())
  def showDialog(loadingDialog: LoadingDialog): Unit = Platform.runLater(() => loadingDialog.getDialogStage.show())
  def closeDialog(loadingDialog: LoadingDialog): Unit = Platform.runLater(() => loadingDialog.getDialogStage.close())

  def chatContainsUser(chat: ChatWrapper, user: User): Boolean = {
    chat.members.foreach(m => if(m.getId.equals(user.getId)) return true)
    false
  }
  val SYSTEM_NAME: String = "MySystem"
  val LAYOUT_PATH = "/view/view.fxml"
  val WINDOW_TITLE = "BETTER ACTORS CHAT"
}
