package com.healthtracker.controller

import com.healthtracker.database.MoodDAO
import com.healthtracker.service.DashboardService
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.{Label, ListCell, ListView}
import javafx.scene.image.ImageView

class DashboardController {

  @FXML private var pendingCountLabel: Label = _
  @FXML private var pendingHabitsLabel: Label = _   // ✅ must match FXML
  @FXML private var next3ReminderList: ListView[String] = _
  @FXML private var habitsNotDoneList: ListView[String] = _
  @FXML private var streakLabel: Label = _
  @FXML private var latest3BadgesList: ListView[String] = _
  @FXML private var motivationLabel: Label = _

  // mood UI
  @FXML private var moodDisplayLabel: Label = _
  @FXML private var moodHappy: ImageView = _
  @FXML private var moodNeutral: ImageView = _
  @FXML private var moodSad: ImageView = _
  @FXML private var moodAngry: ImageView = _

  @FXML def initialize(): Unit = {

    // ✅ Habits color by type
    habitsNotDoneList.setCellFactory(_ => new ListCell[String]() {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (empty || item == null) {
          setText(null)
          setStyle("")
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

    // ✅ Reminders color by priority
    next3ReminderList.setCellFactory(_ => new ListCell[String]() {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (empty || item == null) {
          setText(null)
          setStyle("")
        } else {
          setText(item)

          val style =
            if (item.contains("[High]")) "-fx-background-color: #FFE8E8;"
            else if (item.contains("[Medium]")) "-fx-background-color: #FFF6E8;"
            else if (item.contains("[Low]")) "-fx-background-color: #E8FFF4;"
            else ""

          setStyle(style)
        }
      }
    })

    loadMood()
    refresh()
  }

  @FXML def refresh(): Unit = {
    motivationLabel.setText("“" + DashboardService.randomMotivation + "”")

    pendingCountLabel.setText(s"Pending reminders: ${DashboardService.pendingReminderCount}")

    val next3 = DashboardService.next3Reminders.map { r =>
      s"${r.rdate} - ${r.title} (${r.rtype}) [${r.priority}]"
    }
    next3ReminderList.setItems(FXCollections.observableArrayList(next3: _*))

    val habitsNotDone = DashboardService.habitsNotCompletedToday.map(h => s"${h.title} (${h.htype})")
    habitsNotDoneList.setItems(FXCollections.observableArrayList(habitsNotDone: _*))

    pendingHabitsLabel.setText(s"Pending habits: ${habitsNotDone.size}")

    streakLabel.setText(s"Current streak: ${DashboardService.currentStreak} days")

    val badges = DashboardService.latest3Badges.map(b => s"${b.name} @ ${b.earnedDate}")
    latest3BadgesList.setItems(FXCollections.observableArrayList(badges: _*))
  }

  private def loadMood(): Unit = {
    MoodDAO.getTodayMood() match {
      case Some(m) => setMoodUI(m)
      case None    => moodDisplayLabel.setText("Today’s mood: —")
    }
  }

  private def setMoodUI(mood: String): Unit = {
    val (emoji, text) = mood match {
      case "Happy"   => ("😊", "Happy")
      case "Neutral" => ("😐", "Neutral")
      case "Sad"     => ("😞", "Sad")
      case "Angry"   => ("😡", "Angry")
      case _         => ("", mood)
    }

    moodDisplayLabel.setText(s"Today’s mood: $emoji $text")

    moodHappy.getStyleClass.remove("mood-selected")
    moodNeutral.getStyleClass.remove("mood-selected")
    moodSad.getStyleClass.remove("mood-selected")
    moodAngry.getStyleClass.remove("mood-selected")

    mood match {
      case "Happy"   => moodHappy.getStyleClass.add("mood-selected")
      case "Neutral" => moodNeutral.getStyleClass.add("mood-selected")
      case "Sad"     => moodSad.getStyleClass.add("mood-selected")
      case "Angry"   => moodAngry.getStyleClass.add("mood-selected")
      case _         =>
    }
  }

  @FXML def chooseHappy(): Unit = { MoodDAO.setTodayMood("Happy"); setMoodUI("Happy") }
  @FXML def chooseNeutral(): Unit = { MoodDAO.setTodayMood("Neutral"); setMoodUI("Neutral") }
  @FXML def chooseSad(): Unit = { MoodDAO.setTodayMood("Sad"); setMoodUI("Sad") }
  @FXML def chooseAngry(): Unit = { MoodDAO.setTodayMood("Angry"); setMoodUI("Angry") }
}
