package view;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ViewController {
    private final Map<ActorRef, ObservableList<String>> mapOfChats = new HashMap<>();
    public ListView<String> listOfMessages;
    public ListView<ActorRef> actorsList;
    public TextArea textArea;
    public Label labelActorInfo;
    public Button sendButton;
    public Button addButton;
    public Button removeButton;
    public ChoiceBox choiceBox;
    private ActorRef guiActor;

    private static final String NO_MSG_STRING = "I don't wanna talk";
    private static final String ACTOR_DEFAULT_NAME = "NewActor";
    private static final String DIALOG_TITLE = "Name selection";
    private static final String DIALOG_HEADER = "What's your name?";
    private static final String DIALOG_CONTENT_TEXT = "Name:";
    private static final int DIALOG_PREF_HEIGHT = 120;
    private static final int DIALOG_PREF_WIDTH = 280;
    private static final String LABEL_DEFAULT_TEXT = "Select an actor to send a message:";
    private static final Color LABEL_DEFAULT_COLOR = Color.valueOf("#bcb2b2");

    //TODO
    private String user;
    private int addCounter = 0;

    /**
     * Metodo che inizializza automaticamente la GUI:
     * -Crea il GUIActor
     * -Setta le componenti principali come NOT enambled.
     * -Setta la listView degli attori (inserendovi gli appositi Listeners)
     * -Setta il listener del bottone di Add.
     */
    public void initialize() {

        this.setViewComponents(true, false);
        this.setUpListView();
        this.addButton.setOnAction(e -> this.setDialogWindow());
    }

    public void setUser(final String user) {
        this.user = user;
    }

    /**
     * Metodo che setta i componenti principali.
     * @param areDisabled setta bottone Send, bottone Remove, textArea come disabled se è true; altrimenti enabled.
     * @param areWeInSend se è true vuol dire che abbiamo appena premuto Send, allora svuota la textArea,
     *                    se è false vuol dire che siamo in altre situazioni, quindi setta
     *                    il bottone Remove a areDisabled.
     */
    private void setViewComponents(final boolean areDisabled, final boolean areWeInSend) {
        sendButton.setDisable(areDisabled);
        if (areWeInSend) {
            textArea.clear();
        } else {
            removeButton.setDisable(areDisabled);
        }
        textArea.setDisable(areDisabled);
        labelActorInfo.setText(LABEL_DEFAULT_TEXT);
        this.labelActorInfo.setTextFill(LABEL_DEFAULT_COLOR);
        this.choiceBox.getSelectionModel().selectFirst();
        choiceBox.setPrefWidth(100);
        choiceBox.setStyle("-fx-font: 20px \"Default\";");
    }

    /**
     * Metodo che crea una Dialog per dare un nome all'attore appena creato.
     */
    private void setDialogWindow() {
        TextInputDialog dialog = new TextInputDialog(ACTOR_DEFAULT_NAME +
                (this.addCounter == 0 ? "" : this.addCounter));
        this.addCounter++;
        dialog.setTitle(DIALOG_TITLE);
        dialog.setHeaderText(DIALOG_HEADER);
        dialog.setContentText(DIALOG_CONTENT_TEXT);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(DIALOG_PREF_WIDTH, DIALOG_PREF_HEIGHT);
        Optional<String> result = dialog.showAndWait();
    }

    /**
     * Metodo che ritorna il valore inserito nella TextArea.
     * @return la Stringa inserita, se è null ritorna una Stringa di Deault.
     */
    private String getTextFromArea() {
        return this.textArea.getText().isEmpty() ? NO_MSG_STRING : this.textArea.getText();
    }

    /**
     * Metodo che setta la ListView di attori con gli opportuni listeners.
     */
    private void setUpListView() {
        this.actorsList.setCellFactory(lst -> new ListCell<ActorRef>() {
            @Override
            protected void updateItem(ActorRef item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.path().name());
                }
            }
        });

        this.actorsList.setOnMouseClicked(ev -> {
            final ActorRef currentActor = this.actorsList.getSelectionModel().getSelectedItem();
            if(!this.actorsList.getItems().isEmpty() && currentActor != null) {
                this.setViewComponents(false, false);
                this.listOfMessages.setItems(this.mapOfChats.get(currentActor));
                this.sendButton.setOnAction(add -> {
                    this.setViewComponents(true, true);
                });

                this.removeButton.setOnAction(rem -> {
                    this.setViewComponents(true, false);
                });
            }
        });
    }

}
