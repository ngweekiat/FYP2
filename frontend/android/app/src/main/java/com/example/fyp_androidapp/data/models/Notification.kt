package com.example.fyp_androidapp.data.models

data class Notification(
    val sender: String,
    val title: String,
    val content: String,
    val time: String,
    val isImportant: Boolean = false,
    var status: String? = null // Tracks additional status (e.g., "Event Discarded" or date)
)
