package com.example.fyp_androidapp.data.repository

import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.database.AppDatabase
import com.example.fyp_androidapp.database.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationsRepository() {

    private val db = DatabaseProvider.getDatabase()


    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mma").withZone(ZoneId.of("Asia/Singapore"))

    private fun formatTimestampToSGT(timestamp: Long): String {
        return try {
            timeFormatter.format(Instant.ofEpochMilli(timestamp)).uppercase()
        } catch (e: Exception) {
            "Unknown Time"
        }
    }

    val notificationsFlow: Flow<List<Notification>> =
        db.notificationDao().observeNotifications().map { entityList ->
            entityList.map {
                Notification(
                    id = it.id,
                    sender = it.appName,
                    title = it.title ?: "No Title",
                    content = it.bigText ?: it.text ?: "No Content",
                    time = formatTimestampToSGT(it.timestamp),
                    isImportant = it.isImportant
                )
            }
        }

    suspend fun updateNotificationImportance(notificationId: String, importance: Int) {
        withContext(Dispatchers.IO) {
            db.notificationDao().updateImportance(notificationId, importance == 1)
        }
    }
}
