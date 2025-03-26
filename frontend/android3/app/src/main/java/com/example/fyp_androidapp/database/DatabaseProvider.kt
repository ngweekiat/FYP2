package com.example.fyp_androidapp.database

import android.content.Context
import androidx.room.Room
import com.example.fyp_androidapp.database.dao.UserDao

object DatabaseProvider {
    private lateinit var database: AppDatabase

    fun init(context: Context) {
        if (!::database.isInitialized) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "eventify.db"
            ).build()
        }
    }

    fun getDatabase(): AppDatabase {
        if (!::database.isInitialized) {
            throw IllegalStateException("DatabaseProvider not initialized. Call init() first.")
        }
        return database
    }

    fun getUserDao(): UserDao {
        return getDatabase().userDao()
    }
}
