package com.raulshma.dailylife.ui

import android.content.Intent
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.raulshma.dailylife.domain.BackupResult
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.domain.NotificationSettings
import com.raulshma.dailylife.domain.S3BackupSettings
import com.raulshma.dailylife.ui.ai.components.AIStatusChip
import com.raulshma.dailylife.ui.theme.MaterialYouColorPicker
import com.raulshma.dailylife.ui.theme.PalettePreviewStrip
import com.raulshma.dailylife.ui.theme.PaletteSuggestion
import com.raulshma.dailylife.ui.theme.saveCustomPalette
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    notificationSettings: NotificationSettings,
    s3Settings: S3BackupSettings,
    lastBackupResult: BackupResult?,
    onSaveNotifications: (NotificationSettings) -> Unit,
    onSaveS3: (S3BackupSettings) -> Unit,
    onBackupNow: () -> Unit,
    onClearResult: () -> Unit,
    onNavigateToModelManager: () -> Unit = {},
    aiStorageUsed: Long = 0L,
    defaultModelName: String? = null,
    isAiEnabled: Boolean = true,
    onAiEnabledChanged: (Boolean) -> Unit = {},
    engineState: EngineState = EngineState.Idle,
    isEnrichmentEnabled: Boolean = false,
    onNavigateToEnrichment: () -> Unit = {},
    onPaletteChanged: () -> Unit = {},
    onViewUpcomingReminders: () -> Unit = {},
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                NotificationsSection(
                    settings = notificationSettings,
                    onSave = onSaveNotifications,
                    onViewUpcomingReminders = onViewUpcomingReminders,
                )
            }
            item {
                CloudBackupSection(
                    settings = s3Settings,
                    lastResult = lastBackupResult,
                    onSave = onSaveS3,
                    onBackupNow = onBackupNow,
                    onClearResult = onClearResult,
                )
            }
            item {
                AIAssistantSection(
                    onNavigateToModelManager = onNavigateToModelManager,
                    storageUsed = aiStorageUsed,
                    defaultModelName = defaultModelName,
                    isAiEnabled = isAiEnabled,
                    onAiEnabledChanged = onAiEnabledChanged,
                    engineState = engineState,
                    isEnrichmentEnabled = isEnrichmentEnabled,
                    onNavigateToEnrichment = onNavigateToEnrichment,
                )
            }
            item {
                AppSettingsSection(onPaletteChanged = onPaletteChanged)
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationsSection(
    settings: NotificationSettings,
    onSave: (NotificationSettings) -> Unit,
    onViewUpcomingReminders: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var globalEnabled by rememberSaveable(settings) { mutableStateOf(settings.globalEnabled) }
    var preferredTime by rememberSaveable(settings) {
        mutableStateOf(settings.preferredTime.format(TimeFormatter))
    }
    var flexibleWindow by rememberSaveable(settings) {
        mutableStateOf(settings.flexibleWindowMinutes.toString())
    }
    var snooze by rememberSaveable(settings) {
        mutableStateOf(settings.defaultSnoozeMinutes.toString())
    }
    var batchNotifications by rememberSaveable(settings) { mutableStateOf(settings.batchNotifications) }
    var respectDnd by rememberSaveable(settings) { mutableStateOf(settings.respectDoNotDisturb) }
    var gracePeriod by rememberSaveable(settings) {
        mutableStateOf(settings.missedGracePeriodMinutes.toString())
    }
    var vibrationEnabled by rememberSaveable(settings) { mutableStateOf(settings.vibrationEnabled) }
    var canScheduleExactAlarms by remember { mutableStateOf(context.canScheduleExactAlarms()) }
    var canPostNotifications by remember { mutableStateOf(checkNotificationPermission(context)) }

    val ringtonePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<android.net.Uri>(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canScheduleExactAlarms = context.canScheduleExactAlarms()
                canPostNotifications = checkNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canPostNotifications) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Notifications are disabled",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = "DailyLife needs permission to show reminder notifications. Tap below to grant access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open notification settings")
                        }
                    }
                }
            }

            ToggleRow(
                icon = if (globalEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                label = "Global notifications",
                checked = globalEnabled,
                onCheckedChange = { globalEnabled = it },
            )

            OutlinedTextField(
                value = preferredTime,
                onValueChange = { preferredTime = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Preferred time") },
                placeholder = { Text("HH:mm") },
                leadingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = flexibleWindow,
                    onValueChange = { flexibleWindow = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Window (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = snooze,
                    onValueChange = { snooze = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Snooze (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            OutlinedTextField(
                value = gracePeriod,
                onValueChange = { gracePeriod = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Missed grace period (min)") },
                placeholder = { Text("30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Auto-mark as missed if not acted on within this time. 0 = disabled.") },
            )

            ToggleRow(
                icon = Icons.Filled.EventRepeat,
                label = "Batch reminders",
                checked = batchNotifications,
                onCheckedChange = { batchNotifications = it },
            )

            ToggleRow(
                icon = Icons.Filled.CheckCircle,
                label = "Respect Do Not Disturb",
                checked = respectDnd,
                onCheckedChange = { respectDnd = it },
            )

            ToggleRow(
                icon = Icons.Filled.Notifications,
                label = "Vibration",
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Exact alarm access",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (canScheduleExactAlarms) {
                                "Enabled. Reminders can run at exact times when no flexible window is used."
                            } else {
                                "Not enabled. DailyLife will still schedule reminders, but the system may delay delivery."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { context.openExactAlarmSettings() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage exact alarm access")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onViewUpcomingReminders,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.EventRepeat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View upcoming reminders")
            }

            Button(
                onClick = {
                    onSave(
                        NotificationSettings(
                            globalEnabled = globalEnabled,
                            preferredTime = parseTimeOrNull(preferredTime) ?: settings.preferredTime,
                            flexibleWindowMinutes = flexibleWindow.toIntOrNull()?.coerceAtLeast(0)
                                ?: settings.flexibleWindowMinutes,
                            defaultSnoozeMinutes = snooze.toIntOrNull()?.coerceAtLeast(1)
                                ?: settings.defaultSnoozeMinutes,
                            batchNotifications = batchNotifications,
                            respectDoNotDisturb = respectDnd,
                            missedGracePeriodMinutes = gracePeriod.toIntOrNull()?.coerceAtLeast(0)
                                ?: settings.missedGracePeriodMinutes,
                            notificationSoundUri = settings.notificationSoundUri,
                            vibrationEnabled = vibrationEnabled,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save notifications")
            }
        }
    }
}

@Composable
private fun CloudBackupSection(
    settings: S3BackupSettings,
    lastResult: BackupResult?,
    onSave: (S3BackupSettings) -> Unit,
    onBackupNow: () -> Unit,
    onClearResult: () -> Unit,
) {
    var enabled by rememberSaveable(settings) { mutableStateOf(settings.enabled) }
    var endpoint by rememberSaveable(settings) { mutableStateOf(settings.endpoint) }
    var bucketName by rememberSaveable(settings) { mutableStateOf(settings.bucketName) }
    var region by rememberSaveable(settings) { mutableStateOf(settings.region) }
    var accessKeyId by rememberSaveable(settings) { mutableStateOf(settings.accessKeyId) }
    var secretAccessKey by rememberSaveable(settings) { mutableStateOf(settings.secretAccessKey) }
    var pathPrefix by rememberSaveable(settings) { mutableStateOf(settings.pathPrefix) }
    var autoBackup by rememberSaveable(settings) { mutableStateOf(settings.autoBackup) }
    var backupFrequencyHours by rememberSaveable(settings) {
        mutableStateOf(settings.backupFrequencyHours.toString())
    }
    var encryptBackups by rememberSaveable(settings) { mutableStateOf(settings.encryptBackups) }

    val canBackup = enabled && endpoint.isNotBlank() && bucketName.isNotBlank() &&
        accessKeyId.isNotBlank() && secretAccessKey.isNotBlank()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "Cloud Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider()

            ToggleRow(
                icon = Icons.Filled.CloudUpload,
                label = "Enable S3 backup",
                checked = enabled,
                onCheckedChange = { enabled = it },
            )

            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("S3 endpoint URL") },
                placeholder = { Text("https://s3.amazonaws.com") },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bucketName,
                    onValueChange = { bucketName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Bucket") },
                )
                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Region") },
                )
            }

            OutlinedTextField(
                value = accessKeyId,
                onValueChange = { accessKeyId = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Access key ID") },
            )

            OutlinedTextField(
                value = secretAccessKey,
                onValueChange = { secretAccessKey = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Secret access key") },
                visualTransformation = PasswordVisualTransformation(),
            )

            OutlinedTextField(
                value = pathPrefix,
                onValueChange = { pathPrefix = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Path prefix") },
            )

            ToggleRow(
                icon = Icons.Filled.EventRepeat,
                label = "Auto-backup",
                checked = autoBackup,
                onCheckedChange = { autoBackup = it },
            )

            if (autoBackup) {
                OutlinedTextField(
                    value = backupFrequencyHours,
                    onValueChange = { backupFrequencyHours = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Frequency (hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            ToggleRow(
                icon = Icons.Filled.CheckCircle,
                label = "Encrypt backups",
                checked = encryptBackups,
                onCheckedChange = { encryptBackups = it },
            )

            lastResult?.let { result ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = when (result) {
                            is BackupResult.Success -> MaterialTheme.colorScheme.primaryContainer
                            is BackupResult.Failure -> MaterialTheme.colorScheme.errorContainer
                        },
                    ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = when (result) {
                                is BackupResult.Success -> "Backup started"
                                is BackupResult.Failure -> "Backup failed"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = when (result) {
                                is BackupResult.Success ->
                                    "${result.itemsBackedUp} items, ${result.mediaFilesBackedUp} media files queued."
                                is BackupResult.Failure -> result.reason
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(onClick = onClearResult) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onBackupNow,
                    enabled = canBackup,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup now")
                }
                Button(
                    onClick = {
                        onSave(
                            S3BackupSettings(
                                enabled = enabled,
                                endpoint = endpoint,
                                bucketName = bucketName,
                                region = region,
                                accessKeyId = accessKeyId,
                                secretAccessKey = secretAccessKey,
                                pathPrefix = pathPrefix.ifBlank { "dailylife" },
                                autoBackup = autoBackup,
                                backupFrequencyHours = backupFrequencyHours.toIntOrNull()?.coerceAtLeast(1)
                                    ?: settings.backupFrequencyHours,
                                encryptBackups = encryptBackups,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun HorizontalDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun AIAssistantSection(
    onNavigateToModelManager: () -> Unit,
    storageUsed: Long,
    defaultModelName: String?,
    isAiEnabled: Boolean,
    onAiEnabledChanged: (Boolean) -> Unit,
    engineState: EngineState,
    isEnrichmentEnabled: Boolean,
    onNavigateToEnrichment: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable smart features", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Smart titles, tags, reflections, and more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isAiEnabled,
                    onCheckedChange = onAiEnabledChanged,
                )
            }

            if (isAiEnabled) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AIStatusChip(engineState = engineState)
                    if (defaultModelName != null) {
                        Text(
                            text = "\u00B7 $defaultModelName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (storageUsed > 0) {
                    Text(
                        text = "Storage: ${formatStorageBytes(storageUsed)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToModelManager,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Manage Models")
                }

                OutlinedButton(
                    onClick = onNavigateToEnrichment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enrichment")
                    if (isEnrichmentEnabled) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "ON",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                }
            }
        }
    }
}
}

private fun checkNotificationPermission(context: android.content.Context): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    return permissionGranted && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun formatStorageBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${"%.0f".format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${"%.0f".format(mb)} MB"
    val gb = mb / 1024.0
    return "${"%.1f".format(gb)} GB"
}

@Composable
private fun AppSettingsSection(onPaletteChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dailylife_prefs", android.content.Context.MODE_PRIVATE) }
    var lockEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("lock_enabled", false)) }
    var dynamicColor by rememberSaveable { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }
    var selectedPalette by remember { mutableStateOf<PaletteSuggestion?>(null) }

    val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }

    fun confirmAndEnableLock(enabled: Boolean) {
        if (!enabled) {
            lockEnabled = false
            prefs.edit().putBoolean("lock_enabled", false).apply()
            return
        }
        val activity = context as? androidx.fragment.app.FragmentActivity ?: return
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.getSystemService(android.app.KeyguardManager::class.java)?.isDeviceSecure == true)
        if (!canAuth) return

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm app lock")
            .setDescription("Authenticate to enable app lock")
            .setAllowedAuthenticators(authenticators)
            .build()

        BiometricPrompt(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                lockEnabled = true
                prefs.edit().putBoolean("lock_enabled", true).apply()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
            override fun onAuthenticationFailed() {}
        }).authenticate(promptInfo)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Filled.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text("App settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("App lock (biometric/PIN)", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Require authentication to open the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = lockEnabled,
                    onCheckedChange = { enabled -> confirmAndEnableLock(enabled) },
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic colors (Material You)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Use your wallpaper colors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { enabled ->
                            dynamicColor = enabled
                            prefs.edit().putBoolean("dynamic_color", enabled).apply()
                            if (!enabled) {
                                prefs.edit().saveCustomPalette(null).apply()
                                onPaletteChanged()
                            }
                        },
                    )
                }

                if (dynamicColor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MaterialYouColorPicker(
                        onPaletteSelected = { palette ->
                            selectedPalette = palette
                            prefs.edit().saveCustomPalette(palette).apply()
                            onPaletteChanged()
                        },
                        selectedPalette = selectedPalette,
                    )
                }
            }
        }
    }
}
