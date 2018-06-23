package view

  import javafx.geometry.Insets
  import javafx.geometry.Orientation
  import javafx.scene.Scene
  import javafx.scene.control.Button
  import javafx.scene.control.Label
  import javafx.scene.control.TextField
  import javafx.scene.layout.BorderPane
  import javafx.scene.layout.FlowPane


  object InitialWindowController {
    private val PRESENTATION_STRING = "Please specify your user name: "
    private val USERNAME_LABEL = "User Name: "
    private val OK_TEXT = "OK"
  }

  class InitialWindowController {
    final private val rootBorder = new BorderPane
    final private val flowPane = new FlowPane
    final private val userNameField = new TextField
    final private val presentationLabel = new Label(InitialWindowController.PRESENTATION_STRING)
    final private val userLabel = new Label(InitialWindowController.USERNAME_LABEL)
    final private val dimensionsChosen = new Button(InitialWindowController.OK_TEXT)

    /**
      * Metodo che setta la finestra iniziale di scelta dimensioni.
      *
      * @return la finestra in questione.
      */
    def selectDimensionScene: Scene = {
      this.setUpFlowPane()
      this.addPaddingInsets()
      this.setUpBorderPane()
      new Scene(rootBorder)
    }

    /**
      * Metodo che setta i vari parametri grafici del bottone della finestra iniziale.
      */
    def setUpOkButton(): Button = {
      dimensionsChosen.prefWidthProperty.bind(rootBorder.widthProperty)
      dimensionsChosen
    }

    /**
      * Metodo che setta un BorderPane della finestra iniziale.
      */
    private def setUpBorderPane(): Unit = {
      rootBorder.setTop(presentationLabel)
      rootBorder.setLeft(flowPane)
      rootBorder.setBottom(dimensionsChosen)
    }

    /**
      * Metodo che setta un FlowPane della finestra iniziale.
      */
    private def setUpFlowPane(): Unit = {
      flowPane.setOrientation(Orientation.HORIZONTAL)
      flowPane.getChildren.add(userLabel)
      flowPane.getChildren.add(userNameField)
    }

    /**
      * Metodo che aggiusta i padding delle componenti grafiche della finestra iniziale.
      */
    private def addPaddingInsets(): Unit = {
      userLabel.setPadding(new Insets(0, 5, 0, 5))
      userNameField.setPadding(new Insets(0, 5, 0, 5))
      flowPane.setPadding(new Insets(10, 0, 10, 0))
      rootBorder.setPadding(new Insets(10, 20, 10, 20))
    }

    /**
      * Getter method per il TextField dello user name inserito
      *
      * @return il TextField dello user name.
      */
    def getUserField: TextField = userNameField

}
