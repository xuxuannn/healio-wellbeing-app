package com.healthtracker.controller

import com.healthtracker.database.{HabitDAO, MoodDAO, ReminderDAO}
import com.healthtracker.service.{AchievementService, DashboardService}
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.{BarChart, LineChart, XYChart}
import javafx.scene.control.{Label, ListView}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

class InsightsController {

  // ===== Weekly summary cards =====
  @FXML private var weeklyHabitScoreLabel: Label = _
  @FXML private var weeklyHabitAvgLabel: Label = _
  @FXML private var bestDayLabel: Label = _
  @FXML private var bestDaySubLabel: Label = _
  @FXML private var weeklyReminderMainLabel: Label = _
  @FXML private var weeklyReminderSubLabel: Label = _
  @FXML private var weeklyTipLabel: Label = _

  // ===== Keep current streak =====
  @FXML private var streakLabel: Label = _

  // ===== Mood + tip + list =====
  @FXML private var moodLabel: Label = _
  @FXML private var tipLabel: Label = _
  @FXML private var nextReminderList: ListView[String] = _

  // ===== Charts =====
  @FXML private var habitLineChart: LineChart[String, Number] = _
  @FXML private var reminderBarChart: BarChart[String, Number] = _

  @FXML def initialize(): Unit = refresh()

  @FXML def refresh(): Unit = {
    // ----------------------------
    // Week range (Mon -> Sun)
    // ----------------------------
    val today = LocalDate.now()
    val weekStart = today.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = today.`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    val startStr = weekStart.toString
    val endStr = weekEnd.toString

    // ----------------------------
    // Weekly Habit Score (total completions, avg per day)
    // ----------------------------
    val last7Start = today.minusDays(6)
    val last7StartStr = last7Start.toString
    val last7EndStr = today.toString

    val countsMap = HabitDAO.completionCountByDate(last7StartStr, last7EndStr)

    // Fill 7 days list (including 0)
    val days = (0 to 6).map(i => last7Start.plusDays(i.toLong))
    val counts = days.map(d => countsMap.getOrElse(d.toString, 0))
    val totalCompletions7 = counts.sum
    val avgPerDay = totalCompletions7.toDouble / 7.0

    weeklyHabitScoreLabel.setText(s"$totalCompletions7 completions")
    weeklyHabitAvgLabel.setText(f"Avg per day: $avgPerDay%.1f")

    // Best Day
    val max = if (counts.isEmpty) 0 else counts.max
    if (max == 0) {
      bestDayLabel.setText("—")
      bestDaySubLabel.setText("0 completions")
    } else {
      val bestIndex = counts.indexOf(max)
      val bestDate = days(bestIndex)

      val dayName = bestDate.getDayOfWeek.toString.toLowerCase.capitalize
      bestDayLabel.setText(dayName)
      bestDaySubLabel.setText(s"$max completions")
    }

    // ----------------------------
    // Weekly Reminders (Mon-Sun) - Done vs Pending
    // ----------------------------
    val (doneW, pendingW) = ReminderDAO.statusCountBetween(startStr, endStr)
    weeklyReminderMainLabel.setText(s"Done: $doneW | Pending: $pendingW")
    weeklyReminderSubLabel.setText(s"Total scheduled: ${doneW + pendingW}")

    // ----------------------------
    // Keep current streak
    // ----------------------------
    val s = AchievementService.currentStreak()
    streakLabel.setText(s"Current streak: $s days")

    // ----------------------------
    // Mood + smart tip
    // ----------------------------
    val mood = MoodDAO.getTodayMood().getOrElse("—")
    val prettyMood = mood match {
      case "Happy"   => "😊 Happy"
      case "Neutral" => "😐 Neutral"
      case "Sad"     => "😞 Sad"
      case "Angry"   => "😡 Angry"
      case other     => other
    }
    moodLabel.setText(prettyMood)

    val tip = mood match {
      case "Happy"   => "Keep the momentum — set one small goal now."
      case "Neutral" => "Try completing 1 habit today to maintain your streak."
      case "Sad"     => "Start with an easy habit. Small wins matter."
      case "Angry"   => "Pause 30 seconds, breathe, then do one quick task."
      case _         => DashboardService.randomMotivation
    }
    tipLabel.setText(tip)

    // Weekly tip under charts
    val pendingHabitsToday = HabitDAO.habitsNotCompletedToday().size
    weeklyTipLabel.setText(s"You still have $pendingHabitsToday pending habit(s) today. Keep going!")

    // ----------------------------
    // Chart 1: Habit Completion (Last 7 Days)
    // ----------------------------
    habitLineChart.getData.clear()
    val series1 = new XYChart.Series[String, Number]()
    days.zip(counts).foreach { case (d, c) =>
      val label = d.format(DateTimeFormatter.ofPattern("MM-dd"))
      series1.getData.add(new XYChart.Data[String, Number](label, c))
    }
    habitLineChart.getData.add(series1)

    // ----------------------------
    // Chart 2 (B1): Reminder Status (This Week)
    // ----------------------------
    reminderBarChart.getData.clear()
    val series2 = new XYChart.Series[String, Number]()
    series2.getData.add(new XYChart.Data[String, Number]("Done", doneW))
    series2.getData.add(new XYChart.Data[String, Number]("Pending", pendingW))
    reminderBarChart.getData.add(series2)

    // ----------------------------
    // Next 3 reminders list
    // ----------------------------
    val next3 = DashboardService.next3Reminders.map { r =>
      s"${r.rdate} - ${r.title} (${r.rtype}) [${r.priority}]"
    }
    nextReminderList.setItems(FXCollections.observableArrayList(next3: _*))
  }
}
