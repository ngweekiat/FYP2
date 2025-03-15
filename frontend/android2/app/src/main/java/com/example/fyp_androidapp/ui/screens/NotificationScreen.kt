package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.ui.components.NotificationCard
import com.example.fyp_androidapp.viewmodel.NotificationsViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel = viewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // Observe isLoading from ViewModel
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
    var eventDetails by remember { mutableStateOf<EventDetails?>(null) }
    var eventDetailsMap by remember { mutableStateOf(mapOf<String, EventDetails>()) }

    val lazyListState = rememberLazyListState()

    // Trigger fetching of notifications when scrolling reaches the bottom
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .debounce(300L)
            .collect { visibleItems ->
                val lastIndex = lazyListState.layoutInfo.totalItemsCount - 1
                val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1

                if (lastVisibleIndex >= lastIndex && !isLoading) {
                    viewModel.fetchNotifications() // Fetch more notifications when bottom is reached
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                val eventDetails = eventDetailsMap[notification.id]

                Column {
                    NotificationCard(
                        notification = notification,
                        onAdd = {
                            selectedNotification = notification
                        },
                        onDiscard = {
                            eventDetailsMap = eventDetailsMap - notification.id
                        },
                        statusMessage = ""
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }

            // Show loading indicator when fetching more notifications
            item {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
