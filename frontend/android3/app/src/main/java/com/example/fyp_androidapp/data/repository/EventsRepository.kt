package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.database.DatabaseProvider
import com.example.fyp_androidapp.database.entities.EventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository {

    private val db = DatabaseProvider.getDatabase()
    private val eventDao = db.eventDao()

    suspend fun fetchCalendarEvent(notificationId: String): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val entity = eventDao.getEventById(notificationId) ?: return@withContext null
                EventDetails(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    startDate = entity.startDate,
                    startTime = entity.startTime,
                    endDate = entity.endDate,
                    endTime = entity.endTime,
                    allDay = entity.allDay,
                    location = entity.location,
                    buttonStatus = entity.buttonStatus
                )
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error fetching event: ${e.message}")
                null
            }
        }
    }

    suspend fun addEventToCalendar(notificationId: String, eventDetails: EventDetails): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val entity = EventEntity(
                    id = notificationId,
                    title = eventDetails.title,
                    description = eventDetails.description,
                    startDate = eventDetails.startDate,
                    startTime = eventDetails.startTime,
                    endDate = eventDetails.endDate,
                    endTime = eventDetails.endTime,
                    allDay = eventDetails.allDay,
                    location = eventDetails.location,
                    buttonStatus = eventDetails.buttonStatus
                )
                eventDao.insertEvent(entity)
                Log.d("EventsRepository", "Event added to Room DB: $entity")
                eventDetails.copy(buttonStatus = 1)
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error saving event: ${e.message}")
                null
            }
        }
    }

    suspend fun discardEvent(eventId: String): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val entity = eventDao.getEventById(eventId) ?: return@withContext null
                val discardedEntity = entity.copy(buttonStatus = 2)
                eventDao.insertEvent(discardedEntity)
                Log.d("EventsRepository", "Event discarded (same data): $discardedEntity")
                EventDetails(
                    id = discardedEntity.id,
                    title = discardedEntity.title,
                    description = discardedEntity.description,
                    startDate = discardedEntity.startDate,
                    startTime = discardedEntity.startTime,
                    endDate = discardedEntity.endDate,
                    endTime = discardedEntity.endTime,
                    allDay = discardedEntity.allDay,
                    location = "",
                    buttonStatus = 2
                )
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error discarding event: ${e.message}")
                null
            }
        }
    }




    suspend fun updateEvent(notificationId: String, updatedEvent: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entity = EventEntity(
                    id = notificationId,
                    title = updatedEvent.title,
                    description = updatedEvent.description,
                    startDate = updatedEvent.startDate,
                    startTime = updatedEvent.startTime,
                    endDate = updatedEvent.endDate,
                    endTime = updatedEvent.endTime,
                    allDay = updatedEvent.allDay,
                    location = updatedEvent.location,
                    buttonStatus =  updatedEvent.buttonStatus
                )
                eventDao.insertEvent(entity)
                Log.d("EventsRepository", "Event updated in Room DB: $entity")
                true
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error updating event: ${e.message}")
                false
            }
        }
    }
}
