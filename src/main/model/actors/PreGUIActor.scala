package model.actors

import java.io.IOException

import akka.actor.{Actor, ActorSystem, Props}
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.{AnchorPane, GridPane}
import javafx.stage.Stage
import model.Utility
import model.messages._
import view.MainViewController

class PreGUIActor extends Actor {

  val LAYOUT_PATH = "/view/view.fxml"
  val WINDOW_TITLE = "BETTER ACTORS CHAT"
  private val restClient = ActorSystem.create("MySystem").actorOf(Props(new RestClient()))
  private var mainStage: Stage = _
  private var userIdChosen: String = ""
  private var userName: String = ""

  override def receive: Receive = {
    case UserSelected(userId, primaryStage) =>
      this.mainStage = primaryStage
      this.userIdChosen = userId
      this.restClient.tell(UserMsg(userId), self)

    case UserRes (user) => loadGUI(user)

    case ErrorUserReq(_) => Platform.runLater(() => {
      val yesToNewAccount = this.createAlertUserNotExistent()
      if(yesToNewAccount) {
        this.createAlertNewAccount()
      }
    })
    case OKSetUserMsg(user) => loadGUI(user)
    case ErrorChatsReq(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("Chats", detail))
    case ErrorSetUser(detail) => Platform.runLater(() => Utility.createErrorAlertDialog("User", detail))
  }


  private def createAlertUserNotExistent(): Boolean = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle("The Username does not exists!")
    alert.setHeaderText("The Username you provided does not exist on this Server!")
    alert.setContentText("Do you want to create a New Account?")

    val buttonTypeYes = new ButtonType("Yes", ButtonData.YES)
    val buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)

    alert.getButtonTypes.setAll(buttonTypeYes, buttonTypeCancel)
    alert.showAndWait.get eq buttonTypeYes
  }

  /**
    * Genera Alert per la creazione di un nuovo account.
    */
  private def createAlertNewAccount(): Unit = {

    val dialog = new Dialog
    dialog.setTitle("Create new Account")
    dialog.setHeaderText("Create your brand new Account!")

    // Set the button types.
    val loginButtonType = new ButtonType("Create", ButtonData.OK_DONE)
    dialog.getDialogPane.getButtonTypes.addAll(loginButtonType, ButtonType.CANCEL)

    // Create the username and name labels and fields.
    val grid = new GridPane
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setPadding(new Insets(20, 150, 10, 10))

    val userId = new TextField()
    userId.setText(userIdChosen)
    val name = new TextField

    grid.add(new Label("UserID:"), 0, 0)
    grid.add(userId, 1, 0)
    grid.add(new Label("Your Name:"), 0, 1)
    grid.add(name, 1, 1)

    val loginButton = dialog.getDialogPane.lookupButton(loginButtonType)
    loginButton.setDisable(true)

    name.textProperty.addListener((_, _, newValue) => {
      loginButton.setDisable(newValue.trim.isEmpty)
    })

    dialog.getDialogPane.setContent(grid)
    val result = dialog.showAndWait()
    if(result.isPresent) {
      this.userName = name.getText()
      this.userIdChosen = userId.getText()
      this.restClient.tell(SetUserMsg(new User(userIdChosen, userName)), self)
    }
  }

  /**
    * Metodo per inizializzare la GUI dell'applicazione.
    *
    * @param primaryStage lo stage primario della GUI JavaFX.
    * @return il FXMLLoader.
    */
  @throws[IOException]
  private def initGui(primaryStage: Stage, userName: String): FXMLLoader = {
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

  /**
    * Carica la GUI principale con i giusti parametri.
    */
  private def loadGUI(user: User): Unit = Platform.runLater(()=>
    try {
      val loader = initGui(mainStage, user.getId)
      val lc = loader.getController[MainViewController]
      lc.setUser(user, restClient)
    } catch {
      case e@(_: IOException | _: InterruptedException) => e.printStackTrace()
    })
}
