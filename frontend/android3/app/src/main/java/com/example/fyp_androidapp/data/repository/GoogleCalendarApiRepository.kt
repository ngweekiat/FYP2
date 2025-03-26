package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.database.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GoogleCalendarApiRepository(
    private val userDao: UserDao,
    private val authRepository: AuthRepository
) {
    private val client = OkHttpClient()
    private val TAG = "GoogleCalendarSync"

    suspend fun upsertEventToGoogleCalendar(eventId: String, eventDetails: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            val users = userDao.getAllUsers()
            var allSuccessful = true

            for (user in users) {
                val accessToken = getValidAccessToken(user.uid, user.accessToken, user.refreshToken)
                if (accessToken.isNullOrEmpty()) {
                    Log.e(TAG, "Failed to get valid access token for user: ${user.uid}")
                    allSuccessful = false
                    continue
                }

                val eventJson = JSONObject().apply {
                    put("summary", eventDetails.title)
                    put("location", eventDetails.location ?: "")
                    put("description", eventDetails.description ?: "")
                    put("start", JSONObject().apply {
                        put("dateTime", "${eventDetails.startDate}T${formatTime(eventDetails.startTime)}")
                        put("timeZone", "Asia/Singapore")
                    })
                    put("end", JSONObject().apply {
                        put("dateTime", "${eventDetails.endDate ?: eventDetails.startDate}T${formatTime(eventDetails.endTime ?: eventDetails.startTime)}")
                        put("timeZone", "Asia/Singapore")
                    })
                }

                val updateRequest = Request.Builder()
                    .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .put(eventJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                client.newCall(updateRequest).execute().use { updateResponse ->
                    Log.d(TAG, "⬆️ Update JSON for user ${user.uid}: ${eventJson.toString(4)}")

                    val responseCode = updateResponse.code
                    val responseBody = updateResponse.body?.string()

                    Log.d(TAG, "Update response (${user.uid}): $responseCode")

                    if (updateResponse.isSuccessful) {
                        Log.d(TAG, "✅ Event updated for user ${user.uid}")
                    } else if (responseCode == 404) {
                        val insertJson = JSONObject(eventJson.toString()).apply {
                            put("id", eventId)
                        }

                        val insertRequest = Request.Builder()
                            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                            .addHeader("Authorization", "Bearer $accessToken")
                            .addHeader("Content-Type", "application/json")
                            .post(insertJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                            .build()

                        client.newCall(insertRequest).execute().use { insertResponse ->
                            Log.d(TAG, "➕ Insert JSON for user ${user.uid}: ${insertJson.toString(4)}")

                            val insertCode = insertResponse.code
                            val insertBody = insertResponse.body?.string()

                            Log.d(TAG, "Insert response (${user.uid}): $insertCode")

                            if (insertResponse.isSuccessful) {
                                Log.d(TAG, "✅ Event inserted for user ${user.uid}")
                            } else {
                                logErrorJson("insert", user.uid, insertCode, insertBody)
                                allSuccessful = false
                            }
                        }
                    } else {
                        logErrorJson("update", user.uid, responseCode, responseBody)
                        allSuccessful = false
                    }
                }
            }

            return@withContext allSuccessful
        }
    }

    suspend fun deleteEventFromGoogleCalendar(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val users = userDao.getAllUsers()
            var allSuccessful = true

            for (user in users) {
                val accessToken = getValidAccessToken(user.uid, user.accessToken, user.refreshToken)
                if (accessToken.isNullOrEmpty()) {
                    Log.e(TAG, "Failed to get valid access token for user: ${user.uid}")
                    allSuccessful = false
                    continue
                }

                val request = Request.Builder()
                    .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .delete()
                    .build()

                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val body = response.body?.string()

                    Log.d(TAG, "Delete response (${user.uid}): $code")

                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ Event deleted for user ${user.uid}")
                    } else {
                        logErrorJson("delete", user.uid, code, body)
                        allSuccessful = false
                    }
                }
            }

            return@withContext allSuccessful
        }
    }

    private suspend fun getValidAccessToken(userId: String, currentToken: String?, refreshToken: String?): String? {
        if (currentToken.isNullOrEmpty()) {
            Log.e(TAG, "Current token is null or empty for user: $userId")
            return null
        }

        val testRequest = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/users/me/calendarList")
            .addHeader("Authorization", "Bearer $currentToken")
            .build()

        client.newCall(testRequest).execute().use { response ->
            if (response.isSuccessful) {
                return currentToken
            } else if (response.code == 401) {
                Log.w(TAG, "Access token expired for user $userId. Attempting refresh...")

                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available for user: $userId")
                } else {
                    val newToken = authRepository.refreshAccessToken(refreshToken)
                    if (!newToken.isNullOrEmpty()) {
                        userDao.updateAccessToken(userId, newToken)
                        Log.d(TAG, "Access token refreshed for user: $userId")
                        return newToken
                    } else {
                        Log.e(TAG, "Failed to refresh access token for user: $userId")
                    }
                }

                // Fallback: Proceed with possibly expired token
                return currentToken.also {
                    Log.w(TAG, "Proceeding with possibly expired access token for user: $userId")
                }
            } else {
                Log.e(TAG, "Access token check failed with code ${response.code} for user: $userId")
            }
        }

        return currentToken.also {
            Log.w(TAG, "Proceeding with possibly expired access token for user: $userId")
        }
    }



    private fun logErrorJson(action: String, uid: String, code: Int, errorBody: String?) {
        try {
            val errorJson = JSONObject(errorBody ?: "")
            Log.e(TAG, "❌ Failed to $action event for user $uid: $code\n${errorJson.toString(4)}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to $action event for user $uid: $code\nRaw error: $errorBody")
        }
    }

    private fun formatTime(time: String): String {
        return if (time.length == 5) "$time:00" else time
    }
}
