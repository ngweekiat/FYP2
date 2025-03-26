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
            // 1. Check importance using ONLY the current message
            val currentText = listOfNotNull(entity.title, entity.text, entity.bigText).joinToString(" ")
            val importance = LlmEventImportance.detectEventImportance(currentText)

            if (importance == 1) {
                Log.d("NotificationProcessor", "📌 Important event detected by LLM: ${entity.id}")
                database.notificationDao().updateImportance(entity.id, true)

                val isoTimestamp = Instant.ofEpochMilli(entity.timestamp)
                    .atZone(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT)

                // 2. Build extended context using previous 10 messages from same group
                val groupMessages = if (!entity.groupKey.isNullOrBlank() && !entity.title.isNullOrBlank()) {
                    database.notificationDao().getPreviousMessagesByGroupAndTitle(
                        groupKey = entity.groupKey!!,
                        title = entity.title!!,
                        beforeTimestamp = entity.timestamp
                    )
                } else emptyList()

// 🧠 Build context with timestamps
                val contextText = groupMessages
                    .sortedBy { it.timestamp }
                    .joinToString("\n") { msg ->
                        val msgTimestamp = Instant.ofEpochMilli(msg.timestamp)
                            .atZone(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)

                        "[$msgTimestamp] ${listOfNotNull(msg.title, msg.text, msg.bigText).joinToString(" ")}"
                    }

                val currentText = listOfNotNull(entity.title, entity.text, entity.bigText).joinToString(" ")
                val currentTimestamp = Instant.ofEpochMilli(entity.timestamp)
                    .atZone(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT)

                val fullTextWithHistory = """
    Conversation History:
    $contextText

    [${currentTimestamp}] $currentText
""".trimIndent()



                // 3. Extract event with LLM using the extended context
                val eventJson: JSONObject? = LlmEventExtractor.extractEventDetails(fullTextWithHistory, isoTimestamp)

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
            allDay = json.getBoolean("all_day_event"),
            location = json.optString("location", ""),
            buttonStatus = 0
        )
    }
}
