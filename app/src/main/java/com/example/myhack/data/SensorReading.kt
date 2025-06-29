package com.example.myhack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SensorReading")
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val sensorId: Int,
    val pressure: Int,
    val timestamp: Long,

    val sessionId: String  // Add this to support filtering by session
)
