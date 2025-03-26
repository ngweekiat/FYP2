package com.example.fyp_androidapp.data.models

data class EventDetails(
    val id: String = "",  // ✅ Ensure each event has a unique ID
    val title: String = "",
    val description: String = "",
    val allDay: Boolean = false, // All day button
    val startDate: String = "", // Start date
    val startTime: String = "", // Start time
    val endDate: String = "", // End date
    val endTime: String = "", // End time
    val location: String = "", // Add location/meeting time
    val buttonStatus: Int = 0
)