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
import com.example.fyp_androidapp.Constants
import kotlinx.coroutines.*
import kotlinx.datetime.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun CalendarScreen() {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var year by remember { mutableStateOf(currentDate.year) }
    var month by remember { mutableStateOf(currentDate.month) }

    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }
    val years = (2000..2030).toList()

    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isPopupVisible by remember { mutableStateOf(false) }
    var selectedEventDetails by remember { mutableStateOf(EventDetails()) }

    fun fetchEventsForMonth(selectedYear: Int, selectedMonth: Month) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val url = "${Constants.BASE_URL}/notifications/calendar_events?year=$selectedYear&month=${selectedMonth.ordinal + 1}"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@launch
                    val jsonArray = JSONArray(responseBody)

                    val fetchedEvents = mutableMapOf<LocalDate, List<String>>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonEvent = jsonArray.getJSONObject(i)
                        val eventDate = LocalDate.parse(jsonEvent.getString("start_date"))
                        val eventTime = jsonEvent.getString("start_time")
                        val eventTitle = jsonEvent.getString("title")

                        fetchedEvents[eventDate] = fetchedEvents.getOrDefault(eventDate, emptyList()) + "$eventTime - $eventTitle"
                    }

                    withContext(Dispatchers.Main) {
                        events = fetchedEvents
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fetch events when the month changes
    LaunchedEffect(year, month) {
        fetchEventsForMonth(year, month)
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

        FloatingActionButton(
            onClick = {
                selectedEventDetails = EventDetails()
                isPopupVisible = true
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
            onSave = {
                isPopupVisible = false
            },
            onDismiss = { isPopupVisible = false }
        )
    }
}
