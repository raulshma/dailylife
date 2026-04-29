package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_records",
    foreignKeys = [
        ForeignKey(
            entity = LifeItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("itemId"),
        Index("occurrenceDate"),
        Index(value = ["itemId", "occurrenceDate", "completedAt"], unique = true),
    ],
)
data class CompletionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val occurrenceDate: String,
    val completedAt: String,
    val missed: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val batteryLevel: Int? = null,
    val appVersion: String? = null,
    val note: String? = null,
)
