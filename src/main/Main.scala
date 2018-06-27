import java.io.IOException

import akka.actor.{ActorRef, ActorSystem, Props}
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.stage.Stage
import model.actors.PreGUIActor
import model.messages.UserSelected
import view.InitialWindowController

class Main extends Application {

  val DEBUG = true
  val LAYOUT_PATH = "/view/view.fxml"
  val WINDOW_TITLE = "BETTER ACTORS CHAT"
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
        this.preActor.tell(UserSelected(initialWindow.getUserField.getText(), primaryStage), ActorRef.noSender)
      }
    })
  }


}

object Main {
  def main(args: Array[String]) {
    Application.launch(classOf[Main], args: _*)
  }
}
