package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                    Text(text = "Events on ${selectedDate.toString()}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedEvents.forEach { event ->
                        Text(
                            text = "- $event",
                            modifier = Modifier.clickable {
                                onEventSelected(EventDetails(title = "Event", description = event))
                            }
                        )
                    }
                }
            } else {
                Text(text = "No events on ${selectedDate.toString()}.")
            }
        } else {
            Text(
                text = "Select a date to view events.",)
        }
    }
}
