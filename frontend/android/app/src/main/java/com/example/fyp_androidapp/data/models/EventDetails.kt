package com.example.fyp_androidapp.data.models

data class EventDetails(
    val title: String = "",
    val location: String = "",
    val allDay: Boolean = false,
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val travelTime: String = "None",
    val repeat: String = "Never",
    val alert: String = "None",
    val guests: List<String> = emptyList()
)
