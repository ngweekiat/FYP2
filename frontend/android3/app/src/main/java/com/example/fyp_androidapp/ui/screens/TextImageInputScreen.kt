package com.example.fyp_androidapp.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.ui.components.NotificationCard
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.backend.llm.LlmEventExtractor
import com.example.fyp_androidapp.backend.llm.LlmMultiEventExtractor
import com.example.fyp_androidapp.viewmodel.NotificationsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@Composable
fun TextImageInputScreen(notificationsViewModel: NotificationsViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pastedText by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    var extractedEvents by remember { mutableStateOf<List<Pair<String, EventDetails>>>(emptyList()) }
    var isExtracting by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogEventDetails by remember { mutableStateOf<EventDetails?>(null) }
    var dialogEventId by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                bitmap = result.drawable.toBitmap()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Text to Event", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Paste Text (collapsible)
        ShrinkCardButton(
            title = "Paste Text",
            subtitle = "Input or Paste your text",
            icon = Icons.Default.TextFields,
            content = {
                OutlinedTextField(
                    value = pastedText,
                    onValueChange = { pastedText = it },
                    label = { Text("Your text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isExtracting = true
                        extractedEvents = emptyList()

                        coroutineScope.launch {
                            val jsonList = LlmMultiEventExtractor.extractMultipleEvents(
                                notificationText = pastedText,
                                receivedAtTimestamp = LocalDateTime.now().toString()
                            )

                            val mappedEvents = jsonList.mapIndexed { index, json ->
                                val id = System.currentTimeMillis().toString() + (index + 1).toString()
                                id to EventDetails(
                                    id = id,
                                    title = json.optString("title", ""),
                                    description = json.optString("description", ""),
                                    location = json.optString("location", ""),
                                    startDate = json.optString("start_date", ""),
                                    startTime = json.optString("start_time", ""),
                                    endDate = json.optString("end_date", ""),
                                    endTime = json.optString("end_time", ""),
                                    allDay = json.optBoolean("all_day_event", false),
                                    buttonStatus = 0
                                )
                            }

                            extractedEvents = mappedEvents
                            isExtracting = false
                        }
                    },
                    enabled = pastedText.isNotBlank()
                ) {
                    Text(if (isExtracting) "Extracting..." else "Extract Events")
                }
            }
        )


        Spacer(modifier = Modifier.height(12.dp))


        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Uploaded image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Events", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

        extractedEvents.forEachIndexed { index, (eventId, eventDetails) ->
            NotificationCard(
                notification = Notification(
                    id = eventId,
                    sender = "Pasted Text",
                    title = eventDetails.title,
                    content = pastedText,
                    time = "${eventDetails.startDate} ${eventDetails.startTime}".trim(),
                    isImportant = true
                ),
                eventDetails = eventDetails,
                onAdd = {
                    dialogEventDetails = eventDetails
                    dialogEventId = eventId
                    showDialog = true
                },
                onDiscard = {
                    val updatedEvent = eventDetails.copy(buttonStatus = 2)
                    extractedEvents = extractedEvents.map {
                        if (it.first == eventId) it.first to updatedEvent else it
                    }
                    notificationsViewModel.discardEvent(eventId, updatedEvent)
                },
                onLongPress = {
                    dialogEventDetails = eventDetails
                    dialogEventId = eventId
                    showDialog = true
                }
            )

            if (index < extractedEvents.lastIndex) {
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }


        if (showDialog && dialogEventDetails != null) {
            EventPopupDialog(
                eventDetails = dialogEventDetails!!,
                onSave = { updated ->
                    val updatedEvent = updated.copy(buttonStatus = 1)
                    extractedEvents = extractedEvents.map {
                        if (it.first == dialogEventId) it.first to updatedEvent else it
                    }
                    notificationsViewModel.addEvent(dialogEventId, updatedEvent)
                    showDialog = false
                },
                onDiscard = {
                    val updatedEvent = dialogEventDetails!!.copy(buttonStatus = 2)
                    extractedEvents = extractedEvents.map {
                        if (it.first == dialogEventId) it.first to updatedEvent else it
                    }
                    notificationsViewModel.discardEvent(dialogEventId, updatedEvent)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}


@Composable
fun ShrinkCardButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable (() -> Unit)? = null
) {
    val baseHeight = if (content == null) 72.dp else Dp.Unspecified

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = baseHeight)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }

            content?.let {
                Spacer(modifier = Modifier.height(12.dp))
                it()
            }
        }
    }
}

