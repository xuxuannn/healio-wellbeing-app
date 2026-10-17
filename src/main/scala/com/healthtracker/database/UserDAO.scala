package com.healthtracker.database

import java.security.MessageDigest
import java.time.LocalDate

object UserDAO {

  private def sha256(text: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(text.getBytes("UTF-8"))
    bytes.map("%02x".format(_)).mkString
  }

  def usernameExists(username: String): Boolean = {
    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username=?")
      ps.setString(1, username)
      val rs = ps.executeQuery()
      rs.next()
      val exists = rs.getInt(1) > 0
      rs.close()
      ps.close()
      exists
    } finally conn.close()
  }

  def register(username: String, password: String): Boolean = {
    val u = Option(username).getOrElse("").trim
    val p = Option(password).getOrElse("").trim
    if (u.isEmpty || p.isEmpty) return false
    if (usernameExists(u)) return false

    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement(
        "INSERT INTO users(username, password_hash, created_at) VALUES(?,?,?)"
      )
      ps.setString(1, u)
      ps.setString(2, sha256(p))
      ps.setString(3, LocalDate.now().toString)
      ps.executeUpdate()
      ps.close()
      true
    } catch {
      case _: Exception => false
    } finally conn.close()
  }

  /**
   * ✅ Login check: return Some(userId) if valid, else None
   */
  def validateLogin(username: String, password: String): Option[Int] = {
    val u = Option(username).getOrElse("").trim
    val p = Option(password).getOrElse("").trim
    if (u.isEmpty || p.isEmpty) return None

    val conn = DB.getConnection
    try {
      val ps = conn.prepareStatement("SELECT id, password_hash FROM users WHERE username=?")
      ps.setString(1, u)
      val rs = ps.executeQuery()

      val result =
        if (rs.next()) {
          val id = rs.getInt("id")
          val stored = rs.getString("password_hash")
          if (stored == sha256(p)) Some(id) else None
        } else None

      rs.close()
      ps.close()
      result
    } finally conn.close()
  }
}
