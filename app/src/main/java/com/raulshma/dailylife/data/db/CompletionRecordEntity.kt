package com.raulshma.dailylife.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completion_records")
data class CompletionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val occurrenceDate: String,
    val completedAt: String,
    val missed: Boolean,
)
