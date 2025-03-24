// com.example.fyp_androidapp.database.dao.EventDao.kt
package com.example.fyp_androidapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.fyp_androidapp.database.entities.EventEntity

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: EventEntity)
}
