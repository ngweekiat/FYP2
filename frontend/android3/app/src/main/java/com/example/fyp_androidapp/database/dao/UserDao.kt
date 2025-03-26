package com.example.fyp_androidapp.database.dao

import androidx.room.*
import com.example.fyp_androidapp.database.entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUserById(uid: String)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("UPDATE users SET accessToken = :newToken WHERE uid = :userId")
    suspend fun updateAccessToken(userId: String, newToken: String)

}
