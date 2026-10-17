package com.healthtracker.database

import com.healthtracker.Session
import com.healthtracker.model.Badge

import java.sql.ResultSet

object AchievementDAO {

  def addBadgeIfNotExists(code: String, name: String, earnedDate: String): Unit = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val check = conn.prepareStatement("SELECT COUNT(*) AS c FROM badges WHERE user_id=? AND code=?")
      check.setInt(1, uid)
      check.setString(2, code)
      val rs = check.executeQuery()
      rs.next()
      val exists = rs.getInt("c") > 0
      rs.close()
      check.close()

      if (!exists) {
        val ps = conn.prepareStatement(
          "INSERT INTO badges(user_id, code, name, earned_date) VALUES(?,?,?,?)"
        )
        ps.setInt(1, uid)
        ps.setString(2, code)
        ps.setString(3, name)
        ps.setString(4, earnedDate)
        ps.executeUpdate()
        ps.close()
      }
    } finally conn.close()
  }

  def latest3Badges(): List[Badge] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "SELECT id, code, name, earned_date FROM badges WHERE user_id=? ORDER BY earned_date DESC, id DESC FETCH FIRST 3 ROWS ONLY"
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  def findAllBadges(): List[Badge] = {
    val uid = Session.requireUserId()
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "SELECT id, code, name, earned_date FROM badges WHERE user_id=? ORDER BY earned_date DESC, id DESC"
      )
      ps.setInt(1, uid)
      val rs = ps.executeQuery()
      val list = toList(rs)
      rs.close()
      ps.close()
      list
    } finally conn.close()
  }

  private def toList(rs: ResultSet): List[Badge] = {
    val b = List.newBuilder[Badge]
    while (rs.next()) {
      b += Badge(
        id = rs.getInt("id"),
        code = rs.getString("code"),
        name = rs.getString("name"),
        earnedDate = rs.getString("earned_date")
      )
    }
    b.result()
  }
}
