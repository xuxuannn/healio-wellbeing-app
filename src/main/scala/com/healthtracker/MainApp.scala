package com.healthtracker

import com.healthtracker.database.DB
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.image.Image
import javafx.stage.Stage

class MainApp extends Application {

  override def start(stage: Stage): Unit = {
    DB.init()

    // ✅ START FROM LOGIN
    val root: Parent =
      FXMLLoader.load(getClass.getResource("/com/healthtracker/controller/Login.fxml"))

    val scene = new Scene(root)

    // ✅ attach CSS
    val cssUrl = getClass.getResource("/styles/healio-theme.css")
    if (cssUrl != null) scene.getStylesheets.add(cssUrl.toExternalForm)

    // ✅ window icon
    val iconStream = getClass.getResourceAsStream("/icon/app-icon.png")
    if (iconStream != null) {
      stage.getIcons.add(new Image(iconStream))
      iconStream.close()
    }

    stage.setTitle("Healiooooo")
    stage.setScene(scene)
    stage.show()
  }
}

object MainApp {
  def main(args: Array[String]): Unit =
    Application.launch(classOf[MainApp], args: _*)
}
