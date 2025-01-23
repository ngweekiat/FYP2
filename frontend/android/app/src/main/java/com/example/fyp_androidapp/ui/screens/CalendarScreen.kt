package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.ui.components.CalendarViewer
import com.example.fyp_androidapp.ui.components.EventListViewer
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.datetime.*

@Composable
fun CalendarScreen() {
    var year by remember { mutableStateOf(2025) }
    var month by remember { mutableStateOf(Month.JANUARY) }

    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    val years = (2000..2030).toList()
    val events = remember {
        mapOf(
            LocalDate(2025, Month.JANUARY, 3) to listOf("10:00 AM - Meeting with Team", "1:00 PM - Lunch with Sarah"),
            LocalDate(2025, Month.JANUARY, 5) to listOf("3:00 PM - Doctor's Appointment"),
        )
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isPopupVisible by remember { mutableStateOf(false) }
    var selectedEventDetails by remember { mutableStateOf(EventDetails()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Text(
                        text = month.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.clickable { isMonthDropdownExpanded = true },
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

                Box {
                    Text(
                        text = year.toString(),
                        modifier = Modifier.clickable { isYearDropdownExpanded = true },
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

            CalendarViewer(
                year = year,
                month = month,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                events = events
            )

            Spacer(modifier = Modifier.height(16.dp))

            EventListViewer(
                selectedDate = selectedDate,
                events = events,
                onEventSelected = {
                    selectedEventDetails = it
                    isPopupVisible = true
                }
            )
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                selectedEventDetails = EventDetails() // Reset event details for new event
                isPopupVisible = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Requires androidx.compose.material.icons.*
                contentDescription = "Add Event"
            )
        }
    }

    if (isPopupVisible) {
        EventPopupDialog(
            eventDetails = selectedEventDetails,
            onSave = {
                // Handle saving event details here
                isPopupVisible = false
            },
            onDismiss = { isPopupVisible = false }
        )
    }
}
