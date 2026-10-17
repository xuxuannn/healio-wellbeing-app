package com.healthtracker.model

import com.healthtracker.traits.{Trackable, Prioritizable}

case class Reminder(
                     id: Int,
                     title: String,
                     rtype: String,
                     rdate: String,
                     priority: String,
                     isDone: Boolean
                   ) extends Trackable with Prioritizable {

  override def isCompleted: Boolean = isDone
}
