package com.example.fyp_androidapp.data.database

import Notification
import androidx.room.*

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<Notification>

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): Notification?

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)

    // Add other methods as needed (e.g., update, filter, etc.)
}