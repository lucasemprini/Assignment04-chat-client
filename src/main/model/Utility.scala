package model

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

object Utility {
  def createErrorAlertDialog(what: String, detail: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.setTitle("ERROR")
    alert.setHeaderText("Error getting the " + what + "!")
    alert.setContentText(detail)
    alert.showAndWait()
  }
}
