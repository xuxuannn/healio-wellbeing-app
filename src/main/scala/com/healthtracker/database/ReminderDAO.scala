package com.healthtracker.database

import com.healthtracker.Session
import com.healthtracker.model.Reminder

import java.sql.ResultSet

object ReminderDAO {

  def add(title: String, rtype: String, rdate: String, priority: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "INSERT INTO reminders(user_id, title, rtype, rdate, priority, is_done) VALUES(?,?,?,?,?,0)"
      )
      ps.setInt(1, uid)
      ps.setString(2, title)
      ps.setString(3, rtype)
      ps.setString(4, rdate)
      ps.setString(5, Option(priority).getOrElse("Medium"))
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  def update(id: Int, title: String, rtype: String, rdate: String, priority: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "UPDATE reminders SET title=?, rtype=?, rdate=?, priority=? WHERE id=? AND user_id=?"
      )
      ps.setString(1, title)
      ps.setString(2, rtype)
      ps.setString(3, rdate)
      ps.setString(4, Option(priority).getOrElse("Medium"))
      ps.setInt(5, id)
      ps.setInt(6, uid)
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  def findAll(): List[Reminder] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |SELECT id, title, rtype, rdate, priority, is_done
          |FROM reminders
          |WHERE user_id=?
          |ORDER BY
          |  CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END,
          |  rdate ASC, id DESC
          |""".stripMargin
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  def pendingCount(): Int = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT COUNT(*) FROM reminders WHERE user_id=? AND is_done=0")
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      rs.next()
      val c = rs.getInt(1)
      rs.close()
      ps.close()
      c
    } finally conn.close()
  }

  def next3Pending(): List[Reminder] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |SELECT id, title, rtype, rdate, priority, is_done
          |FROM reminders
          |WHERE user_id=? AND is_done=0
          |ORDER BY
          |  CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END,
          |  rdate ASC, id ASC
          |FETCH FIRST 3 ROWS ONLY
          |""".stripMargin
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  def markDone(id: Int, done: Boolean): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("UPDATE reminders SET is_done=? WHERE user_id=? AND id=?")
      ps.setInt(1, if (done) 1 else 0)
      ps.setInt(2, uid)
      ps.setInt(3, id)
      ps.executeUpdate()
      ps.close()
    } finally conn.close()
  }

  def doneCount(): Int = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT COUNT(*) FROM reminders WHERE user_id=? AND is_done=1")
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      rs.next()
      val c = rs.getInt(1)
      rs.close()
      ps.close()
      c
    } finally conn.close()
  }

  def statusCountBetween(startDate: String, endDate: String): (Int, Int) = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        """
          |SELECT
          |  SUM(CASE WHEN is_done=1 THEN 1 ELSE 0 END) AS done_count,
          |  SUM(CASE WHEN is_done=0 THEN 1 ELSE 0 END) AS pending_count
          |FROM reminders
          |WHERE user_id=? AND rdate >= ? AND rdate <= ?
          |""".stripMargin
      )
      ps.setInt(1, uid)
      ps.setString(2, startDate)
      ps.setString(3, endDate)
      val rs = ps.executeQuery()
      rs.next()

      val done = rs.getInt("done_count")
      val pending = rs.getInt("pending_count")

      rs.close()
      ps.close()

      (done, pending)
    } finally conn.close()
  }

  private def toList(rs: ResultSet): List[Reminder] = {
    val b = List.newBuilder[Reminder]
    while (rs.next()) {
      val pr = Option(rs.getString("priority")).getOrElse("Medium")
      b += Reminder(
        id = rs.getInt("id"),
        title = rs.getString("title"),
        rtype = rs.getString("rtype"),
        rdate = rs.getString("rdate"),
        priority = pr,
        isDone = rs.getInt("is_done") == 1
      )
    }
    b.result()
  }
}
