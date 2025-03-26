// com.example.fyp_androidapp.database.entities.EventEntity.kt
package com.example.fyp_androidapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val allDay: Boolean,
    val location: String,
    val buttonStatus: Int
)
