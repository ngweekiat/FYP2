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
    private val userDao: UserDao
) {
    private val client = OkHttpClient()

    suspend fun upsertEventToGoogleCalendar(eventId: String, eventDetails: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            val users = userDao.getAllUsers()
            var allSuccessful = true

            for (user in users) {
                val accessToken = user.accessToken
                if (accessToken.isNullOrEmpty()) {
                    Log.e("CalendarAPI", "Access token missing for user: ${user.uid}")
                    allSuccessful = false
                    continue
                }

                val eventJson = JSONObject().apply {
                    put("summary", eventDetails.title)
                    put("location", eventDetails.locationOrMeeting ?: "")
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
                    if (updateResponse.isSuccessful) {
                        Log.d("CalendarAPI", "Event updated for user ${user.uid}")
                    } else if (updateResponse.code == 404) {
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
                            if (insertResponse.isSuccessful) {
                                Log.d("GoogleCalendarApiRepository", "Event inserted for user ${user.uid}")
                            } else {
                                val errorBody = insertResponse.body?.string()
                                logErrorJson("insert", user.uid, insertResponse.code, errorBody)
                                allSuccessful = false
                            }
                        }
                    } else {
                        val errorBody = updateResponse.body?.string()
                        logErrorJson("update", user.uid, updateResponse.code, errorBody)
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
                val accessToken = user.accessToken
                if (accessToken.isNullOrEmpty()) {
                    Log.e("CalendarAPI", "Access token missing for user: ${user.uid}")
                    allSuccessful = false
                    continue
                }

                val request = Request.Builder()
                    .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .delete()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("GoogleCalendarApiRepository", "Event deleted for user ${user.uid}")
                    } else {
                        val errorBody = response.body?.string()
                        logErrorJson("delete", user.uid, response.code, errorBody)
                        allSuccessful = false
                    }
                }
            }

            return@withContext allSuccessful
        }
    }

    private fun logErrorJson(action: String, uid: String, code: Int, errorBody: String?) {
        try {
            val errorJson = JSONObject(errorBody ?: "")
            Log.e(
                "GoogleCalendarApiRepository",
                "Failed to $action event for user $uid: $code\n${errorJson.toString(4)}"
            )
        } catch (e: Exception) {
            Log.e(
                "GoogleCalendarApiRepository",
                "Failed to $action event for user $uid: $code\nRaw error: $errorBody"
            )
        }
    }

    private fun formatTime(time: String): String {
        return if (time.length == 5) "$time:00" else time
    }
}
