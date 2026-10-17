package com.healthtracker.service

import com.healthtracker.database.{AchievementDAO, HabitDAO, ReminderDAO}
import com.healthtracker.model.{Badge, Habit, Reminder}

object DashboardService {

  private val motivations = Vector(
    "Small steps every day.",
    "You don’t need perfect. You need progress.",
    "Today is a good day to start again.",
    "Do it for your future self.",
    "Consistency beats intensity."
  )

  def pendingReminderCount: Int =
    ReminderDAO.pendingCount()

  def next3Reminders: List[Reminder] =
    ReminderDAO.next3Pending()

  // ✅ MUST match HabitDAO method name exactly
  def habitsNotCompletedToday: List[Habit] =
    HabitDAO.habitsNotCompletedToday()

  def currentStreak: Int =
    AchievementService.currentStreak()

  def latest3Badges: List[Badge] =
    AchievementDAO.latest3Badges()

  def randomMotivation: String =
    motivations(scala.util.Random.nextInt(motivations.size))
}
