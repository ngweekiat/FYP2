package com.example.fyp_androidapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fyp_androidapp.database.dao.EventDao
import com.example.fyp_androidapp.database.dao.NotificationDao
import com.example.fyp_androidapp.database.dao.UserDao
import com.example.fyp_androidapp.database.entities.EventEntity
import com.example.fyp_androidapp.database.entities.NotificationEntity
import com.example.fyp_androidapp.database.entities.UserEntity

@Database(entities = [NotificationEntity::class, EventEntity::class, UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eventify_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
