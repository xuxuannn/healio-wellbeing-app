package com.healthtracker.model

import com.healthtracker.traits.Trackable

case class Habit(
                  id: Int,
                  title: String,
                  htype: String,
                  completedToday: Boolean = false
                ) extends Trackable {

  override def isCompleted: Boolean = completedToday
}
