package com.raulshma.dailylife.ui.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.CompletionRecord
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.ui.OpenStreetMapPreview
import com.raulshma.dailylife.ui.components.StaggeredEnter
import com.raulshma.dailylife.ui.components.rememberStaggeredVisibility
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HistoryTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
private val HistoryDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
private val EditTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val EditDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionHistoryScreen(
    item: LifeItem,
    onBack: () -> Unit,
    onUpdateRecord: (Long, CompletionRecord) -> Unit,
    onDeleteRecord: (Long, LocalDate, LocalDateTime) -> Unit,
) {
    var showMissed by remember { mutableStateOf(true) }
    val allRecords = item.completionHistory.sortedByDescending { it.completedAt }
    val displayRecords = if (showMissed) allRecords else allRecords.filter { !it.missed }

    val completedCount = allRecords.count { !it.missed }
    val missedCount = allRecords.count { it.missed }

    var editingRecord by remember { mutableStateOf<CompletionRecord?>(null) }
    var deletingRecord by remember { mutableStateOf<CompletionRecord?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Completion History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SummaryChipsRow(
                completedCount = completedCount,
                missedCount = missedCount,
                showMissed = showMissed,
                onShowMissedChanged = { showMissed = it },
            )

            if (displayRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No completion records yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 32.dp,
                    ),
                ) {
                    itemsIndexed(
                        displayRecords,
                        key = { _, record -> "${record.occurrenceDate}-${record.completedAt}-${record.missed}" },
                    ) { index, record ->
                        val visible = rememberStaggeredVisibility(index, baseDelayMs = 60, maxDelayMs = 400)
                        AnimatedVisibility(visibleState = visible, enter = StaggeredEnter) {
                            CompletionHistoryCard(
                                record = record,
                                onEdit = { editingRecord = record },
                                onDelete = { deletingRecord = record },
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingRecord != null) {
        val record = editingRecord!!
        EditCompletionSheet(
            record = record,
            onSave = { updated ->
                onUpdateRecord(item.id, updated)
                editingRecord = null
            },
            onDismiss = { editingRecord = null },
        )
    }

    if (deletingRecord != null) {
        val record = deletingRecord!!
        AlertDialog(
            onDismissRequest = { deletingRecord = null },
            title = { Text("Delete record") },
            text = {
                Text("Remove this ${if (record.missed) "missed" else "completion"} record from ${record.occurrenceDate.format(EditDateFormatter)}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(item.id, record.occurrenceDate, record.completedAt)
                        deletingRecord = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRecord = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SummaryChipsRow(
    completedCount: Int,
    missedCount: Int,
    showMissed: Boolean,
    onShowMissedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = true,
            onClick = {},
            label = { Text("$completedCount completed") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
        if (missedCount > 0) {
            FilterChip(
                selected = showMissed,
                onClick = { onShowMissedChanged(!showMissed) },
                label = { Text("$missedCount missed") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun CompletionHistoryCard(
    record: CompletionRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (record.missed) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (record.missed) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (record.missed) Icons.Filled.Close else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (record.missed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (record.missed) "Missed" else "Completed",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (record.missed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                    Text(
                        text = record.occurrenceDate.format(HistoryDateFormatter),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = record.completedAt.format(HistoryTimeFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (!record.note.isNullOrBlank()) {
                Text(
                    text = record.note!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val metadataRows = buildList {
                if (record.latitude != null && record.longitude != null) {
                    add("Location" to "%.4f, %.4f".format(record.latitude, record.longitude))
                }
                if (record.batteryLevel != null) {
                    add("Battery" to "${record.batteryLevel}%")
                }
                if (record.appVersion != null) {
                    add("App version" to record.appVersion)
                }
            }

            if (metadataRows.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    metadataRows.forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = when (label) {
                                    "Location" -> Icons.Filled.LocationOn
                                    "Battery" -> Icons.Filled.BatteryStd
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(80.dp),
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            if (record.latitude != null && record.longitude != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    OpenStreetMapPreview(
                        latitude = record.latitude,
                        longitude = record.longitude,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCompletionSheet(
    record: CompletionRecord,
    onSave: (CompletionRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editDate by remember { mutableStateOf(record.completedAt.toLocalDate()) }
    var editTime by remember { mutableStateOf(record.completedAt.toLocalTime()) }
    var editNote by remember { mutableStateOf(record.note ?: "") }
    var editLatitude by remember { mutableStateOf(record.latitude?.toString() ?: "") }
    var editLongitude by remember { mutableStateOf(record.longitude?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (record.missed) "Edit missed record" else "Edit completion record",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                editDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            editDate.year,
                            editDate.monthValue - 1,
                            editDate.dayOfMonth,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(editDate.format(EditDateFormatter), maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                editTime = LocalTime.of(hourOfDay, minute)
                            },
                            editTime.hour,
                            editTime.minute,
                            true,
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(editTime.format(EditTimeFormatter), maxLines = 1)
                }
            }

            OutlinedTextField(
                value = editNote,
                onValueChange = { editNote = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note") },
                placeholder = { Text("Add a note about this completion...") },
                maxLines = 4,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = editLatitude,
                    onValueChange = { editLatitude = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Latitude") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = editLongitude,
                    onValueChange = { editLongitude = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Longitude") },
                    singleLine = true,
                )
            }

            if (record.batteryLevel != null || record.appVersion != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (record.batteryLevel != null) {
                        Text(
                            text = "Battery at completion: ${record.batteryLevel}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (record.appVersion != null) {
                        Text(
                            text = "App version: ${record.appVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val lat = editLatitude.toDoubleOrNull()
                        val lon = editLongitude.toDoubleOrNull()
                        onSave(
                            record.copy(
                                completedAt = LocalDateTime.of(editDate, editTime),
                                occurrenceDate = editDate,
                                note = editNote.ifBlank { null },
                                latitude = lat,
                                longitude = lon,
                            ),
                        )
                    },
                ) {
                    Text("Save")
                }
            }
        }
    }
}
