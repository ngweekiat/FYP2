package com.example.fyp_androidapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val infoText: String?,
    val summaryText: String?,
    val bigText: String?,
    val category: String?,
    val showWhen: String?,
    val channelId: String?,
    val peopleList: String?, // Stored as comma-separated string
    val template: String?,
    val remoteInputHistory: String?, // Stored as comma-separated string
    val visibility: String?,
    val priority: String?,
    val flags: String?,
    val color: String?,
    val sound: String?,
    val vibrate: String?,
    val audioStreamType: String?,
    val contentView: String?,
    val bigContentView: String?,
    val groupKey: String?,
    val group: String?,
    val overrideGroupKey: String?,
    val isOngoing: String?,
    val isClearable: String?,
    val userHandle: String?,
    val timestamp: Long,
    val isImportant: Boolean = false
)
