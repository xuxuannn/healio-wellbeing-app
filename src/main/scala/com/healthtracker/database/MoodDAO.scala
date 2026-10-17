package com.healthtracker.database

import com.healthtracker.Session
import java.time.LocalDate

object MoodDAO {

  def getTodayMood(): Option[String] =
    getMood(LocalDate.now().toString)

  def setTodayMood(mood: String): Unit =
    setMood(LocalDate.now().toString, mood)

  def getMood(date: String): Option[String] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT mood FROM daily_mood WHERE user_id=? AND mdate=?")
      ps.setInt(1, uid)
      ps.setString(2, date)
      val rs = ps.executeQuery()
      val result =
        if (rs.next()) Option(rs.getString("mood")) else None
      rs.close()
      ps.close()
      result
    } finally conn.close()
  }

  def setMood(date: String, mood: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val update = conn.prepareStatement(
        "UPDATE daily_mood SET mood=? WHERE user_id=? AND mdate=?"
      )
      update.setString(1, mood)
      update.setInt(2, uid)
      update.setString(3, date)
      val updated = update.executeUpdate()
      update.close()

      if (updated == 0) {
        val insert = conn.prepareStatement(
          "INSERT INTO daily_mood(user_id, mdate, mood) VALUES(?,?,?)"
        )
        insert.setInt(1, uid)
        insert.setString(2, date)
        insert.setString(3, mood)
        insert.executeUpdate()
        insert.close()
      }
    } finally conn.close()
  }
}
