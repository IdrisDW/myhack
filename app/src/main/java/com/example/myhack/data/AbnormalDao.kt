package com.example.myhack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AbnormalDao {
    @Insert
    suspend fun insert(event: AbnormalEvent)

    @Query("SELECT * FROM AbnormalEvent")
    suspend fun getAll(): List<AbnormalEvent>
}
