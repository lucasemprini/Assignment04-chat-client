package view

import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.{Label, ProgressBar, ProgressIndicator}
import javafx.scene.layout.HBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class LoadingDialog {
  private var dialogStage: Stage = _
  private var progressBar: ProgressBar = _
  private var progressIndicator: ProgressIndicator = _

  def setupDialog(): Unit = {
    progressBar = new ProgressBar()
    progressIndicator = new ProgressIndicator()
    dialogStage = new Stage()
    dialogStage.initStyle(StageStyle.DECORATED)
    dialogStage.setResizable(false)
    dialogStage.initModality(Modality.APPLICATION_MODAL)
    dialogStage.setTitle("LOADING")
    val label = new Label()
    label.setText("Please wait...")
    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS)
    progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS)
    val hb = new HBox
    hb.setSpacing(5)
    hb.setAlignment(Pos.CENTER)
    hb.getChildren.addAll(progressBar, progressIndicator)
    val scene = new Scene(hb)
    dialogStage.setScene(scene)
  }

  @throws[InterruptedException]
  def activateProgressBar(task: Task[Unit]): Unit = {
    progressBar.progressProperty.bind(task.progressProperty)
    progressIndicator.progressProperty.bind(task.progressProperty)
    dialogStage.show()
  }

  def getDialogStage: Stage = dialogStage
}
