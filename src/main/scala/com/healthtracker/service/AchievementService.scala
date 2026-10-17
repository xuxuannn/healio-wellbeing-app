package com.healthtracker.service

import com.healthtracker.Session
import com.healthtracker.database.{AchievementDAO, DB, HabitDAO, ReminderDAO}

import java.time.LocalDate

object AchievementService {

  def currentStreak(): Int = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "SELECT DISTINCT cdate FROM habit_completions WHERE user_id=? ORDER BY cdate DESC"
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()

      val dates = scala.collection.mutable.ArrayBuffer[String]()
      while (rs.next()) {
        dates += rs.getString("cdate")
      }
      rs.close()
      ps.close()

      if (dates.isEmpty) return 0

      val set = dates.toSet
      var streak = 0
      var day = LocalDate.now()

      while (set.contains(day.toString)) {
        streak += 1
        day = day.minusDays(1)
      }
      streak
    } finally conn.close()
  }

  def refreshBadgesAfterHabitCompletion(): Unit = {
    val today = LocalDate.now().toString
    val totalCompletions = totalHabitCompletions()

    if (totalCompletions >= 1)
      AchievementDAO.addBadgeIfNotExists("FIRST_HABIT", "First Habit Completed", today)

    if (totalCompletions >= 5)
      AchievementDAO.addBadgeIfNotExists("HABIT_5_TOTAL", "Completed 5 Habits Total", today)

    if (HabitDAO.todayProgressPercentage() == 100)
      AchievementDAO.addBadgeIfNotExists("ALL_TODAY_100", "Completed All Habits Today (100%)", today)

    val s = currentStreak()
    if (s >= 3) AchievementDAO.addBadgeIfNotExists("STREAK_3", "3-Day Streak", today)
    if (s >= 7) AchievementDAO.addBadgeIfNotExists("STREAK_7", "7-Day Streak", today)
  }

  def refreshBadgesAfterReminderDone(): Unit = {
    val today = LocalDate.now().toString
    val doneCount = ReminderDAO.doneCount()

    if (doneCount >= 5)
      AchievementDAO.addBadgeIfNotExists("REMINDER_5_DONE", "Reminder Master (5 Completed)", today)
  }

  private def totalHabitCompletions(): Int = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM habit_completions WHERE user_id=?")
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      rs.next()
      val c = rs.getInt("c")
      rs.close()
      ps.close()
      c
    } finally conn.close()
  }
}
