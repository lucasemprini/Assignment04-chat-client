import java.io.IOException

import akka.actor.{ActorRef, ActorSystem, Props}
import javafx.application.{Application, Platform}
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{Alert, Button, ButtonType}
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import model.actors.PreGUIActor
import model.messages.CreateMainViewMsg
import view.{InitialWindowController, MainViewController}

class Main extends Application {

  val DEBUG = true
  private val LAYOUT_PATH = "/view/view.fxml"
  private val WINDOW_TITLE = "BETTER ACTORS CHAT"
  private val preActor = ActorSystem.create("MySystem").actorOf(Props(new PreGUIActor()))

  override def start(primaryStage: Stage) {

    val initialWindow = new InitialWindowController
    val scene = initialWindow.selectDimensionScene
    primaryStage.setTitle(WINDOW_TITLE)
    primaryStage.setScene(scene)
    this.addListenerToOkButton(initialWindow.setUpOkButton(), initialWindow, primaryStage)
    scene.getWindow.centerOnScreen()
    primaryStage.show()
  }

  /**
    * Metodo che aggiunge il Listener al Button della finestra iniziale.
    *
    * @param okButton      il Bottone della finestra iniziale.
    * @param initialWindow il controller della finestra iniziale.
    * @param primaryStage  il primaryStage dell'applicazione.
    */
  private def addListenerToOkButton(okButton: Button, initialWindow: InitialWindowController, primaryStage: Stage): Unit = {
    okButton.setOnAction((_: ActionEvent) => {
      if(!(initialWindow.getUserField.getText.equals(null) || initialWindow.getUserField.getText.isEmpty)) {

        if(true) {
          //TODO
          val yesToNewAccount = this.createAlert()
        }
        val userId = initialWindow.getUserField.getText()
        try {
          val loader = initGui(primaryStage, userId)
          val lc = loader.getController.asInstanceOf[MainViewController]
          this.preActor.tell(CreateMainViewMsg(userId, lc), ActorRef.noSender)
        } catch {
          case e@(_: IOException | _: InterruptedException) =>
            e.printStackTrace()
        }
      }
    })
  }

  /**
    * Metodo per inizializzare la GUI dell'applicazione.
    *
    * @param primaryStage lo stage primario della GUI JavaFX.
    * @return il FXMLLoader.
    */
  @throws[IOException]
  private def initGui(primaryStage: Stage, userName: String) = {
    val loader = new FXMLLoader(getClass.getResource(LAYOUT_PATH))
    val root = loader.load().asInstanceOf[AnchorPane]
    primaryStage.setTitle(WINDOW_TITLE + " - " + userName)
    primaryStage.setScene(new Scene(root))
    primaryStage.setOnCloseRequest((_) => {
      Platform.exit()
      System.exit(0)
    })
    primaryStage.getScene.getWindow.centerOnScreen()
    loader
  }

  private def createAlert(): Boolean = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle("The Username does not exists!")
    alert.setHeaderText("The Username you provided does not exist on this Server!")
    alert.setContentText("Do you want to create a New Account?")

    val buttonTypeYes = new ButtonType("Yes", ButtonData.YES)
    val buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)

    alert.getButtonTypes.setAll(buttonTypeYes, buttonTypeCancel)
    alert.showAndWait.get eq buttonTypeYes
  }
}

object Main {
  def main(args: Array[String]) {
    Application.launch(classOf[Main], args: _*)
  }
}
