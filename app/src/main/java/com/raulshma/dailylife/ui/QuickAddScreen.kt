package com.raulshma.dailylife.ui

import android.content.Context
import android.net.Uri
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.raulshma.dailylife.ui.components.icon
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeSpring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.raulshma.dailylife.data.media.MediaThumbnailGenerator
import com.raulshma.dailylife.data.security.EncryptionProgress
import com.raulshma.dailylife.domain.DayOfWeek
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.domain.GeofenceTrigger
import com.raulshma.dailylife.domain.ItemNotificationSettings
import com.raulshma.dailylife.domain.LifeItemDraft
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.RecurrenceFrequency
import com.raulshma.dailylife.domain.RecurrenceRule
import com.raulshma.dailylife.domain.TaskStatus
import com.raulshma.dailylife.domain.WeekOfMonth
import com.raulshma.dailylife.domain.WritingTone
import com.raulshma.dailylife.ui.capture.AudioRecorder
import androidx.compose.material3.CircularProgressIndicator
import com.raulshma.dailylife.ui.capture.SpeechTranscriber
import com.raulshma.dailylife.ui.capture.hasAudioPermission
import com.raulshma.dailylife.ui.capture.hasCameraPermission
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

private enum class TypeDetectMode { OFF, LOGIC, AI }

