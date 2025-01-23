package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text // Import added here
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@Composable
fun CalendarViewer(
    year: Int,
    month: Month,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    events: Map<LocalDate, List<String>>
) {
    val firstDayOfMonth = LocalDate(year, month, 1)
    val daysInMonth = getDaysInMonth(year, month)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayName ->
                Text(
                    text = dayName,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(startDayOfWeek) {
                Box(modifier = Modifier.size(50.dp))
            }

            itemsIndexed(List(daysInMonth) { it + 1 }) { _, day ->
                val currentDate = firstDayOfMonth.plus(day - 1, DateTimeUnit.DAY)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .padding(4.dp)
                        .background(
                            if (selectedDate == currentDate) Color.Gray else Color.Transparent
                        )
                        .clickable { onDateSelected(currentDate) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = day.toString())
                        if (events.containsKey(currentDate)) {
                            Text(
                                text = "\u2022",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getDaysInMonth(year: Int, month: Month): Int {
    val firstDayOfMonth = LocalDate(year, month, 1)
    val firstDayOfNextMonth = if (month == Month.DECEMBER) {
        LocalDate(year + 1, Month.JANUARY, 1)
    } else {
        LocalDate(year, month.next(), 1)
    }
    return firstDayOfMonth.until(firstDayOfNextMonth, DateTimeUnit.DAY)
}

fun Month.next(): Month {
    val values = Month.values()
    return values[(this.ordinal + 1) % values.size]
}
