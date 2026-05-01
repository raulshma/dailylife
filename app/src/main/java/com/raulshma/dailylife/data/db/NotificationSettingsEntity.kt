package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val globalEnabled: Boolean,
    val preferredTime: String,
    val flexibleWindowMinutes: Int,
    val defaultSnoozeMinutes: Int,
    val batchNotifications: Boolean,
    val respectDoNotDisturb: Boolean,
    val missedGracePeriodMinutes: Int = 30,
    val notificationSoundUri: String? = null,
    val vibrationEnabled: Boolean = true,
)
