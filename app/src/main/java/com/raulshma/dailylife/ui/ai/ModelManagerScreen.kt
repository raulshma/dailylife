package com.raulshma.dailylife.ui.ai

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.ModelManager
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.AIModelCapability
import com.raulshma.dailylife.domain.ModelDownloadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    modelManager: ModelManager,
    onBack: () -> Unit,
) {
    val catalog by modelManager.catalog.collectAsState()
    val isLoading by modelManager.isLoadingCatalog.collectAsState()
    val catalogError by modelManager.catalogError.collectAsState()
    val storageBytes by remember { mutableStateOf(modelManager.getStorageUsage()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (catalog.isEmpty()) {
            modelManager.fetchCatalog()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("AI Models") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets.safeDrawing,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "On-Device AI Models",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Download models to enable AI features. All processing happens on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Storage used: ${formatBytes(storageBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { scope.launch { modelManager.fetchCatalog() } }) {
                    Text("Refresh")
                }
            }

            if (isLoading && catalog.isEmpty()) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (catalogError != null && catalog.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = catalogError ?: "Error loading catalog",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(catalog, key = { it.id }) { model ->
                    ModelCard(
                        model = model,
                        modelManager = modelManager,
                        isDefault = modelManager.getDefaultModelId() == model.id,
                        onSetDefault = { modelManager.setDefaultModelId(model.id) },
                        onDelete = { modelManager.deleteModel(model.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: AIModel,
    modelManager: ModelManager,
    isDefault: Boolean,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    val downloadState by modelManager.getDownloadState(model.id).collectAsState(ModelDownloadState.NotDownloaded)
    val isDownloaded = downloadState is ModelDownloadState.Downloaded
    val isDownloading = downloadState is ModelDownloadState.Downloading

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDefault) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                CardDefaults.elevatedCardColors().containerColor
            }
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (isDefault && isDownloaded) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Default model",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CapabilityChip("Text", AIModelCapability.TEXT_GENERATION in model.capabilities)
                CapabilityChip("Vision", AIModelCapability.VISION in model.capabilities)
                CapabilityChip("Audio", AIModelCapability.AUDIO in model.capabilities)
                CapabilityChip("Tools", AIModelCapability.TOOL_CALLING in model.capabilities)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatBytes(model.fileSizeBytes)} \u00B7 ~${model.ramRequiredMb}MB RAM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            when (downloadState) {
                is ModelDownloadState.NotDownloaded -> {
                    Button(
                        onClick = { modelManager.downloadModel(model) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Download ${formatBytes(model.fileSizeBytes)}")
                    }
                }
                is ModelDownloadState.Downloading -> {
                    val progress = (downloadState as ModelDownloadState.Downloading).progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        TextButton(onClick = { modelManager.cancelDownload(model.id) }) {
                            Text("Cancel")
                        }
                    }
                }
                is ModelDownloadState.Downloaded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (!isDefault) {
                            FilledTonalButton(
                                onClick = onSetDefault,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Set as Default")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Default")
                            }
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                is ModelDownloadState.DownloadFailed -> {
                    val error = (downloadState as ModelDownloadState.DownloadFailed).error
                    Text(
                        text = "Download failed: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { modelManager.downloadModel(model) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Retry Download")
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityChip(label: String, enabled: Boolean) {
    val color = if (enabled) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = color),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${"%.0f".format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${"%.0f".format(mb)} MB"
    val gb = mb / 1024.0
    return "${"%.1f".format(gb)} GB"
}
