package com.example.myhack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorDao {
    @Insert
    suspend fun insert(reading: SensorReading)

    @Query("SELECT * FROM SensorReading WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: String): List<SensorReading>
}
