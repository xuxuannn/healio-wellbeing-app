package com.healthtracker.controller

import com.healthtracker.database.AchievementDAO
import com.healthtracker.service.AchievementService
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.{Label, ListView}

class AchievementController {

  @FXML private var streakLabel: Label = _
  @FXML private var badgeList: ListView[String] = _

  @FXML def initialize(): Unit = refresh()

  @FXML def refresh(): Unit = {
    val s = AchievementService.currentStreak()
    streakLabel.setText(s"Current streak: $s days")

    val badges = AchievementDAO.findAllBadges().map(b => s"${b.name} | Earned: ${b.earnedDate}")
    badgeList.setItems(FXCollections.observableArrayList(badges: _*))
  }
}
