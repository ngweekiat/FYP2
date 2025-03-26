package com.example.fyp_androidapp.backend.llm

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LlmEventImportance {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash", // or gemini-pro if needed
        apiKey = Constants.GEMINI_API_KEY, // 🔐 Store safely via secrets-gradle-plugin
        generationConfig = generationConfig {
            temperature = 0.5f
            topK = 20
            topP = 0.9f
            maxOutputTokens = 50
            responseMimeType = "text/plain"
        }
    )

    suspend fun detectEventImportance(notificationText: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(notificationText)
                val chat = model.startChat()
                val response = chat.sendMessage(prompt)

                val output = response.text?.trim()

                Log.d("LlmEventImportance", "Gemini raw response: $output")

                if (output == "0" || output == "1") {
                    return@withContext output.toInt()
                } else {
                    Log.e("LlmEventImportance", "Invalid Gemini output: $output")
                    return@withContext 0
                }

            } catch (e: Exception) {
                Log.e("LlmEventImportance", "Gemini API error: ${e.message}", e)
                return@withContext 0
            }
        }
    }

    private fun buildPrompt(notificationText: String): String {
        return """
            You are an AI that determines if a notification contains an event that can be added to a calendar.
            Respond only with '1' if an event is detected and '0' if not. 
            No extra text, no explanations, no formatting.

            An event must have a clear reference to a date, time, or schedule to be considered valid.

            Analyze the following notification:
            "$notificationText"

            Respond only with '1' (yes) or '0' (no).
        """.trimIndent()
    }
}