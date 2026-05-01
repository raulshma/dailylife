package com.raulshma.dailylife.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import com.raulshma.dailylife.domain.LifeItemType

internal fun LifeItemType.icon(): ImageVector = when (this) {
    LifeItemType.Thought -> Icons.Filled.Lightbulb
    LifeItemType.Note -> Icons.Filled.EditNote
    LifeItemType.Task -> Icons.Filled.Checklist
    LifeItemType.Reminder -> Icons.Filled.Alarm
    LifeItemType.Photo -> Icons.Filled.PhotoCamera
    LifeItemType.Video -> Icons.Filled.Videocam
    LifeItemType.Audio -> Icons.Filled.Mic
    LifeItemType.Location -> Icons.Filled.LocationOn
    LifeItemType.Pdf -> Icons.Filled.PictureAsPdf
    LifeItemType.Mixed -> Icons.Filled.Category
}

internal fun LifeItemType.isMediaLike(): Boolean =
    this == LifeItemType.Photo ||
        this == LifeItemType.Video ||
        this == LifeItemType.Audio ||
        this == LifeItemType.Location ||
        this == LifeItemType.Pdf ||
        this == LifeItemType.Mixed
