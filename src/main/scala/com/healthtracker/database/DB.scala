package com.healthtracker.database

import java.sql.{Connection, DriverManager, SQLException}
import java.security.MessageDigest

object DB {

  private val url = "jdbc:derby:healthtrackerDB;create=true"

  def getConnection: Connection =
    DriverManager.getConnection(url)

  /** Call once at app startup */
  def init(): Unit = {

    // Users
    createTable(
      """
        |CREATE TABLE users (
        |  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        |  username VARCHAR(50) NOT NULL UNIQUE,
        |  password_hash VARCHAR(128) NOT NULL,
        |  created_at VARCHAR(20) NOT NULL
        |)
        |""".stripMargin
    )

    // Habits (PER USER)
    createTable(
      """
        |CREATE TABLE habits (
        |  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        |  user_id INT NOT NULL,
        |  title VARCHAR(200) NOT NULL,
        |  htype VARCHAR(50) NOT NULL,
        |  CONSTRAINT fk_habits_user FOREIGN KEY (user_id) REFERENCES users(id)
        |)
        |""".stripMargin
    )

    // Habit completions (PER USER)
    createTable(
      """
        |CREATE TABLE habit_completions (
        |  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        |  user_id INT NOT NULL,
        |  habit_id INT NOT NULL,
        |  cdate VARCHAR(20) NOT NULL,
        |  CONSTRAINT fk_habit FOREIGN KEY (habit_id) REFERENCES habits(id),
        |  CONSTRAINT fk_habitcomp_user FOREIGN KEY (user_id) REFERENCES users(id)
        |)
        |""".stripMargin
    )

    // Reminders (PER USER)
    createTable(
      """
        |CREATE TABLE reminders (
        |  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        |  user_id INT NOT NULL,
        |  title VARCHAR(200) NOT NULL,
        |  rtype VARCHAR(50) NOT NULL,
        |  rdate VARCHAR(20) NOT NULL,
        |  priority VARCHAR(10) DEFAULT 'Medium',
        |  is_done SMALLINT DEFAULT 0,
        |  CONSTRAINT fk_rem_user FOREIGN KEY (user_id) REFERENCES users(id)
        |)
        |""".stripMargin
    )

    // Badges (PER USER)
    createTable(
      """
        |CREATE TABLE badges (
        |  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        |  user_id INT NOT NULL,
        |  code VARCHAR(50) NOT NULL,
        |  name VARCHAR(200) NOT NULL,
        |  earned_date VARCHAR(20) NOT NULL,
        |  CONSTRAINT fk_badge_user FOREIGN KEY (user_id) REFERENCES users(id)
        |)
        |""".stripMargin
    )

    // Daily mood (PER USER)  (composite PK)
    createTable(
      """
        |CREATE TABLE daily_mood (
        |  user_id INT NOT NULL,
        |  mdate VARCHAR(20) NOT NULL,
        |  mood VARCHAR(30) NOT NULL,
        |  PRIMARY KEY (user_id, mdate),
        |  CONSTRAINT fk_mood_user FOREIGN KEY (user_id) REFERENCES users(id)
        |)
        |""".stripMargin
    )
  }

  // =========================
  // Helpers
  // =========================
  private def createTable(sql: String): Unit = {
    val conn = getConnection
    try {
      val st = conn.createStatement()
      st.executeUpdate(sql)
      st.close()
    } catch {
      case e: SQLException =>
        // Derby table already exists: SQLState "X0Y32"
        if (e.getSQLState != "X0Y32") throw e
    } finally conn.close()
  }

  // (kept in case you still use it elsewhere)
  private def sha256(text: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(text.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString
  }
}
