package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "life_items")
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
    val notificationEnabled: Boolean,
    val notificationTimeOverride: String?,
    val notificationFlexibleWindow: Int?,
    val notificationSnoozeMinutes: Int?,
)
