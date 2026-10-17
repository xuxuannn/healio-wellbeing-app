package com.healthtracker.database

import com.healthtracker.Session
import com.healthtracker.model.Habit

import java.sql.ResultSet
import java.time.LocalDate
import scala.collection.mutable

object HabitDAO {

  // =========================
  // CREATE
  // =========================
  def add(title: String, htype: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("INSERT INTO habits(user_id, title, htype) VALUES(?,?,?)")
      ps.setInt(1, uid)
      ps.setString(2, title)
      ps.setString(3, htype)
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  // =========================
  // READ
  // =========================
  def findAll(): List[Habit] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "SELECT id, title, htype FROM habits WHERE user_id=? ORDER BY id DESC"
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  def habitsNotCompletedToday(): List[Habit] = {
    val uid = Session.requireUserId()
    val today = LocalDate.now().toString
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |SELECT h.id, h.title, h.htype
          |FROM habits h
          |LEFT JOIN habit_completions c
          |  ON h.id = c.habit_id AND c.cdate = ? AND c.user_id = ?
          |WHERE h.user_id = ?
          |  AND c.habit_id IS NULL
          |ORDER BY h.id DESC
          |""".stripMargin
      )
      ps.setString(1, today)
      ps.setInt(2, uid)
      ps.setInt(3, uid)

      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  // =========================
  // UPDATE
  // =========================
  def update(id: Int, title: String, htype: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "UPDATE habits SET title=?, htype=? WHERE id=? AND user_id=?"
      )
      ps.setString(1, title)
      ps.setString(2, htype)
      ps.setInt(3, id)
      ps.setInt(4, uid)
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  // =========================
  // DELETE
  // =========================
  def deleteById(habitId: Int): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps1 = conn.prepareStatement("DELETE FROM habit_completions WHERE user_id=? AND habit_id=?")
      ps1.setInt(1, uid)
      ps1.setInt(2, habitId)
      ps1.executeUpdate()
      ps1.close()

      val ps2 = conn.prepareStatement("DELETE FROM habits WHERE user_id=? AND id=?")
      ps2.setInt(1, uid)
      ps2.setInt(2, habitId)
      ps2.executeUpdate()
      ps2.close()
    } finally conn.close()
  }

  // =========================
  // MARK COMPLETED TODAY (Derby-safe)
  // =========================
  def markCompletedToday(habitId: Int): Unit = {
    val uid = Session.requireUserId()
    val today = LocalDate.now().toString
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |INSERT INTO habit_completions(user_id, habit_id, cdate)
          |SELECT ?, ?, ?
          |FROM SYSIBM.SYSDUMMY1
          |WHERE NOT EXISTS (
          |  SELECT 1 FROM habit_completions
          |  WHERE user_id=? AND habit_id=? AND cdate=?
          |)
          |""".stripMargin
      )
      ps.setInt(1, uid)
      ps.setInt(2, habitId)
      ps.setString(3, today)
      ps.setInt(4, uid)
      ps.setInt(5, habitId)
      ps.setString(6, today)
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  def isCompletedToday(habitId: Int): Boolean = {
    val uid = Session.requireUserId()
    val today = LocalDate.now().toString
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "SELECT COUNT(*) FROM habit_completions WHERE user_id=? AND habit_id=? AND cdate=?"
      )
      ps.setInt(1, uid)
      ps.setInt(2, habitId)
      ps.setString(3, today)
      val rs = ps.executeQuery()
      rs.next()
      val ok = rs.getInt(1) > 0
      rs.close()
      ps.close()
      ok
    } finally conn.close()
  }

  def todayProgressPercentage(): Int = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val totalPs = conn.prepareStatement("SELECT COUNT(*) FROM habits WHERE user_id=?")
      totalPs.setInt(1, uid)
      val totalRs = totalPs.executeQuery()
      totalRs.next()
      val total = totalRs.getInt(1)
      totalRs.close()
      totalPs.close()

      if (total == 0) return 0

      val today = LocalDate.now().toString
      val donePs = conn.prepareStatement(
        "SELECT COUNT(DISTINCT habit_id) FROM habit_completions WHERE user_id=? AND cdate=?"
      )
      donePs.setInt(1, uid)
      donePs.setString(2, today)
      val doneRs = donePs.executeQuery()
      doneRs.next()
      val done = doneRs.getInt(1)
      doneRs.close()
      donePs.close()

      ((done.toDouble / total.toDouble) * 100).toInt
    } finally conn.close()
  }

  def completionCountByDate(startDate: String, endDate: String): Map[String, Int] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |SELECT cdate, COUNT(*) AS c
          |FROM habit_completions
          |WHERE user_id=? AND cdate >= ? AND cdate <= ?
          |GROUP BY cdate
          |ORDER BY cdate
          |""".stripMargin
      )
      ps.setInt(1, uid)
      ps.setString(2, startDate)
      ps.setString(3, endDate)
      val rs = ps.executeQuery()

      val m = mutable.Map.empty[String, Int]
      while (rs.next()) {
        m += rs.getString("cdate") -> rs.getInt("c")
      }
      rs.close()
      ps.close()
      m.toMap
    } finally conn.close()
  }

  private def toList(rs: ResultSet): List[Habit] = {
    val b = List.newBuilder[Habit]
    while (rs.next()) {
      b += Habit(
        id = rs.getInt("id"),
        title = rs.getString("title"),
        htype = rs.getString("htype")
      )
    }
    b.result()
  }
}
