package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun EventListViewer(
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<EventDetails>>,  // ✅ Updated to List<EventDetails>
    onEventSelected: (EventDetails) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Ensure the box takes full screen height
            .padding(16.dp)
    ) {
        if (selectedDate != null) {
            val selectedEvents = events[selectedDate]
            if (!selectedEvents.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Events on ${selectedDate.toString()}:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn( // Use LazyColumn for scrolling
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(selectedEvents) { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEventSelected(event) }  // ✅ Pass the actual event
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.title,  // ✅ Use event details directly
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = event.startTime,  // ✅ Use event time
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
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
