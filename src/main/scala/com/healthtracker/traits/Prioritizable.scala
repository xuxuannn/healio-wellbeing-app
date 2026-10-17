package com.healthtracker.traits

trait Prioritizable {
  def priority: String

  def priorityColor: String = priority match {
    case "High"   => "#FFE8E8"   // red-ish
    case "Medium" => "#FFF6E8"   // yellow-ish
    case "Low"    => "#E8FFF4"   // green-ish
    case _        => "transparent"
  }
}
