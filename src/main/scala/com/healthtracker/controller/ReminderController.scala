package com.healthtracker.controller

import com.healthtracker.database.ReminderDAO
import com.healthtracker.model.Reminder
import com.healthtracker.service.AchievementService
import javafx.collections.FXCollections
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Scene
import javafx.scene.control.{ListCell, ListView}
import javafx.stage.{Modality, Stage}

class ReminderController {

  @FXML private var reminderList: ListView[String] = _

  private var current: List[Reminder] = Nil

  @FXML def initialize(): Unit = {

    // ✅ Priority row color
    reminderList.setCellFactory(_ => new ListCell[String]() {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (empty || item == null) {
          setText(null); setStyle("")
        } else {
          setText(item)

          val style =
            if (item.contains("🔥 High")) "-fx-background-color: #FFE2E8;"
            else if (item.contains("⚠ Medium")) "-fx-background-color: #FFF6E8;"
            else if (item.contains("🟢 Low")) "-fx-background-color: #E8FFF4;"
            else ""

          setStyle(style)
        }
      }
    })

    refreshList()
  }

  // =========================
  // Dialog openers
  // =========================

  @FXML def openAddDialog(): Unit = {
    openDialog(
      mode = "add",
      existing = None
    )
  }

  @FXML def openEditDialog(): Unit = {
    val idx = reminderList.getSelectionModel.getSelectedIndex
    if (idx < 0 || idx >= current.length) return

    openDialog(
      mode = "edit",
      existing = Some(current(idx))
    )
  }

  private def openDialog(mode: String, existing: Option[Reminder]): Unit = {
    val url = getClass.getResource("/com/healthtracker/controller/ReminderDialog.fxml")
    val loader = new FXMLLoader(url)
    val root = loader.load[javafx.scene.Parent]()

    val controller = loader.getController[ReminderDialogController]

    val stage = new Stage()
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.setTitle(if (mode == "edit") "Edit Reminder" else "Add Reminder")

    val scene = new Scene(root)

    // ✅ attach css so design match
    val cssUrl = getClass.getResource("/styles/healio-theme.css")
    if (cssUrl != null) scene.getStylesheets.add(cssUrl.toExternalForm)

    stage.setScene(scene)

    controller.setStage(stage)
    controller.setOnSave { r =>
      if (mode == "edit" && existing.isDefined) {
        ReminderDAO.update(existing.get.id, r.title, r.rtype, r.rdate, r.priority)
      } else {
        ReminderDAO.add(r.title, r.rtype, r.rdate, r.priority)
      }
      refreshList()
    }

    existing match {
      case Some(r) => controller.setEditMode(r)
      case None    => controller.setAddMode()
    }

    stage.showAndWait()
  }

  // =========================
  // Normal actions
  // =========================

  @FXML def markDone(): Unit = {
    val idx = reminderList.getSelectionModel.getSelectedIndex
    if (idx >= 0 && idx < current.length) {
      val r = current(idx)
      ReminderDAO.markDone(r.id, done = true)

      AchievementService.refreshBadgesAfterReminderDone()
      refreshList()
    }
  }

  @FXML def refreshList(): Unit = {
    current = ReminderDAO.findAll()

    val display = current.map { r =>
      val status = if (r.isDone) "✅ Done" else "⏳ Pending"
      val pr = r.priority match {
        case "High" => "🔥 High"
        case "Low"  => "🟢 Low"
        case _      => "⚠ Medium"
      }
      s"$status | $pr | ${r.rdate} | ${r.title} (${r.rtype})"
    }

    reminderList.setItems(FXCollections.observableArrayList(display: _*))
  }
}
