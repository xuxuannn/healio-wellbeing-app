package com.healthtracker.controller

import com.healthtracker.database.HabitDAO
import com.healthtracker.model.Habit
import com.healthtracker.service.AchievementService
import javafx.collections.FXCollections
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Scene
import javafx.scene.control.{Alert, ButtonType, Label, ListCell, ListView, ProgressBar}
import javafx.stage.{Modality, Stage}

class HabitController {

  @FXML private var habitList: ListView[String] = _
  @FXML private var progressLabel: Label = _
  @FXML private var progressBar: ProgressBar = _

  private var current: List[Habit] = Nil

  @FXML def initialize(): Unit = {
    habitList.setCellFactory(_ => new ListCell[String]() {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (empty || item == null) {
          setText(null); setStyle("")
        } else {
          setText(item)
          val style =
            if (item.contains("(Sleep)")) "-fx-background-color: #E8F0FF;"
            else if (item.contains("(Water)")) "-fx-background-color: #E8FFF4;"
            else if (item.contains("(Exercise)")) "-fx-background-color: #FFF6E8;"
            else if (item.contains("(Medication)")) "-fx-background-color: #FFE8F1;"
            else ""
          setStyle(style)
        }
      }
    })

    refreshList()
  }

  // =========================
  // ADD
  // =========================
  @FXML def openAddDialog(): Unit = {
    openDialog(mode = "add", existing = None)
  }

  // =========================
  // EDIT
  // =========================
  @FXML def openEditDialog(): Unit = {
    val idx = habitList.getSelectionModel.getSelectedIndex
    if (idx < 0 || idx >= current.length) {
      showThemedAlert(
        Alert.AlertType.INFORMATION,
        "Info",
        "Please select a habit first.",
        "Tip: Click one row in the habit list, then press Edit."
      ).showAndWait()
      return
    }
    openDialog(mode = "edit", existing = Some(current(idx)))
  }

  // =========================
  // DIALOG LOADER (SAME AS REMINDER)
  // =========================
  private def openDialog(mode: String, existing: Option[Habit]): Unit = {
    try {
      val url = getClass.getResource("/com/healthtracker/controller/HabitDialog.fxml")
      if (url == null) {
        showThemedAlert(Alert.AlertType.ERROR,
          "FXML Missing",
          "HabitDialog.fxml not found",
          "Cannot find:\n/com/healthtracker/controller/HabitDialog.fxml"
        ).showAndWait()
        return
      }

      val loader = new FXMLLoader(url)
      val root = loader.load[javafx.scene.Parent]()

      val controller = loader.getController[HabitDialogController]

      val stage = new Stage()
      stage.initModality(Modality.APPLICATION_MODAL)
      stage.setTitle(if (mode == "edit") "Edit Habit" else "Add Habit")

      val scene = new Scene(root)

      // attach CSS
      val cssUrl = getClass.getResource("/styles/healio-theme.css")
      if (cssUrl != null) scene.getStylesheets.add(cssUrl.toExternalForm)

      stage.setScene(scene)

      controller.setStage(stage)
      controller.setOnSave { (title: String, htype: String) =>
        if (mode == "edit" && existing.isDefined) {
          HabitDAO.update(existing.get.id, title, htype)
        } else {
          HabitDAO.add(title, htype)
        }
        refreshList()
      }

      existing match {
        case Some(h) => controller.setEditMode(h)
        case None    => controller.setAddMode()
      }

      stage.showAndWait()

    } catch {
      case ex: Exception =>
        showThemedAlert(
          Alert.AlertType.ERROR,
          "Dialog Error",
          "Failed to open Habit dialog",
          s"${ex.getClass.getSimpleName}: ${ex.getMessage}"
        ).showAndWait()
    }
  }

  // ======================
  // MARK COMPLETED
  // ======================
  @FXML def markCompletedToday(): Unit = {
    val idx = habitList.getSelectionModel.getSelectedIndex
    if (idx < 0 || idx >= current.length) {
      showThemedAlert(Alert.AlertType.INFORMATION,
        "Info",
        "Please select a habit first.",
        "Tip: Select a row, then press Mark Completed."
      ).showAndWait()
      return
    }

    HabitDAO.markCompletedToday(current(idx).id)
    AchievementService.refreshBadgesAfterHabitCompletion()
    refreshList()
  }

  // ======================
  // DELETE
  // ======================
  @FXML def deleteSelected(): Unit = {
    val idx = habitList.getSelectionModel.getSelectedIndex
    if (idx < 0 || idx >= current.length) {
      showThemedAlert(Alert.AlertType.INFORMATION,
        "Info",
        "Please select a habit first.",
        "Tip: Select a row, then press Delete."
      ).showAndWait()
      return
    }

    val h = current(idx)

    val alert = showThemedAlert(
      Alert.AlertType.CONFIRMATION,
      "Delete Habit",
      "Confirm deletion",
      s"${h.title} (${h.htype})"
    )

    val result = alert.showAndWait()
    if (result.isPresent && result.get == ButtonType.OK) {
      HabitDAO.deleteById(h.id)
      refreshList()
    }
  }

  // ======================
  // REFRESH
  // ======================
  @FXML def refreshList(): Unit = {
    current = HabitDAO.findAll()

    val display = current.map { h =>
      val done = HabitDAO.isCompletedToday(h.id)
      val status = if (done) "✅ Done" else "⏳ Pending"
      s"$status | ${h.title} (${h.htype})"
    }

    habitList.setItems(FXCollections.observableArrayList(display: _*))

    val pct = HabitDAO.todayProgressPercentage()
    progressLabel.setText(s"Today Progress: $pct%")
    progressBar.setProgress(pct / 100.0)
  }

  // ======================
  // PINK THEMED ALERT
  // ======================
  private def showThemedAlert(t: Alert.AlertType, title: String, header: String, content: String): Alert = {
    val a = new Alert(t)
    a.setTitle(title)
    a.setHeaderText(header)
    a.setContentText(content)

    val cssUrl = getClass.getResource("/styles/healio-theme.css")
    if (cssUrl != null) {
      val sheet = cssUrl.toExternalForm
      if (!a.getDialogPane.getStylesheets.contains(sheet)) {
        a.getDialogPane.getStylesheets.add(sheet)
      }
    }

    a.getDialogPane.getStyleClass.add("healio-dialog")
    a
  }
}
