package com.healthtracker

object Session {
  var currentUserId: Option[Int] = None
  var currentUsername: Option[String] = None

  def loginAs(id: Int, username: String): Unit = {
    currentUserId = Some(id)
    currentUsername = Some(username)
  }

  def logout(): Unit = {
    currentUserId = None
    currentUsername = None
  }

  def requireUserId(): Int =
    currentUserId.getOrElse(throw new IllegalStateException("No user logged in"))
}
