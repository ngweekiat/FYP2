package com.example.fyp_androidapp.backend.llm

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object LlmEventExtractor {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyDcpjLErwLkqoqCnvcKl6557PKKG86nkPM", // 🔐 Replace with secure storage
        generationConfig = generationConfig {
            temperature = 0.2f
            topK = 20
            topP = 0.9f
            maxOutputTokens = 512
            responseMimeType = "text/plain"
        }
    )

    suspend fun extractEventDetails(notificationText: String, receivedAtTimestamp: String): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(notificationText, receivedAtTimestamp)
                val chat = model.startChat()
                val response = chat.sendMessage(prompt)

                val content = response.text?.trim()
                Log.d("LlmEventExtractor", "Gemini response:\n$content")

                if (content.isNullOrBlank()) {
                    Log.e("LlmEventExtractor", "Empty response from Gemini")
                    return@withContext null
                }

                val jsonStart = content.indexOf('{')
                val jsonEnd = content.lastIndexOf('}')

                if (jsonStart == -1 || jsonEnd == -1) {
                    Log.e("LlmEventExtractor", "No valid JSON found")
                    return@withContext null
                }

                val jsonString = content.substring(jsonStart, jsonEnd + 1)
                return@withContext JSONObject(jsonString)

            } catch (e: Exception) {
                Log.e("LlmEventExtractor", "Failed to extract event details: ${e.message}", e)
                null
            }
        }
    }

    private fun buildPrompt(notificationText: String, receivedAtTimestamp: String): String {
        return """
            You are an intelligent assistant that extracts calendar events from notifications.
            You must respond strictly in valid JSON matching this format:
            {
              "title": "Event Title",
              "description": "Optional event description",
              "all_day_event": false,
              "start_date": "YYYY-MM-DD",
              "start_time": "HH:MM",
              "end_date": "YYYY-MM-DD",
              "end_time": "HH:MM"
            }

            Notification Details:
            - Notification text: "$notificationText"
            - Received timestamp: "$receivedAtTimestamp"

            Rules:
            1. Interpret relative dates using the timestamp above.
            2. Don't guess. If something is unclear or missing, leave it as "".
            3. Set "all_day_event": true if clearly stated.
            4. Do not return anything else. JSON only.
        """.trimIndent()
    }
}