package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fyp_androidapp.data.models.EventDetails

@Composable
fun EventPopupDialog(
    eventDetails: EventDetails = EventDetails(),
    onSave: (EventDetails) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue(eventDetails.title)) }
    var startDate by remember { mutableStateOf(eventDetails.startDate) }
    var startTime by remember { mutableStateOf(eventDetails.startTime) }
    var endDate by remember { mutableStateOf(eventDetails.endDate) }
    var endTime by remember { mutableStateOf(eventDetails.endTime) }

    // Full-screen Dialog
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth() // Occupy the full width
                .fillMaxHeight() // Occupy the full height
                .padding(horizontal = 0.dp, vertical = 0.dp), // Remove horizontal padding to occupy full width
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
                        .padding(16.dp),
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
                                title = title.text,
                                startDate = startDate,
                                startTime = startTime,
                                endDate = endDate,
                                endTime = endTime
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
                        .weight(1f), // Fill the remaining vertical space
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // Start and End Date-Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = startDate,
                                    onValueChange = { startDate = it },
                                    label = { Text("Starts") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = startTime,
                                    onValueChange = { startTime = it },
                                    label = { Text("Time") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = endDate,
                                    onValueChange = { endDate = it },
                                    label = { Text("Ends") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = endTime,
                                    onValueChange = { endTime = it },
                                    label = { Text("Time") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
