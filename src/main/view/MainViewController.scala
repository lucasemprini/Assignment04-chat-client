package view

import akka.actor.{ActorRef, ActorSystem, Props}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control._
import javafx.scene.paint.Color
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.input.MouseEvent
import model.ChatWrapper
import model.actors.GUIActor
import model.messages._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object MainViewController {
  private val NO_MSG_STRING = "I don't wanna talk"
  private val CHAT_DEFAULT_NAME = "NewChat"
  private val DIALOG_TITLE = "Name the Chat"
  private val DIALOG_HEADER = "What's the new chat's name?"
  private val DIALOG_CONTENT_TEXT = "Name:"
  private val DIALOG_PREF_HEIGHT = 120
  private val DIALOG_PREF_WIDTH = 280
  private val LABEL_DEFAULT_TEXT = "Select a chat where to send a message:"
  private val LABEL_DEFAULT_COLOR = Color.valueOf("#bcb2b2")
  private val GLOBAL_CHATS = "GLOBAL CHATS"
  private val MY_CHATS = "MY CHATS - "
}

class MainViewController {
  private val mapOfChats = new mutable.HashMap[ActorRef, ObservableList[String]]
  @FXML
  var listOfMessages: ListView[String] = _
  @FXML
  var chatList: ListView[ChatWrapper] = _
  @FXML
  var textArea: TextArea = _
  @FXML
  var labelActorInfo: Label = _
  @FXML
  var sendButton: Button = _
  @FXML
  var addButton: Button = _
  @FXML
  var removeButton: Button = _
  @FXML
  var choiceBox: ChoiceBox[String] = _

  private var guiActor: ActorRef = _
  //TODO cambiare TIPO dello user ??
  private var user: User = _
  private var restClient: ActorRef = _
  private var addCounter = 0

  /**
    * Metodo che inizializza automaticamente la GUI.
    */
  def initialize(): Unit = {}

  /**
    * Metodo che setta lo username ricevuto dalla finestra iniziale e setta tutte le componenti che ne dipendono.
    * @param user lo user preso dal DB specificato dall'utente
    */
  def setUser(user: User, restClient: ActorRef): Unit = {
    this.user = user
    this.restClient = restClient
    //this.chatList.setItems()
    this.setGUIActor()
    this.setChoiceBox()
    this.setViewComponents(areDisabled = true, areWeInSend = false)
    this.setUpListView()
    this.addButton.setOnAction((_: ActionEvent) => this.setDialogWindow())
  }

  /**
    * Metodo che crea il GUIActor.
    */
  private def setGUIActor(): Unit = {
    this.guiActor =
    ActorSystem.create("MySystem").actorOf(Props(new GUIActor(
        this.chatList.getItems,
        this.mapOfChats,
        this.listOfMessages.getItems,
        this.labelActorInfo,
        this.user,
        this.restClient)))
    this.guiActor.tell(SetupViewMsg(), ActorRef.noSender)
  }


