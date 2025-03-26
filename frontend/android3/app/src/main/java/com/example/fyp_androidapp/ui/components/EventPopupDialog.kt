package com.example.fyp_androidapp.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fyp_androidapp.data.models.EventDetails
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun EventPopupDialog(
    eventDetails: EventDetails = EventDetails(),
    onSave: (EventDetails) -> Unit,
    onDiscard: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var id by remember { mutableStateOf(TextFieldValue(eventDetails.id)) }
    var title by remember { mutableStateOf(TextFieldValue(eventDetails.title)) }
    var description by remember { mutableStateOf(TextFieldValue(eventDetails.description)) }
    var startDate by remember { mutableStateOf(TextFieldValue(eventDetails.startDate)) }
    var startTime by remember { mutableStateOf(TextFieldValue(eventDetails.startTime)) }
    var endDate by remember { mutableStateOf(TextFieldValue(eventDetails.endDate)) }
    var endTime by remember { mutableStateOf(TextFieldValue(eventDetails.endTime)) }
    var locationOrMeeting by remember { mutableStateOf(TextFieldValue(eventDetails.location)) }

    // Error states
    var titleError by remember { mutableStateOf(false) }
    var startDateError by remember { mutableStateOf(false) }
    var startTimeError by remember { mutableStateOf(false) }
    var endDateError by remember { mutableStateOf(false) }
    var endTimeError by remember { mutableStateOf(false) }
    var dateLogicError by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                    Text(text = "Add Event", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        // Validation
                        titleError = title.text.isBlank()
                        startDateError = startDate.text.isBlank()
                        startTimeError = startTime.text.isBlank()
                        endDateError = endDate.text.isBlank()
                        endTimeError = endTime.text.isBlank()
                        dateLogicError = false

                        val baseValid = !titleError && !startDateError && !startTimeError && !endDateError && !endTimeError

                        if (baseValid) {
                            try {
                                val startDT = LocalDateTime.parse("${startDate.text} ${startTime.text}", dateFormatter)
                                val endDT = LocalDateTime.parse("${endDate.text} ${endTime.text}", dateFormatter)
                                if (endDT.isAfter(startDT)) {
                                    onSave(
                                        EventDetails(
                                            id = eventDetails.id,
                                            title = title.text,
                                            description = description.text,
                                            allDay = false, // Always false
                                            startDate = startDate.text,
                                            startTime = startTime.text,
                                            endDate = endDate.text,
                                            endTime = endTime.text,
                                            location = locationOrMeeting.text
                                        )
                                    )
                                } else {
                                    dateLogicError = true
                                    endDateError = true
                                    endTimeError = true
                                }
                            } catch (e: Exception) {
                                dateLogicError = true
                                endDateError = true
                                endTimeError = true
                            }
                        }
                    }) {
                        Text("Add", color = MaterialTheme.colorScheme.primary)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    item {
                        TitleSection(title = title, onTitleChange = { title = it }, isError = titleError)
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    item {
                        DescriptionSection(description = description, onDescriptionChange = { description = it })
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    item {
                        LocationSection(location = locationOrMeeting, onLocationChange = { locationOrMeeting = it })
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    item {
                        DateTimeSection(
                            startDate = startDate.text,
                            startTime = startTime.text,
                            onStartDateClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val selectedDate = LocalDate.of(y, m + 1, d)
                                        startDate =
                                            TextFieldValue(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
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
                                    { _, h, m ->
                                        startTime = TextFieldValue(
                                            "${
                                                h.toString().padStart(2, '0')
                                            }:${m.toString().padStart(2, '0')}"
                                        )
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
                                    { _, y, m, d ->
                                        val selectedDate = LocalDate.of(y, m + 1, d)
                                        endDate =
                                            TextFieldValue(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
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
                                    { _, h, m ->
                                        endTime = TextFieldValue(
                                            "${
                                                h.toString().padStart(2, '0')
                                            }:${m.toString().padStart(2, '0')}"
                                        )
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            startDateError = startDateError,
                            startTimeError = startTimeError,
                            endDateError = endDateError,
                            endTimeError = endTimeError,
                        )
                    }
                    if (dateLogicError) {
                        item {
                            Text(
                                text = "End date/time must be after start date/time.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onDiscard(eventDetails.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}


// Rest of your components remain unchanged but are needed to compile:

@Composable
fun TitleSection(title: TextFieldValue, onTitleChange: (TextFieldValue) -> Unit, isError: Boolean) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isError) Color(0xFFFFCDD2) else Color.Transparent),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (title.text.isEmpty()) {
                    Text(
                        text = "Add title",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun DescriptionSection(description: TextFieldValue, onDescriptionChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = description,
        onValueChange = onDescriptionChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
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

@Composable
fun LocationSection(location: TextFieldValue, onLocationChange: (TextFieldValue) -> Unit) {
    BasicTextField(
        value = location,
        onValueChange = onLocationChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (location.text.isEmpty()) {
                    Text(
                        text = "Add location or meeting link",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
                innerTextField()
            }
        }
    )
}


@Composable
fun DateTimeSection(
    startDate: String,
    startTime: String,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    endDate: String,
    endTime: String,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    startDateError: Boolean,
    startTimeError: Boolean,
    endDateError: Boolean,
    endTimeError: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Date & Time",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (startDate.isNotEmpty()) startDate else "Select Date",
                modifier = Modifier
                    .clickable(onClick = onStartDateClick)
                    .weight(1f)
                    .background(if (startDateError) Color(0xFFFFCDD2) else Color.Transparent),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (startTime.isNotEmpty()) startTime else "Select Time",
                modifier = Modifier
                    .clickable(onClick = onStartTimeClick)
                    .background(if (startTimeError) Color(0xFFFFCDD2) else Color.Transparent),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (endDate.isNotEmpty()) endDate else "Select Date",
                modifier = Modifier
                    .clickable(onClick = onEndDateClick)
                    .weight(1f)
                    .background(if (endDateError) Color(0xFFFFCDD2) else Color.Transparent),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (endTime.isNotEmpty()) endTime else "Select Time",
                modifier = Modifier
                    .clickable(onClick = onEndTimeClick)
                    .background(if (endTimeError) Color(0xFFFFCDD2) else Color.Transparent),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

