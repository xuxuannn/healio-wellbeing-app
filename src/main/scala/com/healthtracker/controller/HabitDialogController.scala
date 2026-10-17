package com.healthtracker.controller

import com.healthtracker.model.Habit
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.{ComboBox, Label, TextField}
import javafx.stage.Stage

class HabitDialogController {

  @FXML private var titleLabel: Label = _
  @FXML private var titleField: TextField = _
  @FXML private var typeCombo: ComboBox[String] = _
  @FXML private var msgLabel: Label = _

  private var stage: Stage = _
  private var onSave: (String, String) => Unit = (_, _) => ()

  @FXML def initialize(): Unit = {
    typeCombo.setItems(FXCollections.observableArrayList("Sleep", "Water", "Exercise", "Medication"))
    typeCombo.getSelectionModel.selectFirst()
    msgLabel.setText("")
  }

  def setStage(s: Stage): Unit = stage = s
  def setOnSave(cb: (String, String) => Unit): Unit = onSave = cb

  def setAddMode(): Unit = {
    titleLabel.setText("Add Habit")
    titleField.clear()
    typeCombo.getSelectionModel.selectFirst()
    msgLabel.setText("")
  }

  def setEditMode(h: Habit): Unit = {
    titleLabel.setText("Edit Habit")
    titleField.setText(h.title)
    typeCombo.getSelectionModel.select(h.htype)
    msgLabel.setText("")
  }

  @FXML def handleCancel(): Unit = {
    if (stage != null) stage.close()
  }

  @FXML def handleSave(): Unit = {
    val title = Option(titleField.getText).getOrElse("").trim
    val htype = typeCombo.getValue

    if (title.isEmpty) {
      msgLabel.setText("Title cannot be empty.")
      return
    }

    onSave(title, htype)
    if (stage != null) stage.close()
  }
}