  /**
    * Metodo che setta la choiceBox e il relativo Listener.
    */
  private def setChoiceBox(): Unit = {
    this.choiceBox.setPrefWidth(100)
    this.choiceBox.setStyle("-fx-font: 20px \"Default\";")
    this.choiceBox.setItems(FXCollections.observableArrayList[String](
      MainViewController.GLOBAL_CHATS, MainViewController.MY_CHATS + user.getName))
    this.choiceBox.getSelectionModel.selectFirst()
    this.choiceBox.getSelectionModel.selectedIndexProperty.addListener(new ChangeListener[Number]() {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        println(choiceBox.getItems.get(newValue.asInstanceOf[Integer]))
        if(choiceBox.getItems.get(newValue.asInstanceOf[Integer]) == (MainViewController.MY_CHATS + user.getName)) {
          chatList.getItems.stream().filter(c => c.members.contains(user))
        }
      }
    })
  }
  /**
    * Metodo che setta i componenti principali.
    *
    * @param areDisabled setta bottone Send, bottone Remove, textArea come disabled se è true; altrimenti enabled.
    * @param areWeInSend se è true vuol dire che abbiamo appena premuto Send, allora svuota la textArea,
    *                    se è false vuol dire che siamo in altre situazioni, quindi setta
    *                    il bottone Remove a areDisabled.
    */
  private def setViewComponents(areDisabled: Boolean, areWeInSend: Boolean): Unit = {
    this.sendButton.setDisable(areDisabled)
    if (areWeInSend) textArea.clear()
    else this.removeButton.setDisable(areDisabled)
    this.textArea.setDisable(areDisabled)
    this.labelActorInfo.setText(MainViewController.LABEL_DEFAULT_TEXT)
    this.labelActorInfo.setTextFill(MainViewController.LABEL_DEFAULT_COLOR)
  }

  /**
    * Metodo che crea una Dialog per dare un nome all'attore appena creato.
    */
  private def setDialogWindow(): Unit = {
    val dialog = new TextInputDialog(MainViewController.CHAT_DEFAULT_NAME
      + (if (this.addCounter == 0) "" else this.addCounter))
    this.addCounter += 1
    dialog.setTitle(MainViewController.DIALOG_TITLE)
    dialog.setHeaderText(MainViewController.DIALOG_HEADER)
    dialog.setContentText(MainViewController.DIALOG_CONTENT_TEXT)
    dialog.setResizable(true)
    dialog.getDialogPane.setPrefSize(MainViewController.DIALOG_PREF_WIDTH, MainViewController.DIALOG_PREF_HEIGHT)
    val result = dialog.showAndWait
    result.ifPresent(res => invokeGuiActorForAddChat(res))
  }

  /**
    * Metodo che ritorna il valore inserito nella TextArea.
    *
    * @return la Stringa inserita, se è null ritorna una Stringa di Deault.
    */
  private def getTextFromArea = if (this.textArea.getText.isEmpty) MainViewController.NO_MSG_STRING
  else this.textArea.getText

  /**
    * Metodo che setta la ListView di attori con gli opportuni listeners.
    */
  private def setUpListView(): Unit = {
    this.chatList.setCellFactory((_: ListView[ChatWrapper]) => new ListCell[ChatWrapper]() {
      override protected def updateItem(item: ChatWrapper, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (empty) setText("")
        else {
          setText(item.chatName + "\n"
            + " - Members: " + item.members.mkString(", "))
          setStyle("-fx-font: 16px \"Default\";")
        }
      }
    })

    this.chatList.setOnMouseClicked((_: MouseEvent) => {
        val currentChat = this.chatList.getSelectionModel.getSelectedItem
        if (!this.chatList.getItems.isEmpty && currentChat != null) {

          this.invokeGuiActorForSelectedChat(currentChat.actor)
          this.setViewComponents(areDisabled = false, areWeInSend = false)
          this.listOfMessages.setItems(this.mapOfChats(currentChat.actor))

          this.sendButton.setOnAction((_: ActionEvent) => {
            this.invokeGuiActorForSendMsg(currentChat.actor, this.getTextFromArea)
            this.setViewComponents(areDisabled = true, areWeInSend = true)
          })

          this.removeButton.setOnAction((_: ActionEvent) => {
            this.invokeGuiActorForRemoveChat(currentChat)
            this.setViewComponents(areDisabled = true, areWeInSend = false)
          })
        }
    })
  }


  /**
    * Metodo che invia un messaggio di NewChatMsg al GUIActor.
    *
    * @param chatName il nome della nuova chat.
    */
  private def invokeGuiActorForAddChat(chatName: String): Unit = {
    //TODO getNewChatID????
    guiActor.tell(NewChatButtonMsg(this.chatList.getItems, chatName, new Chat(chatName, ListBuffer.empty)), ActorRef.noSender)
  }

  /**
    * Metodo che invia un messaggio di RemoveChatButtonMsg al GUIActor.
    *
    * @param toRemove la chat da rimuovere.
    */
  private def invokeGuiActorForRemoveChat(toRemove: ChatWrapper): Unit = {
    guiActor.tell(RemoveChatButtonMsg(toRemove), ActorRef.noSender)
  }

  /**
    * Metodo che invia un messaggio di SendButtonMessageMsg al GUIActor.
    *
    * @param currentChat la chat su cui inviare il messaggio.
    * @param msg  il messaggio da inviare.
    */
  private def invokeGuiActorForSendMsg(currentChat: ActorRef, msg: String): Unit = {
    guiActor.tell(SendButtonMsg(msg, this.mapOfChats(currentChat), currentChat), ActorRef.noSender)
  }

  /**
    * Metodo che invia un messaggio di ChatSelectedMSg al GUIActor.
    *
    * @param currentChat la chat selezionata.
    */
  private def invokeGuiActorForSelectedChat(currentChat: ActorRef): Unit = {
    guiActor.tell(ChatSelectedMSg(currentChat), ActorRef.noSender)
  }
}

