package com.healthtracker.traits

trait Trackable {
  def isCompleted: Boolean
  def statusText: String =
    if (isCompleted) "Completed" else "Pending"
}
