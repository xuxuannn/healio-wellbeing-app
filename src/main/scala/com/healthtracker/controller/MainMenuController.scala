package com.healthtracker.controller

import com.healthtracker.Session
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Parent
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.layout.{Pane, StackPane}
import javafx.stage.Stage
import scala.jdk.OptionConverters._

class MainMenuController {

  @FXML private var contentPane: StackPane = _

  @FXML def initialize(): Unit = {
    loadCenter("/com/healthtracker/controller/Dashboard.fxml")
  }

  private def loadCenter(fxmlPath: String): Unit = {
    val url = getClass.getResource(fxmlPath)
    if (url == null) {
      showThemedAlert(Alert.AlertType.ERROR, "FXML not found", "FXML not found",
        s"Cannot find:\n$fxmlPath\n\nMake sure it is inside resources folder."
      ).showAndWait()
      return
    }

    try {
      val pane: Pane = FXMLLoader.load(url)
      contentPane.getChildren.setAll(pane)
    } catch {
      case e: Exception =>
        showThemedAlert(Alert.AlertType.ERROR, "Load error", "Load error",
          s"Failed to load:\n$fxmlPath\n\n${e.getMessage}"
        ).showAndWait()
    }
  }

  @FXML def showDashboard(e: ActionEvent): Unit = loadCenter("/com/healthtracker/controller/Dashboard.fxml")
  @FXML def showDashboard(): Unit = showDashboard(null)

  @FXML def showInsights(e: ActionEvent): Unit = loadCenter("/com/healthtracker/controller/Insights.fxml")
  @FXML def showInsights(): Unit = showInsights(null)

  @FXML def showReminder(e: ActionEvent): Unit = loadCenter("/com/healthtracker/controller/Reminder.fxml")
  @FXML def showReminder(): Unit = showReminder(null)

  @FXML def showHabit(e: ActionEvent): Unit = loadCenter("/com/healthtracker/controller/Habit.fxml")
  @FXML def showHabit(): Unit = showHabit(null)

  @FXML def showAchievement(e: ActionEvent): Unit = loadCenter("/com/healthtracker/controller/Achievement.fxml")
  @FXML def showAchievement(): Unit = showAchievement(null)

  @FXML def handleLogout(e: ActionEvent): Unit = {
    val confirm = showThemedAlert(Alert.AlertType.CONFIRMATION, "Logout", "Logout now?", "You will return to Login page.")

    val ok = confirm.showAndWait().toScala.contains(ButtonType.OK)
    if (!ok) return

    Session.logout()

    val loginUrl = getClass.getResource("/com/healthtracker/controller/Login.fxml")
    if (loginUrl == null) {
      showThemedAlert(Alert.AlertType.ERROR, "Login.fxml not found", "Login.fxml not found",
        "Cannot find:\n/com/healthtracker/controller/Login.fxml\n\nCheck file path + resources folder."
      ).showAndWait()
      return
    }

    try {
      val root: Parent = FXMLLoader.load(loginUrl)
      val stage = contentPane.getScene.getWindow.asInstanceOf[Stage]
      val scene = stage.getScene
      scene.setRoot(root)

      val cssUrl = getClass.getResource("/styles/healio-theme.css")
      if (cssUrl != null && !scene.getStylesheets.contains(cssUrl.toExternalForm)) {
        scene.getStylesheets.add(cssUrl.toExternalForm)
      }

      stage.setTitle("Healiooooo")
      stage.show()
    } catch {
      case ex: Exception =>
        showThemedAlert(Alert.AlertType.ERROR, "Logout failed", "Logout failed",
          s"Failed to open Login page.\n\n${ex.getMessage}"
        ).showAndWait()
    }
  }
  @FXML def handleLogout(): Unit = handleLogout(null)

  @FXML def handleExit(e: ActionEvent): Unit = {
    val confirm = showThemedAlert(Alert.AlertType.CONFIRMATION, "Exit", "Exit application?", "Are you sure you want to exit Healio?")

    val ok = confirm.showAndWait().toScala.contains(ButtonType.OK)
    if (ok) System.exit(0)
  }
  @FXML def handleExit(): Unit = handleExit(null)

  @FXML def showAbout(e: ActionEvent): Unit = {
    showThemedAlert(Alert.AlertType.INFORMATION, "About Healio", "Healio – Health Tracker",
      "Health tracker with habits, reminders, mood and achievements."
    ).showAndWait()
  }
  @FXML def showAbout(): Unit = showAbout(null)

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
