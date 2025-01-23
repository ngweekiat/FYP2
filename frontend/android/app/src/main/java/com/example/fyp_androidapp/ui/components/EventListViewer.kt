package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.datetime.LocalDate

@Composable
fun EventListViewer(
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<String>>,
    onEventSelected: (EventDetails) -> Unit
) {
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedEvents.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEventSelected(EventDetails(title = "Event", description = event))
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Assuming events are stored as "Time - Event Description"
                            val parts = event.split(" - ", limit = 2)
                            val time = parts.getOrNull(0) ?: ""
                            val description = parts.getOrNull(1) ?: event

                            // Event Description
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Time
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No events on ${selectedDate.toString()}.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Text(
                text = "Select a date to view events.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
