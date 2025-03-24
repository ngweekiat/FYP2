package com.example.fyp_androidapp.utils

import android.util.Log
import com.example.fyp_androidapp.backend.llm.LlmEventExtractor
import com.example.fyp_androidapp.backend.llm.LlmEventImportance
import com.example.fyp_androidapp.database.AppDatabase
import com.example.fyp_androidapp.database.entities.EventEntity
import com.example.fyp_androidapp.database.entities.NotificationEntity
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object NotificationETL {

    suspend fun processNotificationImportance(
        database: AppDatabase,
        entity: NotificationEntity
    ) {
        try {
            val fullText = listOfNotNull(entity.title, entity.text, entity.bigText).joinToString(" ")
            val importance = LlmEventImportance.detectEventImportance(fullText)

            if (importance == 1) {
                Log.d("NotificationProcessor", "📌 Important event detected by LLM: ${entity.id}")
                database.notificationDao().updateImportance(entity.id, true)

                val timestampLong = entity.timestamp?.toLongOrNull() ?: 0L // Default to 0L if invalid
                val isoTimestamp = Instant.ofEpochMilli(timestampLong)
                    .atZone(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT)

                val eventJson: JSONObject? = LlmEventExtractor.extractEventDetails(fullText, isoTimestamp)

                if (eventJson != null) {
                    Log.d("NotificationProcessor", "📅 Extracted event: $eventJson")

                    val eventEntity = jsonToEventEntity(eventJson, entity.id)
                    database.eventDao().insertEvent(eventEntity)

                    Log.d("NotificationProcessor", "✅ Event saved to DB: ${eventEntity.title}")
                } else {
                    Log.w("NotificationProcessor", "⚠️ No valid event extracted from LLM.")
                }
            }

        } catch (e: Exception) {
            Log.e("NotificationProcessor", "❌ Failed to process LLM: ${e.message}", e)
        }
    }

    private fun jsonToEventEntity(json: JSONObject, notificationId: String): EventEntity {
        return EventEntity(
            id = notificationId,  // <- use notification ID as primary key
            title = json.getString("title"),
            description = json.optString("description", ""),
            startDate = json.getString("start_date"),
            startTime = json.getString("start_time"),
            endDate = json.getString("end_date"),
            endTime = json.getString("end_time"),
            allDay = json.getBoolean("all_day_event")
        )
    }
}
