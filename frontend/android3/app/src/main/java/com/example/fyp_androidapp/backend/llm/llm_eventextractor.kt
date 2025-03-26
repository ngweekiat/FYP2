package com.example.fyp_androidapp.backend.llm

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object LlmEventExtractor {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = Constants.GEMINI_API_KEY, // 🔐 Replace with secure storage
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
                Log.d("GeminiPrompt", "🔍 Gemini Input:\n$prompt")
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
        You are an intelligent assistant that extracts calendar event information from a sequence of chat or notification messages.

        You must extract only the **last event mentioned** in the conversation history and return it in **strictly valid JSON format** like this:
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

        Chat History:
        $notificationText

        Current timestamp: "$receivedAtTimestamp"

        Rules:
        1. Use the current timestamp to interpret relative times like "tomorrow" or "next Friday".
        2. If the end time/date is not provided, default it to the same as the start.
        2A. If only the start time is found but no end time, assume the event lasts 1 hour and compute the end time accordingly.
        3. If time is missing but a date is present, leave the time as "" and set "all_day_event": true.
        4. If any field is missing or unclear, leave it as "".
        5. DO NOT return anything except the JSON. No explanation, comments, or markdown.

        Output:
    """.trimIndent()
    }
}