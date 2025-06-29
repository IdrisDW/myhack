package com.example.myhack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AbnormalEvent")
data class AbnormalEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sensorId: Int,
    val pressure: Int,
    val timestamp: Long
)
