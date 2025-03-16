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
            // Fix: Trim whitespace to prevent parsing errors
            val trimmedDate = date.trim()
            val trimmedTime = time.trim()
            // Ensure correct formats
            val localDate = LocalDate.parse(trimmedDate, DateTimeFormatter.ISO_DATE)
            val formattedDate = localDate.format(dateFormatter)

            val formattedTime = if (trimmedTime.isNotEmpty() && trimmedTime != "Unknown Time") {
                val localTime = LocalTime.parse(trimmedTime, DateTimeFormatter.ofPattern("H:mm"))
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
     * Formats a date string from `yyyy-MM-dd` to `MMM dd, yyyy`.
     * @param date The date in `yyyy-MM-dd` format.
     * @return Formatted date in `MMM dd, yyyy` format or "Invalid Date" if parsing fails.
     */
    fun formatDateOnly(date: String): String {
        return try {
            val localDate = LocalDate.parse(date.trim())
            localDate.format(dateFormatter)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    /**
     * Formats a time string from `HH:mm` or `H:mm` to `hh:mma`.
     * @param time The time in `HH:mm` format.
     * @return Formatted time in `hh:mma` format or "Invalid Time" if parsing fails.
     */
    fun formatTimeOnly(time: String): String {
        return try {
            val localTime = LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("H:mm")) // Allows single-digit hours
            localTime.format(timeFormatter).uppercase() // Converts AM/PM to uppercase
        } catch (e: Exception) {
            "Invalid Time"
        }
    }

}
