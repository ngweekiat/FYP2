package com.example.fyp_androidapp.database.dao

import androidx.room.*
import com.example.fyp_androidapp.database.entities.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun observeNotifications(): kotlinx.coroutines.flow.Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationById(id: String): NotificationEntity?

    @Query("UPDATE notifications SET isImportant = :isImportant WHERE id = :id")
    suspend fun updateImportance(id: String, isImportant: Boolean)

    @Query("""
    SELECT * FROM notifications
    WHERE groupKey = :groupKey
      AND title = :title
      AND timestamp < :beforeTimestamp
    ORDER BY timestamp DESC
    LIMIT :limit
""")
    suspend fun getPreviousMessagesByGroupAndTitle(
        groupKey: String,
        title: String,
        beforeTimestamp: Long,
        limit: Int = 10
    ): List<NotificationEntity>

}