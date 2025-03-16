package com.example.fyp_androidapp.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mma")

    /**
     * Formats the date and time into a human-readable format.
     * @param date The date in `yyyy-MM-dd` format.
     * @param time The time in `HH:mm` format.
     * @return A formatted string in `MMM dd, yyyy hh:mma` format.
     */
    fun formatDate(date: String, time: String): String {
        return try {
            val localDate = LocalDate.parse(date)
            val formattedDate = localDate.format(dateFormatter)

            val formattedTime = if (time != "Unknown Time") {
                val localTime = LocalTime.parse(time)
                localTime.format(timeFormatter).uppercase()
            } else {
                "Unknown Time"
            }

            "$formattedDate $formattedTime"
        } catch (e: Exception) {
            "Invalid Date/Time"
        }
    }

    /**
     * Converts an ISO 8601 timestamp into a Singapore timezone formatted string.
     * @param timestamp ISO 8601 formatted timestamp.
     * @return A formatted time string in `MMM dd, yyyy hh:mma` format.
     */
    fun formatTimestampToSGT(timestamp: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(timestamp)
                .withZoneSameInstant(ZoneId.of("Asia/Singapore"))
            zonedDateTime.format(dateFormatter) + " " + zonedDateTime.format(timeFormatter).uppercase()
        } catch (e: Exception) {
            "Unknown Time"
        }
    }
}
