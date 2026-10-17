package com.healthtracker.model

case class User(
                 id: Int,
                 username: String,
                 passwordHash: String
               )
