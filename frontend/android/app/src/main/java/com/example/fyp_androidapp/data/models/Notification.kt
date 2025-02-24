package com.example.fyp_androidapp.data.models

data class Notification(
    val id: String,
    val sender: String,
    val title: String,
    val content: String,
    val time: String,
    val isImportant: Boolean,
    var status: String? = null,  // Already tracking status
    var isActionPerformed: Boolean = false  // New property to track state
)

