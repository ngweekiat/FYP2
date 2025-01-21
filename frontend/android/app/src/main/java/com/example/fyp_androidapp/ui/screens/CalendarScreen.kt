package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@Composable
fun CalendarScreen() {
    // State to track the current year and month
    var year by remember { mutableStateOf(2025) }
    var month by remember { mutableStateOf(Month.JANUARY) }

    // State for dropdown menus
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    // Generate a list of years (e.g., 2000–2030)
    val years = (2000..2030).toList()

    // Calculate days in the selected month
    val firstDayOfMonth = LocalDate(year, month, 1)
    val daysInMonth = getDaysInMonth(year, month)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 = Monday, 6 = Sunday

    // Mock events for specific days
    val events = remember {
        mapOf(
            LocalDate(2025, Month.JANUARY, 3) to listOf("Meeting at 10 AM", "Lunch with Sarah"),
            LocalDate(2025, Month.JANUARY, 5) to listOf("Doctor's Appointment"),
            LocalDate(2025, Month.JANUARY, 15) to listOf("Project Deadline", "Team Standup"),
        )
    }

    // State to track selected date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdowns for Month and Year Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Dropdown
            Box {
                Text(
                    text = month.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier
                        .clickable { isMonthDropdownExpanded = true }
                        .padding(8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                DropdownMenu(
                    expanded = isMonthDropdownExpanded,
                    onDismissRequest = { isMonthDropdownExpanded = false }
                ) {
                    Month.values().forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                month = m
                                isMonthDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Year Dropdown
            Box {
                Text(
                    text = year.toString(),
                    modifier = Modifier
                        .clickable { isYearDropdownExpanded = true }
                        .padding(8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                DropdownMenu(
                    expanded = isYearDropdownExpanded,
                    onDismissRequest = { isYearDropdownExpanded = false }
                ) {
                    years.forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y.toString()) },
                            onClick = {
                                year = y
                                isYearDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days of the week header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f) // Makes the calendar occupy most of the screen
        ) {
            // Empty slots for days before the first of the month
            items(startDayOfWeek) {
                Box(modifier = Modifier.size(50.dp))
            }

            // Days of the month
            itemsIndexed(List(daysInMonth) { it + 1 }) { _, day ->
                val currentDate = firstDayOfMonth.plus(day - 1, DateTimeUnit.DAY)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .padding(4.dp)
                        .background(
                            if (selectedDate == currentDate) Color.Gray else Color.Transparent
                        )
                        .clickable { selectedDate = currentDate },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = day.toString())
                        if (events.containsKey(currentDate)) {
                            Text(
                                text = "•", // Indicates events
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Events for the selected date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (selectedDate != null) {
                val selectedEvents = events[selectedDate]
                if (selectedEvents != null && selectedEvents.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Events on ${selectedDate.toString()}:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedEvents.forEach { event ->
                            Text(text = "- $event", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text(
                        text = "No events on ${selectedDate.toString()}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "Select a date to view events.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Helper function to calculate the number of days in a month
fun getDaysInMonth(year: Int, month: Month): Int {
    val firstDayOfMonth = LocalDate(year, month, 1)
    val firstDayOfNextMonth = if (month == Month.DECEMBER) {
        LocalDate(year + 1, Month.JANUARY, 1)
    } else {
        LocalDate(year, month.next(), 1)
    }
    return firstDayOfMonth.until(firstDayOfNextMonth, DateTimeUnit.DAY)
}

// Extension function to get the next month
fun Month.next(): Month {
    val values = Month.values()
    return values[(this.ordinal + 1) % values.size]
}

// Extension function to get the previous month
fun Month.previous(): Month {
    val values = Month.values()
    return values[(this.ordinal - 1 + values.size) % values.size]
}
