package com.example.fyp_androidapp.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,           // Firebase UID as primary key
    val email: String,
    val displayName: String,
    val idToken: String?,
    val authCode: String?,
    val accessToken: String?,
    val refreshToken: String?
)
