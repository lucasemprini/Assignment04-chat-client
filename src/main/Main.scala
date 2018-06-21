import java.io.IOException

import javafx.application.{Application, Platform}
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import view.{InitialWindowLayoutController, ViewController}

class Main extends Application {

  val DEBUG = true
  private val LAYOUT_PATH = "/view/view.fxml"
  private val WINDOW_TITLE = "BETTER ACTORS CHAT"

  override def start(primaryStage: Stage) {

    val initialWindow = new InitialWindowLayoutController
    val scene = initialWindow.selectDimensionScene
    primaryStage.setTitle(WINDOW_TITLE)
    primaryStage.setScene(scene)

    this.addListenerToOkButton(initialWindow.setUpOkButton(), initialWindow, primaryStage)

    primaryStage.show()
  }

  /**
    * Metodo che aggiunge il Listener al Button della finestra iniziale.
    *
    * @param okButton      il Bottone della finestra iniziale.
    * @param initialWindow il controller della finestra iniziale.
    * @param primaryStage  il primaryStage dell'applicazione.
    */
  private def addListenerToOkButton(okButton: Button, initialWindow: InitialWindowLayoutController, primaryStage: Stage): Unit = {
    okButton.setOnAction((_: ActionEvent) => {
      if(!(initialWindow.getUserField.getText.equals(null) || initialWindow.getUserField.getText.isEmpty)) {

        val userName = initialWindow.getUserField.getText()
        try {
          val loader = initGui(primaryStage, userName)
          val lc = loader.getController.asInstanceOf[ViewController]
          lc.setUser(userName)
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
    loader
  }
}

object Main {
  def main(args: Array[String]) {
    Application.launch(classOf[Main], args: _*)
  }
}
