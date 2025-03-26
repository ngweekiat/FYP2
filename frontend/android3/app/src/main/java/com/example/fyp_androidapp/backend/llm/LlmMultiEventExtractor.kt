package com.example.fyp_androidapp.backend.llm

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object LlmMultiEventExtractor {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Constants.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
            topK = 20
            topP = 0.9f
            maxOutputTokens = 1024
            responseMimeType = "text/plain"
        }
    )

    suspend fun extractMultipleEvents(notificationText: String, receivedAtTimestamp: String): List<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(notificationText, receivedAtTimestamp)
                val chat = model.startChat()
                val response = chat.sendMessage(prompt)

                val content = response.text?.trim()
                Log.d("GeminiPrompt", "📥 Prompt:\n$prompt")
                Log.d("LlmMultiEventExtractor", "📤 Gemini Response:\n$content")

                if (content.isNullOrBlank()) return@withContext emptyList()

                val jsonStart = content.indexOf('[')
                val jsonEnd = content.lastIndexOf(']')

                if (jsonStart == -1 || jsonEnd == -1) {
                    Log.e("LlmMultiEventExtractor", "No valid JSON array found")
                    return@withContext emptyList()
                }

                val jsonArray = JSONArray(content.substring(jsonStart, jsonEnd + 1))
                return@withContext List(jsonArray.length()) { jsonArray.getJSONObject(it) }

            } catch (e: Exception) {
                Log.e("LlmMultiEventExtractor", "Failed to extract multiple events: ${e.message}", e)
                emptyList()
            }
        }
    }

    private fun buildPrompt(notificationText: String, receivedAtTimestamp: String): String {
        return """
        You are a smart assistant that extracts **all calendar events** mentioned in a block of text or chat history.

        Return the result as a **JSON array** of objects. Each object must have this format:
        {
          "title": "Event Title",
          "description": "Optional event description",
          "location": "Event location or meeting link",
          "all_day_event": false,
          "start_date": "YYYY-MM-DD",
          "start_time": "HH:MM",
          "end_date": "YYYY-MM-DD",
          "end_time": "HH:MM"
        }

        Rules:
        1. Extract all event-like structures, not just the last one.
        2. Interpret relative dates like "tomorrow" based on: "$receivedAtTimestamp".
        3. If end time is missing, assume 1-hour duration from start time.
        4. If only a date is present, leave time fields empty and set `"all_day_event": true`.
        5. If any field is missing, leave it as "".
        6. Return only the **JSON array**. Do not include explanations or comments.

        Text:
        $notificationText
        """.trimIndent()
    }
}
