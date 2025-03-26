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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fyp_androidapp.ui.components.CalendarViewer
import com.example.fyp_androidapp.ui.components.EventListViewer
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.viewmodel.CalendarViewModel
import kotlinx.datetime.*

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var year by remember { mutableStateOf(currentDate.year) }
    var month by remember { mutableStateOf(currentDate.month) }

    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }
    val years = (2000..2030).toList()

    val events by viewModel.events.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPopupVisible by viewModel.isPopupVisible.collectAsState()
    val selectedEventDetails by viewModel.selectedEventDetails.collectAsState()

    // Fetch events when the month changes
    LaunchedEffect(year, month) {
        viewModel.fetchEventsForMonth(year, month.ordinal + 1)
    }

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
                onDateSelected = { viewModel.selectDate(it) },
                events = events  // ✅ Pass the updated List<EventDetails>
            )

            Spacer(modifier = Modifier.height(16.dp))

            EventListViewer(
                selectedDate = selectedDate,
                events = events,  // ✅ Now using EventDetails
                onEventSelected = { eventDetails ->
                    viewModel.showEventPopup(eventDetails)  // ✅ Pass EventDetails directly
                }
            )
        }

        FloatingActionButton(
            onClick = {
                // ✅ Ensure a new event has a unique ID
                val newEvent = EventDetails(
                    id = System.currentTimeMillis().toString(), // 🔥 Generate unique ID
                    title = "New Event",
                    startDate = selectedDate?.toString() ?: currentDate.toString(),
                    startTime = "00:00",
                    endDate = selectedDate?.toString() ?: currentDate.toString(),
                    endTime = "23:59",
                    location = "",
                    description = "",
                    allDay = false,
                    buttonStatus = 1
                )
                viewModel.showEventPopup(newEvent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event"
            )
        }
    }

    if (isPopupVisible) {
        EventPopupDialog(
            eventDetails = selectedEventDetails,
            onSave = { newEventDetails ->
                val eventId = if (newEventDetails.id.isNotEmpty()) newEventDetails.id
                else System.currentTimeMillis().toString()
                val newEvent = newEventDetails.copy(id = eventId)

                if (selectedEventDetails.id.isEmpty()) {
                    viewModel.addNewEvent(newEvent)
                } else {
                    val eventToAdd = newEventDetails.copy(id = eventId, buttonStatus = 1)
                    viewModel.addNewEvent(eventToAdd)
                }
                viewModel.hideEventPopup()
            }
            ,
            onDismiss = { viewModel.hideEventPopup() },
            onDiscard = {
                selectedEventDetails.id?.let { eventId ->
                    viewModel.discardEvent(eventId, selectedEventDetails.startDate)
                }
                viewModel.hideEventPopup()
            }
        )
    }
}
