package com.healthtracker.controller

import com.healthtracker.model.Reminder
import javafx.fxml.FXML
import javafx.scene.control.{ComboBox, DatePicker, Label, TextField}
import javafx.stage.Stage
import javafx.collections.FXCollections

class ReminderDialogController {

  @FXML private var titleLabel: Label = _
  @FXML private var titleField: TextField = _
  @FXML private var typeCombo: ComboBox[String] = _
  @FXML private var priorityCombo: ComboBox[String] = _
  @FXML private var datePicker: DatePicker = _
  @FXML private var msgLabel: Label = _

  private var stage: Stage = _
  private var reminderId: Option[Int] = None
  private var onSave: Reminder => Unit = _

  @FXML def initialize(): Unit = {
    typeCombo.setItems(FXCollections.observableArrayList("Doctor", "Body Check", "Medication", "Custom"))
    typeCombo.getSelectionModel.selectFirst()

    priorityCombo.setItems(FXCollections.observableArrayList("High", "Medium", "Low"))
    priorityCombo.getSelectionModel.select("Medium")

    msgLabel.setText("")
  }

  /** set dialog window */
  def setStage(s: Stage): Unit = stage = s

  /** callback for saving */
  def setOnSave(cb: Reminder => Unit): Unit = onSave = cb

  /** Add mode */
  def setAddMode(): Unit = {
    reminderId = None
    titleLabel.setText("Add Reminder")
  }

  /** Edit mode */
  def setEditMode(r: Reminder): Unit = {
    reminderId = Some(r.id)
    titleLabel.setText("Edit Reminder")

    titleField.setText(r.title)
    typeCombo.getSelectionModel.select(r.rtype)
    priorityCombo.getSelectionModel.select(r.priority)

    // r.rdate is string "yyyy-mm-dd"
    try datePicker.setValue(java.time.LocalDate.parse(r.rdate))
    catch { case _: Exception => () }
  }

  @FXML def handleCancel(): Unit = {
    if (stage != null) stage.close()
  }

  @FXML def handleSave(): Unit = {
    val title = Option(titleField.getText).getOrElse("").trim
    val rtype = typeCombo.getValue
    val priority = priorityCombo.getValue
    val date = datePicker.getValue

    if (title.isEmpty) {
      msgLabel.setText("Title cannot be empty.")
      return
    }
    if (date == null) {
      msgLabel.setText("Please choose a date.")
      return
    }

    val newReminder = Reminder(
      id = reminderId.getOrElse(-1),   // controller will decide insert/update
      title = title,
      rtype = rtype,
      rdate = date.toString,
      priority = priority,
      isDone = false
    )

    if (onSave != null) onSave(newReminder)
    if (stage != null) stage.close()
  }
}