@Composable
private fun typeDetectChipIcon(mode: TypeDetectMode): (@Composable () -> Unit)? = when (mode) {
    TypeDetectMode.OFF -> null
    TypeDetectMode.LOGIC -> {{ Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) }}
    TypeDetectMode.AI -> {{ Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickAddScreen(
    draft: QuickAddDraft,
    onDraftChanged: (QuickAddDraft) -> Unit,
    onAdd: (LifeItemDraft) -> Unit,
    onAddAndContinue: (LifeItemDraft) -> Unit,
    onDismiss: () -> Unit,
    onDiscardDraft: () -> Unit,
    mediaLauncher: com.raulshma.dailylife.ui.capture.MediaCaptureLauncher,
    onShowLocationPicker: (Double?, Double?, String?, (Double, Double, String) -> Unit) -> Unit,
    allTags: List<String> = emptyList(),
    isEditMode: Boolean = false,
    encryptionProgress: EncryptionProgress? = null,
    aiSmartTitle: String = "",
    aiTagSuggestions: List<String> = emptyList(),
    aiRewrittenText: String = "",
    isAiGenerating: Boolean = false,
    engineState: EngineState = EngineState.Idle,
    onGenerateSmartTitle: ((String) -> Unit)? = null,
    onSuggestTags: ((String, String) -> Unit)? = null,
    onRewriteText: ((String, WritingTone) -> Unit)? = null,
    onApplyAiTitle: (String) -> Unit = {},
    onApplyAiTags: (List<String>) -> Unit = {},
    onApplyAiRewrite: (String) -> Unit = {},
    onClearAiState: () -> Unit = {},
    aiInferredType: LifeItemType? = null,
    onInferTypeWithAI: ((String, String) -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        QuickAddContent(
            modifier = Modifier.weight(1f),
            initialDraft = draft,
            onDraftChanged = onDraftChanged,
            onAdd = onAdd,
            onAddAndContinue = onAddAndContinue,
            onDiscardDraft = onDiscardDraft,
            mediaLauncher = mediaLauncher,
            onShowLocationPicker = onShowLocationPicker,
            allTags = allTags,
            isEditMode = isEditMode,
            encryptionProgress = encryptionProgress,
            aiSmartTitle = aiSmartTitle,
            aiTagSuggestions = aiTagSuggestions,
            aiRewrittenText = aiRewrittenText,
            isAiGenerating = isAiGenerating,
            engineState = engineState,
            onGenerateSmartTitle = onGenerateSmartTitle,
            onSuggestTags = onSuggestTags,
            onRewriteText = onRewriteText,
            onApplyAiTitle = onApplyAiTitle,
            onApplyAiTags = onApplyAiTags,
            onApplyAiRewrite = onApplyAiRewrite,
            onClearAiState = onClearAiState,
            aiInferredType = aiInferredType,
            onInferTypeWithAI = onInferTypeWithAI,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddContent(
    modifier: Modifier = Modifier,
    initialDraft: QuickAddDraft,
    onDraftChanged: (QuickAddDraft) -> Unit,
    onAdd: (LifeItemDraft) -> Unit,
    onAddAndContinue: (LifeItemDraft) -> Unit,
    onDiscardDraft: () -> Unit,
    mediaLauncher: com.raulshma.dailylife.ui.capture.MediaCaptureLauncher,
    onShowLocationPicker: (Double?, Double?, String?, (Double, Double, String) -> Unit) -> Unit,
    allTags: List<String> = emptyList(),
    isEditMode: Boolean = false,
    encryptionProgress: EncryptionProgress? = null,
    aiSmartTitle: String = "",
    aiTagSuggestions: List<String> = emptyList(),
    aiRewrittenText: String = "",
    isAiGenerating: Boolean = false,
    engineState: EngineState = EngineState.Idle,
    onGenerateSmartTitle: ((String) -> Unit)? = null,
    onSuggestTags: ((String, String) -> Unit)? = null,
    onRewriteText: ((String, WritingTone) -> Unit)? = null,
    onApplyAiTitle: (String) -> Unit = {},
    onApplyAiTags: (List<String>) -> Unit = {},
    onApplyAiRewrite: (String) -> Unit = {},
    onClearAiState: () -> Unit = {},
    aiInferredType: LifeItemType? = null,
    onInferTypeWithAI: ((String, String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val initialType = remember(initialDraft.typeName) {
        LifeItemType.entries.firstOrNull { it.name == initialDraft.typeName } ?: LifeItemType.Thought
    }
    
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    var showSketchCanvas by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf(initialDraft.title) }
    var body by rememberSaveable { mutableStateOf(initialDraft.body) }
    var tags by rememberSaveable { mutableStateOf(initialDraft.tags) }
    var favorite by rememberSaveable { mutableStateOf(initialDraft.favorite) }
    var pinned by rememberSaveable { mutableStateOf(initialDraft.pinned) }
    var reminderDate by rememberSaveable { mutableStateOf(initialDraft.reminderDate) }
    var reminderTime by rememberSaveable { mutableStateOf(initialDraft.reminderTime) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(initialDraft.notificationsEnabled) }
    var overrideTime by rememberSaveable { mutableStateOf(initialDraft.overrideTime) }
    var recurring by rememberSaveable { mutableStateOf(initialDraft.recurring) }
    var recurrenceFrequency by rememberSaveable { mutableStateOf(initialDraft.recurrenceFrequency) }
    var recurrenceDaysOfWeek by rememberSaveable { mutableStateOf(initialDraft.recurrenceDaysOfWeek) }
    var recurrenceDayOfWeek by rememberSaveable { mutableStateOf(initialDraft.recurrenceDayOfWeek) }
    var recurrenceWeekOfMonth by rememberSaveable { mutableStateOf(initialDraft.recurrenceWeekOfMonth) }
    var geofenceLatitude by rememberSaveable { mutableStateOf(initialDraft.geofenceLatitude) }
    var geofenceLongitude by rememberSaveable { mutableStateOf(initialDraft.geofenceLongitude) }
    var geofenceTrigger by rememberSaveable { mutableStateOf(initialDraft.geofenceTrigger) }
    var createdDate by rememberSaveable { mutableStateOf(initialDraft.createdDate) }
    var createdTime by rememberSaveable { mutableStateOf(initialDraft.createdTime) }

    var showAdvanced by rememberSaveable { mutableStateOf(initialDraft.showAdvanced) }
    var showReminderOptions by rememberSaveable { mutableStateOf(initialDraft.showReminderOptions) }
    
    var isRecordingAudio by remember { mutableStateOf(false) }
    var activeRecordingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAudioUri by remember { mutableStateOf<Uri?>(null) }
    var liveTranscription by rememberSaveable { mutableStateOf("") }
    var titleBubbleDismissed by remember { mutableStateOf(false) }
    var rewriteBubbleDismissed by remember { mutableStateOf(false) }
    var showSavedIndicator by remember { mutableStateOf(false) }
    var typeDetectMode by remember {
        val saved = context.getSharedPreferences("dailylife_prefs", Context.MODE_PRIVATE)
            .getString("type_detect_mode", null)
        mutableStateOf(
            try { TypeDetectMode.valueOf(saved ?: "LOGIC") }
            catch (_: Exception) { TypeDetectMode.LOGIC }
        )
    }
    val audioRecorder = remember { AudioRecorder(context) }
    val speechTranscriber = remember { SpeechTranscriber(context) }

    val titleFocusRequester = remember { FocusRequester() }
    val bodyFocusRequester = remember { FocusRequester() }
    val inferredType = remember(body) { inferTypeFromBody(body) }
    val effectiveInferredType = when (typeDetectMode) {
        TypeDetectMode.OFF -> null
        TypeDetectMode.LOGIC -> inferredType
        TypeDetectMode.AI -> aiInferredType
    }

    fun currentDraftSnapshot(): QuickAddDraft = QuickAddDraft(
        typeName = selectedType.name,
        title = title,
        body = body,
        tags = tags,
        favorite = favorite,
        pinned = pinned,
        reminderDate = reminderDate,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        overrideTime = overrideTime,
        recurring = recurring,
        recurrenceFrequency = recurrenceFrequency,
        recurrenceDaysOfWeek = recurrenceDaysOfWeek,
        recurrenceDayOfWeek = recurrenceDayOfWeek,
        recurrenceWeekOfMonth = recurrenceWeekOfMonth,
        geofenceLatitude = geofenceLatitude,
        geofenceLongitude = geofenceLongitude,
        geofenceTrigger = geofenceTrigger,
        showAdvanced = showAdvanced,
        showReminderOptions = showReminderOptions,
        createdDate = createdDate,
        createdTime = createdTime,
    )

    fun parseRecurrenceRule(): RecurrenceRule {
        val freq = RecurrenceFrequency.entries.firstOrNull { it.name.equals(recurrenceFrequency, ignoreCase = true) }
            ?: RecurrenceFrequency.None
        if (freq == RecurrenceFrequency.None) return RecurrenceRule()
        return RecurrenceRule(
            frequency = freq,
            daysOfWeek = recurrenceDaysOfWeek.split(",").mapNotNull { s ->
                DayOfWeek.entries.firstOrNull { it.name.equals(s.trim(), ignoreCase = true) }
            }.toSet(),
            dayOfWeek = DayOfWeek.entries.firstOrNull { it.name.equals(recurrenceDayOfWeek, ignoreCase = true) },
            weekOfMonth = WeekOfMonth.entries.firstOrNull { it.name.equals(recurrenceWeekOfMonth, ignoreCase = true) },
        )
    }

    fun buildDraftPayload(): LifeItemDraft = LifeItemDraft(
        type = effectiveInferredType ?: selectedType,
        title = title,
        body = body,
        tags = parseTags(tags),
        isFavorite = favorite,
        isPinned = pinned,
        taskStatus = if (selectedType == LifeItemType.Task) TaskStatus.Open else null,
        reminderAt = parseReminderDateTime(reminderDate, reminderTime),
        recurrenceRule = parseRecurrenceRule(),
        notificationSettings = ItemNotificationSettings(
            enabled = notificationsEnabled,
            timeOverride = parseTimeOrNull(overrideTime),
            geofenceLatitude = geofenceLatitude.toDoubleOrNull(),
            geofenceLongitude = geofenceLongitude.toDoubleOrNull(),
            geofenceTrigger = GeofenceTrigger.entries.firstOrNull { it.name.equals(geofenceTrigger, ignoreCase = true) }
                ?: GeofenceTrigger.Arrival,
        ),
        createdAt = parseReminderDateTime(createdDate, createdTime),
    )

    fun resetLocalDraft() {
        selectedType = LifeItemType.Thought
        title = ""
        body = ""
        tags = ""
        favorite = false
        pinned = false
        reminderDate = ""
        reminderTime = ""
        notificationsEnabled = true
        overrideTime = ""
        recurring = false
        recurrenceFrequency = RecurrenceFrequency.None.name
        recurrenceDaysOfWeek = ""
        recurrenceDayOfWeek = ""
        recurrenceWeekOfMonth = ""
        geofenceLatitude = ""
        geofenceLongitude = ""
        geofenceTrigger = "Arrival"
        showAdvanced = false
        showReminderOptions = false
        createdDate = ""
        createdTime = ""
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.isRecording) {
                audioRecorder.cancelRecording()
            }
            speechTranscriber.destroy()
        }
    }

    fun startAudioCapture() {
        pendingAudioUri = null
        liveTranscription = ""
        isRecordingAudio = true
        val transcriberStarted = speechTranscriber.start(
            onTranscriptChanged = { transcript -> liveTranscription = transcript },
            onError = { errorMsg ->
                if (liveTranscription.isBlank()) liveTranscription = errorMsg
            }
        )
        if (!transcriberStarted) {
            liveTranscription = "Speech recognition not available on this device."
        }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isRecordingAudio) {
                val uri = audioRecorder.startRecording()
                if (uri != null) activeRecordingUri = uri
            }
        }, 500L)
    }

    fun stopAudioCapture() {
        if (!isRecordingAudio) return
        val stoppedUri = if (audioRecorder.isRecording) audioRecorder.stopRecording() else null
        val finalTranscript = speechTranscriber.stop()
        isRecordingAudio = false
        pendingAudioUri = stoppedUri ?: activeRecordingUri
        activeRecordingUri = null
        if (finalTranscript.isNotBlank()) liveTranscription = finalTranscript
    }

    fun discardCapturedAudio() {
        pendingAudioUri = null
        activeRecordingUri = null
        liveTranscription = ""
        speechTranscriber.cancel()
        if (audioRecorder.isRecording) {
            audioRecorder.cancelRecording()
        } else {
            audioRecorder.discardLastRecording()
        }
    }

    fun reRecordAudio() {
        discardCapturedAudio()
        startAudioCapture()
    }

    fun addCapturedAudioToDraft() {
        val uri = pendingAudioUri
        val transcript = liveTranscription.trim()
        if (uri != null) {
            body = buildString {
                append(uri.toString())
                if (transcript.isNotBlank()) {
                    append("\n\n")
                    append(transcript)
                }
            }
            selectedType = LifeItemType.Audio
        } else if (transcript.isNotBlank()) {
            body = transcript
        }
        pendingAudioUri = null
        liveTranscription = ""
        audioRecorder.clearLastRecordingReference()
    }

    val canSave = title.isNotBlank() || body.isNotBlank()

    LaunchedEffect(initialDraft.body) {
        if (body != initialDraft.body) {
            body = initialDraft.body
        }
    }

    LaunchedEffect(initialDraft.typeName) {
        val draftType = LifeItemType.entries.firstOrNull { it.name == initialDraft.typeName }
            ?: LifeItemType.Thought
        if (selectedType != draftType) {
            selectedType = draftType
        }
    }

    LaunchedEffect(initialDraft.title) {
        if (title != initialDraft.title) {
            title = initialDraft.title
        }
    }

    LaunchedEffect(initialDraft.tags) {
        if (tags != initialDraft.tags) {
            tags = initialDraft.tags
        }
    }

    LaunchedEffect(showSavedIndicator) {
        if (showSavedIndicator) {
            delay(1800)
            showSavedIndicator = false
        }
    }

    LaunchedEffect(aiSmartTitle) {
        if (aiSmartTitle.isNotBlank()) titleBubbleDismissed = false
    }

    LaunchedEffect(aiRewrittenText) {
        if (aiRewrittenText.isNotBlank()) rewriteBubbleDismissed = false
    }

    LaunchedEffect(typeDetectMode) {
        context.getSharedPreferences("dailylife_prefs", Context.MODE_PRIVATE)
            .edit().putString("type_detect_mode", typeDetectMode.name).apply()
    }

    LaunchedEffect(typeDetectMode, body) {
        if (typeDetectMode == TypeDetectMode.AI && body.isNotBlank() && onInferTypeWithAI != null) {
            delay(1500)
            val snapshotTitle = title
            val snapshotBody = body
            if (snapshotBody.isNotBlank()) {
                onInferTypeWithAI(snapshotTitle, snapshotBody)
            }
        }
    }

    LaunchedEffect(
        selectedType, title, body, tags, favorite, pinned,
        reminderDate, reminderTime, notificationsEnabled,
        overrideTime, recurring, showAdvanced, showReminderOptions,
        createdDate, createdTime,
    ) {
        onDraftChanged(currentDraftSnapshot())
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Timestamp, Templates and Discard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Text(
                        text = "Edit item",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                } else {
                    Text(
                        text = "Today, ${LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEditMode) {
                        var showTemplates by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { showTemplates = true }) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Templates")
                            }
                            DropdownMenu(
                                expanded = showTemplates,
                                onDismissRequest = { showTemplates = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Daily Reflection") },
                                    onClick = {
                                        selectedType = LifeItemType.Note
                                        title = "Daily Reflection"
                                        body = "1. What went well today?\n2. What could be improved?\n3. What am I grateful for?"
                                        showTemplates = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Meeting Notes") },
                                    onClick = {
                                        selectedType = LifeItemType.Note
                                        title = "Meeting: "
                                        body = "Attendees:\n\nAgenda:\n- \n\nAction Items:\n- [ ] "
                                        showTemplates = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Quick Task") },
                                    onClick = {
                                        selectedType = LifeItemType.Task
                                        title = "Task"
                                        body = "Details: "
                                        showTemplates = false
                                    }
                                )
                            }
                        }
                        
                        Box(modifier = Modifier.height(20.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
                    }
                    
                    TextButton(onClick = onDiscardDraft) {
                        Text(if (isEditMode) "Cancel" else "Discard")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = if (typeDetectMode != TypeDetectMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Detect type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TypeDetectMode.entries.forEach { mode ->
                        val label = when (mode) {
                            TypeDetectMode.OFF -> "Off"
                            TypeDetectMode.LOGIC -> "Logic"
                            TypeDetectMode.AI -> "Smart"
                        }
                        FilterChip(
                            selected = typeDetectMode == mode,
                            onClick = { typeDetectMode = mode },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = typeDetectChipIcon(mode),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            // Type Selector (all badges, horizontally scrollable)
            val typeScrollState = rememberScrollState()
            val showTypeScrollCue = typeScrollState.maxValue > 0 && typeScrollState.value < typeScrollState.maxValue
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(typeScrollState)
                        .padding(end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LifeItemType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (selectedType != type) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedType = type
                            },
                            label = { Text(type.label) },
                            leadingIcon = {
                                Icon(imageVector = type.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showTypeScrollCue,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                ),
                            )
                            .padding(start = 18.dp, end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "More types",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Auto-detected type hint
            AnimatedVisibility(
                visible = typeDetectMode != TypeDetectMode.OFF && effectiveInferredType != null && effectiveInferredType != selectedType,
                enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                        + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
                exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                        + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        effectiveInferredType?.let {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedType = it
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Detected ${effectiveInferredType?.label ?: ""}. Tap to switch.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Content Area: Borderless Title & Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { if (it.length <= 120) title = it },
                        modifier = Modifier.weight(1f).focusRequester(titleFocusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    "What's on your mind?",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (body.isNotBlank() && title.isBlank() && onGenerateSmartTitle != null) {
                        IconButton(
                            onClick = { onGenerateSmartTitle(body) },
                            enabled = !isAiGenerating,
                            modifier = Modifier.size(32.dp),
                        ) {
                            when {
                                engineState is EngineState.LoadingModel || engineState is EngineState.Initializing -> {
                                    LinearProgressIndicator(
                                        modifier = Modifier.width(20.dp).height(2.dp),
                                    )
                                }
                                isAiGenerating && aiSmartTitle.isBlank() -> {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                                else -> {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = "Generate title",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = aiSmartTitle.isNotBlank() && !titleBubbleDismissed,
                    enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                            + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
                    exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                            + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
                ) {
                    AIGenerationBubble(
                        text = aiSmartTitle,
                        label = "Smart Title",
                        isGenerating = false,
                        onApply = {
                            onApplyAiTitle(aiSmartTitle)
                            titleBubbleDismissed = true
                        },
                        onDismiss = { titleBubbleDismissed = true },
                    )
                }

                // Body
                if (selectedType == LifeItemType.Task) {
                    ChecklistEditor(
                        body = body,
                        onBodyChange = { if (it.length <= 2000) body = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).focusRequester(bodyFocusRequester)
                    )
                } else {
                    BasicTextField(
                        value = body,
                        onValueChange = { if (it.length <= 2000) body = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).focusRequester(bodyFocusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (body.isEmpty()) {
                                Text(
                                    "Add details, #tags, or paste links here...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                // Character counters (only show when > 80% used)
                if (title.length > 96 || body.length > 1600) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (title.length > 96) {
                            Text(
                                "${title.length}/120", 
                                style = MaterialTheme.typography.labelSmall,
                                color = if (title.length >= 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (title.length > 96 && body.length > 1600) Spacer(modifier = Modifier.width(8.dp))
                        if (body.length > 1600) {
                            Text(
                                "Body: ${body.length}/2000", 
                                style = MaterialTheme.typography.labelSmall,
                                color = if (body.length >= 2000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (onRewriteText != null && selectedType != LifeItemType.Task) {
                AnimatedVisibility(
                    visible = aiRewrittenText.isNotBlank() && !rewriteBubbleDismissed,
                    enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                            + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
                    exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                            + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
                ) {
                    AIGenerationBubble(
                        text = aiRewrittenText,
                        label = "Smart Rewrite",
                        isGenerating = false,
                        onApply = {
                            onApplyAiRewrite(aiRewrittenText)
                            rewriteBubbleDismissed = true
                        },
                        onDismiss = { rewriteBubbleDismissed = true },
                    )
                }

                if (body.isNotBlank() && (aiRewrittenText.isBlank() || rewriteBubbleDismissed)) {
                    var showTonePicker by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Box {
                            TextButton(
                                onClick = { showTonePicker = true },
                                enabled = !isAiGenerating,
                            ) {
                                when {
                                    engineState is EngineState.LoadingModel || engineState is EngineState.Initializing -> {
                                        LinearProgressIndicator(
                                            modifier = Modifier.width(48.dp).height(2.dp),
                                        )
                                    }
                                    isAiGenerating -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 1.5.dp,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Rewriting...",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            Icons.Filled.AutoFixHigh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Smart Rewrite",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showTonePicker,
                                onDismissRequest = { showTonePicker = false },
                            ) {
                                WritingTone.entries.forEach { tone ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                tone.name.replace("_", " ")
                                                    .lowercase()
                                                    .replaceFirstChar { it.titlecase() }
                                            )
                                        },
                                        onClick = {
                                            onRewriteText(body, tone)
                                            showTonePicker = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isRecordingAudio || pendingAudioUri != null || liveTranscription.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                AudioRecordingCard(
                    isRecordingAudio = isRecordingAudio,
                    pendingAudioUri = pendingAudioUri,
                    liveTranscription = liveTranscription,
                    audioRecorder = audioRecorder,
                    onStop = ::stopAudioCapture,
                    onDiscard = ::discardCapturedAudio,
                    onReRecord = ::reRecordAudio,
                    onAddRecording = ::addCapturedAudioToDraft,
                )
            }

            // Visual Attachments Grid
            val attachedUris = remember(body) {
                body.split("\\s+".toRegex()).filter {
                    it.startsWith("content://") || it.startsWith("file://") || it.startsWith("geo:") || it.startsWith("http")
                }
            }
            
            if (attachedUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    attachedUris.forEach { uriStr ->
                        AttachmentPreviewCard(
                            uriStr = uriStr,
                            onRemove = { body = body.replace(uriStr, "").trim() }
                        )
                    }
                }
            }

            // Inline Tags Editor
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val currentTagSet = parseTags(tags)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Label, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (tags.isEmpty()) {
                                Text(
                                    "Add tags (comma separated)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Tag Suggestions
                val suggestions = remember(allTags, currentTagSet) {
                    allTags.filter { it !in currentTagSet }.take(6)
                }
                
                if (suggestions.isNotEmpty() || currentTagSet.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentTagSet.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    // Remove tag logic
                                    val tagPattern = Regex("(^|,\\s*)$tag(\\s*,|\$)")
                                    tags = tags.replace(tagPattern, "$1").trim(',', ' ')
                                },
                                label = { Text(tag) },
                                trailingIcon = { Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                        
                        suggestions.forEach { tag ->
                            AssistChip(
                                onClick = { tags = if (tags.isBlank()) tag else "$tags, $tag" },
                                label = { Text("+$tag") }
                            )
                        }

                        if (onSuggestTags != null && aiTagSuggestions.isNotEmpty()) {
                            aiTagSuggestions.forEach { tag ->
                                if (tag !in currentTagSet) {
                                    AssistChip(
                                        onClick = { onApplyAiTags(listOf(tag)) },
                                        label = { Text("+$tag") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                    )
                                }
                            }
                        } else if (onSuggestTags != null && (title.isNotBlank() || body.isNotBlank()) && currentTagSet.size < 5) {
                            AssistChip(
                                onClick = { onSuggestTags(title, body) },
                                label = {
                                    when {
                                        engineState is EngineState.LoadingModel || engineState is EngineState.Initializing -> {
                                            LinearProgressIndicator(
                                                modifier = Modifier.width(48.dp).height(2.dp),
                                            )
                                        }
                                        isAiGenerating && aiTagSuggestions.isEmpty() -> {
                                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                        }
                                        else -> {
                                            Text("Smart Tags")
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Properties Quick Bar
            val currentMood = remember(tags) {
                Regex("#mood-([a-zA-Z]+)").find(tags)?.groupValues?.get(1)
            }
            var showMoodPicker by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    FilterChip(
                        selected = currentMood != null,
                        onClick = { showMoodPicker = true },
                        label = { Text(currentMood?.replaceFirstChar { it.uppercase() } ?: "Mood") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Mood,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showMoodPicker,
                        onDismissRequest = { showMoodPicker = false }
                    ) {
                        val moods = listOf("awful" to "😫 Awful", "bad" to "😕 Bad", "neutral" to "😐 Neutral", "good" to "🙂 Good", "excellent" to "🤩 Excellent")
                        moods.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    var newTags = tags.replace(Regex("#mood-[a-zA-Z]+(,\\s*)?"), "").trim(',', ' ')
                                    if (newTags.isNotBlank()) newTags += ", "
                                    newTags += "#mood-$id"
                                    tags = newTags
                                    showMoodPicker = false
                                }
                            )
                        }
                        if (currentMood != null) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Clear mood", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    tags = tags.replace(Regex("#mood-[a-zA-Z]+(,\\s*)?"), "").trim(',', ' ')
                                    showMoodPicker = false
                                }
                            )
                        }
                    }
                }
                
                FilterChip(
                    selected = favorite,
                    onClick = { favorite = !favorite },
                    label = { Text("Favorite") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = pinned,
                    onClick = { pinned = !pinned },
                    label = { Text("Pin") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(onClick = { showReminderOptions = !showReminderOptions }) {
                    Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showReminderOptions) "Hide reminder" else "Add reminder")
                }
            }

            // Collapsible Reminder Section
            AnimatedVisibility(
                visible = showReminderOptions,
                enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                        + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
                exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                        + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
            ) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reminder Details", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        ReminderDateTimeRow(
                            reminderDate = parseDateOrNull(reminderDate),
                            reminderTime = parseTimeOrNull(reminderTime),
                            onDateClick = {
                                showDatePicker(
                                    context = context,
                                    initialDate = parseDateOrNull(reminderDate) ?: LocalDate.now(),
                                    onDateSelected = { selected ->
                                        reminderDate = selected.toString()
                                        if (reminderTime.isBlank()) reminderTime = "09:00"
                                    }
                                )
                            },
                            onTimeClick = {
                                showTimePicker(
                                    context = context,
                                    initialTime = parseTimeOrNull(reminderTime) ?: parseTimeOrNull(overrideTime) ?: LocalTime.of(9, 0),
                                    onTimeSelected = { selected ->
                                        if (reminderDate.isBlank()) reminderDate = LocalDate.now().toString()
                                        reminderTime = selected.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                    }
                                )
                            },
                            onClear = { reminderDate = ""; reminderTime = "" }
                        )
                        
                        TextButton(onClick = { showAdvanced = !showAdvanced }) {
                            Text(if (showAdvanced) "Hide advanced options" else "Advanced options")
                        }
                        
                        AnimatedVisibility(
                            visible = showAdvanced,
                            enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                                    + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
                            exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                                    + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ToggleRow(
                                    icon = if (notificationsEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                    label = "Notifications",
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it }
                                )
                                if (notificationsEnabled) {
                                    OutlinedTextField(
                                        value = overrideTime,
                                        onValueChange = { overrideTime = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text("Time override, HH:mm") },
                                        leadingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) }
                                    )
                                }
                                ToggleRow(
                                    icon = Icons.Filled.EventRepeat,
                                    label = "Daily recurrence",
                                    checked = recurring,
                                    onCheckedChange = { recurring = it }
                                )
                                if (!isEditMode) {
                                    Text(
                                        text = "Created date/time",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    ReminderDateTimeRow(
                                        reminderDate = parseDateOrNull(createdDate),
                                        reminderTime = parseTimeOrNull(createdTime),
                                        onDateClick = {
                                            showDatePicker(
                                                context = context,
                                                initialDate = parseDateOrNull(createdDate) ?: LocalDate.now(),
                                                onDateSelected = { selected ->
                                                    createdDate = selected.toString()
                                                    if (createdTime.isBlank()) createdTime = LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                                }
                                            )
                                        },
                                        onTimeClick = {
                                            showTimePicker(
                                                context = context,
                                                initialTime = parseTimeOrNull(createdTime) ?: LocalTime.now(),
                                                onTimeSelected = { selected ->
                                                    if (createdDate.isBlank()) createdDate = LocalDate.now().toString()
                                                    createdTime = selected.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                                }
                                            )
                                        },
                                        onClear = { createdDate = ""; createdTime = "" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // padding for bottom bar
        }

        AnimatedVisibility(
            visible = showSavedIndicator,
            enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                    + expandVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)),
            exit = fadeOut(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Exit))
                    + shrinkVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Exit)),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Item saved!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Smart Toolbar and Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Attachments Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            if (hasCameraPermission(context)) mediaLauncher.launchCamera() else mediaLauncher.requestCameraPermissionIfNeeded()
                        }) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera")
                        }
                        IconButton(onClick = { mediaLauncher.launchPhotoPicker() }) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Photos")
                        }
                        IconButton(onClick = {
                            if (hasCameraPermission(context)) mediaLauncher.launchVideoCamera() else mediaLauncher.requestCameraPermissionIfNeeded()
                        }) {
                            Icon(Icons.Filled.Videocam, contentDescription = "Video")
                        }
                        IconButton(onClick = { showSketchCanvas = true }) {
                            Icon(Icons.Filled.Create, contentDescription = "Sketch")
                        }
                    }
                    
                    // Vertical divider
                    Box(modifier = Modifier.height(24.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {
                            if (isRecordingAudio) {
                                stopAudioCapture()
                            } else {
                                if (hasAudioPermission(context)) startAudioCapture() else mediaLauncher.requestAudioPermissionIfNeeded()
                            }
                        }) {
                            Icon(
                                imageVector = if (isRecordingAudio) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = if (isRecordingAudio) "Stop Recording" else "Audio",
                                tint = if (isRecordingAudio) MaterialTheme.colorScheme.error else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { mediaLauncher.launchFilePicker() }) {
                            Icon(Icons.Filled.EditNote, contentDescription = "File")
                        }
                        IconButton(onClick = {
                            val geoRegex = Regex("""geo:\s*[-+]?\d{1,2}(?:\.\d+)?,\s*[-+]?\d{1,3}(?:\.\d+)?(?:\?[^\s]*)?""", RegexOption.IGNORE_CASE)
                            val existingGeo = geoRegex.find(body)?.value
                            val existingLat = existingGeo?.let { Regex("""geo:\s*([-+]?\d{1,2}(?:\.\d+)?)""", RegexOption.IGNORE_CASE).find(it)?.groupValues?.get(1)?.toDoubleOrNull() }
                            val existingLon = existingGeo?.let { Regex("""geo:\s*[-+]?\d{1,2}(?:\.\d+)?,\s*([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE).find(it)?.groupValues?.get(1)?.toDoubleOrNull() }
                            val existingTile = existingGeo?.let { Regex("""[?&]mapTile=([^&\s]+)""", RegexOption.IGNORE_CASE).find(it)?.groupValues?.get(1) }
                            onShowLocationPicker(existingLat, existingLon, existingTile) { lat, lon, tile ->
                                val cleanedBody = body.replace(geoRegex, "").trim()
                                body = if (cleanedBody.isBlank()) "geo:$lat,$lon?mapTile=$tile" else "$cleanedBody\ngeo:$lat,$lon?mapTile=$tile"
                            }
                        }) {
                            Icon(Icons.Filled.LocationOn, contentDescription = "Location")
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Bottom Action Bar
                if (encryptionProgress != null) {
                    EncryptionProgressBar(
                        progress = encryptionProgress,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isEditMode) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showSavedIndicator = true
                                    onClearAiState()
                                    titleBubbleDismissed = false
                                    rewriteBubbleDismissed = false
                                    onAddAndContinue(buildDraftPayload())
                                    resetLocalDraft()
                                    titleFocusRequester.requestFocus()
                                },
                                enabled = canSave,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save & New")
                            }
                        }
                        
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showSavedIndicator = true
                                onClearAiState()
                                onAdd(buildDraftPayload())
                            },
                            enabled = canSave,
                            modifier = Modifier.weight(if (isEditMode) 1f else 1.5f)
                        ) {
                            Text(if (isEditMode) "Save changes" else "Save Item")
                        }
                    }
                }
            }
        }
    }

    if (showSketchCanvas) {
        SketchCanvasSheet(
            onSave = { uri ->
                body = if (body.isBlank()) uri.toString() else "$body\n$uri"
                showSketchCanvas = false
            },
            onDismiss = { showSketchCanvas = false }
        )
    }
}

@Composable
private fun EncryptionProgressBar(
    progress: EncryptionProgress,
    modifier: Modifier = Modifier,
) {
    val fraction = if (progress.totalBytes > 0) {
        (progress.bytesProcessed.toFloat() / progress.totalBytes.toFloat())
            .coerceIn(0f, 1f)
    } else {
        val stepFraction = (progress.currentStep - 1).toFloat() / progress.totalSteps.toFloat()
        stepFraction.coerceIn(0f, 1f)
    }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
            Text(
                text = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Encrypting ${progress.fileName}...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (progress.totalSteps > 1) {
            Text(
                text = "File ${progress.currentStep} of ${progress.totalSteps}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun AnimatedAudioWaveform(
    amplitudeSamples: List<Float>,
    modifier: Modifier = Modifier,
) {
    val maxBars = 48
    val recordedCount = amplitudeSamples.size.coerceAtMost(maxBars)
    val offset = maxBars - recordedCount

    val paddedSamples = remember(amplitudeSamples) {
        List(maxBars) { index ->
            val sampleIndex = index - offset
            if (sampleIndex >= 0) amplitudeSamples.getOrElse(sampleIndex) { 0f } else 0f
        }
    }

    val animatedHeights = paddedSamples.map { amp ->
        animateFloatAsState(
            targetValue = amp,
            animationSpec = DailyLifeSpring.Gentle,
            label = "waveBar"
        ).value
    }

    val waveColor = MaterialTheme.colorScheme.primary
    val waveTrackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)

    Canvas(modifier = modifier) {
        val barWidth = (size.width / maxBars.toFloat()) * 0.65f
        val gap = (size.width / maxBars.toFloat()) * 0.35f

        animatedHeights.forEachIndexed { index, amp ->
            val isRecorded = index >= offset
            val barHeight = (amp * size.height * 0.85f).coerceAtLeast(4f)
            val x = index * (barWidth + gap)
            val y = (size.height - barHeight) / 2f
            drawRoundRect(
                color = if (isRecorded) waveColor else waveTrackColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f),
            )
        }
    }
}

private enum class AttachmentType {
    Image, Video, Audio, Pdf, Location, Other
}

@Composable
private fun AttachmentPreviewCard(
    uriStr: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val attachmentType = remember(uriStr) {
        // First try inferring from the URI string patterns
        val inferred = when (inferTypeFromBody(uriStr)) {
            LifeItemType.Photo -> AttachmentType.Image
            LifeItemType.Video -> AttachmentType.Video
            LifeItemType.Audio -> AttachmentType.Audio
            LifeItemType.Pdf -> AttachmentType.Pdf
            LifeItemType.Location -> AttachmentType.Location
            else -> {
                when {
                    uriStr.contains("image", ignoreCase = true) -> AttachmentType.Image
                    uriStr.contains("video", ignoreCase = true) -> AttachmentType.Video
                    uriStr.contains("audio", ignoreCase = true) -> AttachmentType.Audio
                    uriStr.contains("pdf", ignoreCase = true) -> AttachmentType.Pdf
                    else -> null
                }
            }
        }
        // If type is unknown and it's a content URI, ask the ContentResolver for MIME type
        inferred ?: if (uriStr.startsWith("content://") || uriStr.startsWith("file://")) {
            val mimeType = runCatching {
                context.contentResolver.getType(Uri.parse(uriStr))
            }.getOrNull()
            when {
                mimeType?.startsWith("image/") == true -> AttachmentType.Image
                mimeType?.startsWith("video/") == true -> AttachmentType.Video
                mimeType?.startsWith("audio/") == true -> AttachmentType.Audio
                mimeType == "application/pdf" -> AttachmentType.Pdf
                else -> AttachmentType.Other
            }
        } else {
            AttachmentType.Other
        }
    }

    val videoThumbUri = remember(uriStr) {
        if (attachmentType == AttachmentType.Video) {
            runCatching {
                val generator = MediaThumbnailGenerator(context)
                generator.generateVideoThumbnail(Uri.parse(uriStr), context)?.toString()
            }.getOrNull()
        } else null
    }

    ElevatedCard(
        modifier = Modifier.width(140.dp).height(100.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachmentType) {
                AttachmentType.Image -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uriStr)
                            .size(280, 200)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                AttachmentType.Video -> {
                    if (videoThumbUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(videoThumbUri)
                                .size(280, 200)
                                .build(),
                            contentDescription = "Video thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                AttachmentType.Audio -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Audio",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Audio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                AttachmentType.Pdf -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                AttachmentType.Location -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                AttachmentType.Other -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "File",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uriStr.substringAfterLast("/").take(20),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).size(28.dp).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioRecordingCard(
    isRecordingAudio: Boolean,
    pendingAudioUri: Uri?,
    liveTranscription: String,
    audioRecorder: AudioRecorder,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onReRecord: () -> Unit,
    onAddRecording: () -> Unit,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val transcriptionScrollState = rememberScrollState()

    var playbackPosition by remember { mutableStateOf(0f) }
    var playbackDuration by remember { mutableStateOf(0) }
    var isSeeking by remember { mutableStateOf(false) }

    var amplitudeSamples by remember { mutableStateOf(listOf<Float>()) }
    var displayedElapsedMs by remember { mutableStateOf(0L) }

    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            amplitudeSamples = emptyList()
            while (isRecordingAudio) {
                val amp = audioRecorder.currentAmplitude
                displayedElapsedMs = audioRecorder.elapsedMs
                amplitudeSamples = (amplitudeSamples + amp).takeLast(48)
                delay(80L)
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val mp = mediaPlayer
            if (mp != null && mp.isPlaying && !isSeeking) {
                playbackPosition = mp.currentPosition.toFloat()
                playbackDuration = mp.duration
            }
            delay(200L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        playbackPosition = 0f
    }

    fun startPlayback(uri: Uri) {
        stopPlayback()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            setOnCompletionListener {
                isPlaying = false
                playbackPosition = 0f
            }
            prepare()
            playbackDuration = duration
            start()
        }
        isPlaying = true
    }

    fun togglePlayback() {
        val uri = pendingAudioUri ?: return
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        } else {
            val mp = mediaPlayer
            if (mp != null) {
                mp.start()
                isPlaying = true
            } else {
                startPlayback(uri)
            }
        }
    }

    LaunchedEffect(liveTranscription) {
        if (liveTranscription.isNotBlank()) {
            transcriptionScrollState.animateScrollTo(transcriptionScrollState.maxValue)
        }
    }

    val recordingPulseAlpha = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by recordingPulseAlpha.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = com.raulshma.dailylife.ui.theme.DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isRecordingAudio) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
                                shape = CircleShape,
                            ),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = when {
                        isRecordingAudio -> "Recording & transcribing"
                        isPlaying -> "Playing recording"
                        pendingAudioUri != null -> "Recording ready"
                        else -> "Transcription ready"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
                if (isRecordingAudio) {
                    Text(
                        text = formatTime(displayedElapsedMs),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (isRecordingAudio) {
                AnimatedAudioWaveform(
                    amplitudeSamples = amplitudeSamples,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                )
            }

            if (isRecordingAudio) {
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Stop recording")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isRecordingAudio) 100.dp else 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    .verticalScroll(transcriptionScrollState)
                    .padding(10.dp),
            ) {
                Text(
                    text = liveTranscription.ifBlank {
                        if (isRecordingAudio) "Listening\u2026" else "No transcription available."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            if (!isRecordingAudio && pendingAudioUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = { togglePlayback() },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        text = formatTime(playbackPosition.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Slider(
                        value = playbackPosition,
                        onValueChange = { newVal ->
                            isSeeking = true
                            playbackPosition = newVal
                        },
                        onValueChangeFinished = {
                            mediaPlayer?.seekTo(playbackPosition.toInt())
                            isSeeking = false
                        },
                        valueRange = 0f..playbackDuration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatTime(playbackDuration.toLong()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = {
                        stopPlayback()
                        onDiscard()
                    }) {
                        Text("Discard")
                    }
                    OutlinedButton(onClick = {
                        stopPlayback()
                        onReRecord()
                    }) {
                        Text("Re-record")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        stopPlayback()
                        onAddRecording()
                    }) {
                        Text("Add")
                    }
                }
            }

            if (!isRecordingAudio && pendingAudioUri == null && liveTranscription.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    OutlinedButton(onClick = onDiscard) {
                        Text("Discard")
                    }
                    Button(onClick = onAddRecording) {
                        Text("Add text")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistEditor(
    body: String,
    onBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(body) {
        if (body.isBlank()) listOf(ChecklistItem("", ChecklistState.NotStarted))
        else body.split("\n").map { line ->
            val state = ChecklistState.fromPrefix(line)
            val text = when {
                line.startsWith("- [x] ", ignoreCase = true) -> line.substring(6)
                line.startsWith("- [-] ", ignoreCase = true) -> line.substring(6)
                line.startsWith("- [ ] ") -> line.substring(6)
                else -> line
            }
            ChecklistItem(text, state)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            val newItems = items.toMutableList()
                            newItems[index] = item.copy(state = item.state.next())
                            onBodyChange(newItems.joinToString("\n") { it.toMarkdown() })
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when (item.state) {
                        ChecklistState.NotStarted ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                                    ),
                            )
                        ChecklistState.InProgress ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                        ChecklistState.Done ->
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Done,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                    }
                }
                BasicTextField(
                    value = item.text,
                    onValueChange = { newText ->
                        val newItems = items.toMutableList()
                        if (newText.contains("\n")) {
                            val parts = newText.split("\n")
                            newItems[index] = item.copy(text = parts[0])
                            newItems.addAll(index + 1, parts.drop(1).map { ChecklistItem(it) })
                        } else {
                            newItems[index] = item.copy(text = newText)
                        }
                        onBodyChange(newItems.joinToString("\n") { it.toMarkdown() })
                    },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (item.state == ChecklistState.Done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.state == ChecklistState.Done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
                    decorationBox = { innerTextField ->
                        if (item.text.isEmpty() && index == items.size - 1) {
                            Text(
                                "Add item...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
                if (items.size > 1) {
                    IconButton(
                        onClick = {
                            val newItems = items.toMutableList()
                            newItems.removeAt(index)
                            onBodyChange(newItems.joinToString("\n") { it.toMarkdown() })
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        TextButton(
            onClick = {
                val newItems = items.toMutableList()
                newItems.add(ChecklistItem(""))
                onBodyChange(newItems.joinToString("\n") { it.toMarkdown() })
            },
            modifier = Modifier.padding(start = 36.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add item")
        }
    }
}

private enum class ChecklistState(val prefix: String) {
    NotStarted("- [ ] "),
    InProgress("- [-] "),
    Done("- [x] ");

    fun next(): ChecklistState = when (this) {
        NotStarted -> InProgress
        InProgress -> Done
        Done -> NotStarted
    }

    companion object {
        fun fromPrefix(line: String): ChecklistState = when {
            line.startsWith(Done.prefix, ignoreCase = true) -> Done
            line.startsWith(InProgress.prefix, ignoreCase = true) -> InProgress
            line.startsWith(NotStarted.prefix) -> NotStarted
            else -> NotStarted
        }
    }
}

private data class ChecklistItem(val text: String, val state: ChecklistState = ChecklistState.NotStarted) {
    fun toMarkdown() = "${state.prefix}$text"
}

@Composable
private fun AIGenerationBubble(
    text: String,
    label: String,
    isGenerating: Boolean,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else {
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onApply) {
                        Text("Apply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SketchCanvasSheet(
    onSave: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var paths by remember { mutableStateOf(listOf<androidx.compose.ui.graphics.Path>()) }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { paths = emptyList() }) {
                    Text("Clear")
                }
                Text("Quick Sketch", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = {
                        if (paths.isEmpty() && currentPath == null) {
                            onDismiss()
                            return@Button
                        }
                        val width = canvasSize.width.toInt().coerceAtLeast(1)
                        val height = canvasSize.height.toInt().coerceAtLeast(1)
                        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 12f
                            strokeJoin = android.graphics.Paint.Join.ROUND
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            isAntiAlias = true
                        }
                        paths.forEach { p ->
                            canvas.drawPath(p.asAndroidPath(), paint)
                        }
                        
                        val file = java.io.File(context.cacheDir, "sketch_${System.currentTimeMillis()}.jpg")
                        file.outputStream().use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        onSave(Uri.fromFile(file))
                    }
                ) {
                    Text("Save")
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = androidx.compose.ui.graphics.Path().apply { moveTo(offset.x, offset.y) }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath = androidx.compose.ui.graphics.Path().apply { 
                                    addPath(currentPath!!)
                                    lineTo(change.position.x, change.position.y)
                                }
                            },
                            onDragEnd = {
                                currentPath?.let { paths = paths + it }
                                currentPath = null
                            },
                            onDragCancel = {
                                currentPath = null
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().onSizeChanged { 
                        canvasSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) 
                    }
                ) {
                    paths.forEach { path ->
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 12f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                    currentPath?.let { path ->
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 12f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
