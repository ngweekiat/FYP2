package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fyp_androidapp.data.models.EventDetails
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.time.LocalDate


@Composable
fun EventPopupDialog(
    eventDetails: EventDetails = EventDetails(),
    onSave: (EventDetails) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var id by remember { mutableStateOf(TextFieldValue(eventDetails.id)) }
    var title by remember { mutableStateOf(TextFieldValue(eventDetails.title)) }
    var description by remember { mutableStateOf(TextFieldValue(eventDetails.description)) }
    var allDay by remember { mutableStateOf(eventDetails.allDay) }
    var startDate by remember { mutableStateOf(TextFieldValue(eventDetails.startDate)) }
    var startTime by remember { mutableStateOf(TextFieldValue(eventDetails.startTime)) }
    var endDate by remember { mutableStateOf(TextFieldValue(eventDetails.endDate)) }
    var endTime by remember { mutableStateOf(TextFieldValue(eventDetails.endTime)) }
    var locationOrMeeting by remember { mutableStateOf(TextFieldValue(eventDetails.locationOrMeeting)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with Cancel and Add Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                    Text(
                        text = "Add Event",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = {
                        onSave(
                            EventDetails(
                                id = eventDetails.id,
                                title = title.text,
                                description = description.text,
                                allDay = allDay,
                                startDate = startDate.text,
                                startTime = startTime.text,
                                endDate = endDate.text,
                                endTime = endTime.text,
                                locationOrMeeting = locationOrMeeting.text
                            )
                        )
                    }) {
                        Text("Add", color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    item {
                        TitleSection(title = title, onTitleChange = { title = it })
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }

                    item {
                        DescriptionSection(description = description, onDescriptionChange = { description = it })
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }

                    item {
                        DateTimeSection(
                            allDay = allDay,
                            onAllDayChange = { allDay = it },
                            startDate = startDate.text,
                            startTime = startTime.text,
                            onStartDateClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                                        startDate = TextFieldValue(localDate.format(DateTimeFormatter.ISO_DATE)) // Stores as yyyy-MM-dd
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            onStartTimeClick = {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        startTime = TextFieldValue("${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            endDate = endDate.text,
                            endTime = endTime.text,
                            onEndDateClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                                        endDate = TextFieldValue(localDate.format(DateTimeFormatter.ISO_DATE)) // Stores as yyyy-MM-dd
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            onEndTimeClick = {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        endTime = TextFieldValue("${hourOfDay.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Title Section
@Composable
fun TitleSection(title: TextFieldValue, onTitleChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold // Make text bold
        ),
        modifier = Modifier
            .fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (title.text.isEmpty()) {
                    Text(
                        text = "Add title",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold // Bold placeholder text
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

// Description Section
@Composable
fun DescriptionSection(description: TextFieldValue, onDescriptionChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = description,
        onValueChange = onDescriptionChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (description.text.isEmpty()) {
                    Text(
                        text = "Add description",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
                innerTextField()
            }
        }
    )
}

// Date-Time Section
@Composable
fun DateTimeSection(
    allDay: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    startDate: String,
    startTime: String,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    endDate: String,
    endTime: String,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Day",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Switch(
                checked = allDay,
                onCheckedChange = onAllDayChange,
                modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp), // Equal vertical padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (startDate.isNotEmpty()) startDate else "Select Date",
                modifier = Modifier
                    .clickable(onClick = onStartDateClick)
                    .weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (startTime.isNotEmpty()) startTime else "Select Time",
                modifier = Modifier.clickable(onClick = onStartTimeClick),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp), // Equal vertical padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (endDate.isNotEmpty()) endDate else "Select Date",
                modifier = Modifier
                    .clickable(onClick = onEndDateClick)
                    .weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (endTime.isNotEmpty()) endTime else "Select Time",
                modifier = Modifier.clickable(onClick = onEndTimeClick),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}
