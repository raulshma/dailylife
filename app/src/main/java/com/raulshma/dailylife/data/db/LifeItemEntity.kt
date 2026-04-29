package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "life_items",
    indices = [
        Index("createdAt"),
        Index("type"),
        Index("isFavorite"),
        Index("isArchived"),
    ],
)
data class LifeItemEntity(
    @PrimaryKey
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val tags: String,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val taskStatus: String?,
    val reminderAt: String?,
    val recurrenceFrequency: String,
    val recurrenceInterval: Int,
    val recurrenceDaysOfWeek: String = "",
    val recurrenceDayOfWeek: String? = null,
    val recurrenceWeekOfMonth: String? = null,
    val notificationEnabled: Boolean,
    val notificationTimeOverride: String?,
    val notificationFlexibleWindow: Int?,
    val notificationSnoozeMinutes: Int?,
    val geofenceLatitude: Double? = null,
    val geofenceLongitude: Double? = null,
    val geofenceRadiusMeters: Float = 200f,
    val geofenceTrigger: String = "Arrival",
    val isArchived: Boolean = false,
)
