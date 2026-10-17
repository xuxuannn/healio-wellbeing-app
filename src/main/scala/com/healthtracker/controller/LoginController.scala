package com.healthtracker.controller

import com.healthtracker.Session
import com.healthtracker.database.UserDAO
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.Scene
import javafx.scene.control.{Label, PasswordField, TextField}
import javafx.scene.layout.{Pane, VBox}
import javafx.stage.Stage

class LoginController {

  // ===== TOP LABEL =====
  @FXML private var modeLabel: Label = _

  // ===== LOGIN =====
  @FXML private var loginPane: VBox = _
  @FXML private var loginUsername: TextField = _
  @FXML private var loginPassword: PasswordField = _
  @FXML private var loginMsg: Label = _

  // ===== REGISTER =====
  @FXML private var registerPane: VBox = _
  @FXML private var regUsername: TextField = _
  @FXML private var regPassword: PasswordField = _
  @FXML private var regConfirm: PasswordField = _
  @FXML private var regMsg: Label = _

  @FXML def initialize(): Unit = {
    loginMsg.setText("")
    regMsg.setText("")
    switchToLogin()
  }

  // =========================
  // Toggle UI
  // =========================
  @FXML def switchToLogin(): Unit = {
    modeLabel.setText("Login")

    loginPane.setVisible(true)
    loginPane.setManaged(true)

    registerPane.setVisible(false)
    registerPane.setManaged(false)

    regMsg.setText("")
  }

  @FXML def switchToRegister(): Unit = {
    modeLabel.setText("Register")

    registerPane.setVisible(true)
    registerPane.setManaged(true)

    loginPane.setVisible(false)
    loginPane.setManaged(false)

    loginMsg.setText("")
  }

  // =========================
  // Login
  // =========================
  @FXML def handleLogin(): Unit = {
    val u = Option(loginUsername.getText).getOrElse("").trim
    val p = Option(loginPassword.getText).getOrElse("").trim

    if (u.isEmpty || p.isEmpty) {
      loginMsg.setText("Please enter username and password.")
      return
    }

    UserDAO.validateLogin(u, p) match {
      case Some(userId) =>
        Session.loginAs(userId, u)
        openMainMenu()
      case None =>
        loginMsg.setText("Invalid username or password.")
    }
  }


  // =========================
  // Register
  // =========================
  @FXML def handleRegister(): Unit = {
    val u = Option(regUsername.getText).getOrElse("").trim
    val p = Option(regPassword.getText).getOrElse("").trim
    val c = Option(regConfirm.getText).getOrElse("").trim

    if (u.isEmpty || p.isEmpty || c.isEmpty) {
      regMsg.setText("Please fill in all fields.")
      return
    }
    if (p.length < 4) {
      regMsg.setText("Password must be at least 4 characters.")
      return
    }
    if (p != c) {
      regMsg.setText("Password and confirm password do not match.")
      return
    }
    if (UserDAO.usernameExists(u)) {
      regMsg.setText("Username already exists. Please choose another.")
      return
    }

    val ok = UserDAO.register(u, p)
    if (ok) {
      regMsg.setText("✅ Register success! Please login.")
      loginUsername.setText(u)
      loginPassword.clear()
      switchToLogin()
    } else {
      regMsg.setText("Register failed. Please try again.")
    }
  }

  // =========================
  // Navigation
  // =========================
  private def openMainMenu(): Unit = {
    val url = getClass.getResource("/com/healthtracker/controller/MainMenu.fxml")
    val root: Pane = FXMLLoader.load(url)

    val stage = loginUsername.getScene.getWindow.asInstanceOf[Stage]
    val scene = new Scene(root)

    val cssUrl = getClass.getResource("/styles/healio-theme.css")
    if (cssUrl != null) scene.getStylesheets.add(cssUrl.toExternalForm)

    stage.setScene(scene)
    stage.setTitle("Healiooooo")
    stage.show()
  }
}
