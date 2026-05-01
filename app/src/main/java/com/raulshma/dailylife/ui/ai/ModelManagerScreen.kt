package com.raulshma.dailylife.ui.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.data.ai.ModelManager
import com.raulshma.dailylife.domain.AIModel
import com.raulshma.dailylife.domain.AIModelCapability
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.domain.ModelDownloadState
import com.raulshma.dailylife.ui.ai.components.AIGradientAccent
import com.raulshma.dailylife.ui.ai.components.AIMetadataBadge
import com.raulshma.dailylife.ui.ai.components.AIStatusChip
import com.raulshma.dailylife.ui.theme.DailyLifeDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    modelManager: ModelManager,
    engineService: LiteRTEngineService? = null,
    onBack: () -> Unit,
) {
    val catalog by modelManager.catalog.collectAsState()
    val isLoading by modelManager.isLoadingCatalog.collectAsState()
    val catalogError by modelManager.catalogError.collectAsState()
    val storageBytes by remember { mutableStateOf(modelManager.getStorageUsage()) }
    val scope = rememberCoroutineScope()
    val engineState by (engineService?.engineState?.collectAsState() ?: remember { mutableStateOf<EngineState>(EngineState.Idle) })
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    val filteredCatalog = remember(catalog, searchQuery) {
        if (searchQuery.isBlank()) catalog
        else catalog.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        if (catalog.isEmpty()) {
            modelManager.fetchCatalog()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.SmartToy,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "Models",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = { scope.launch { modelManager.fetchCatalog() } }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Refresh")
                        }
                    },
                    windowInsets = WindowInsets.safeDrawing,
                )
                AIGradientAccent()
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "On-Device Intelligence",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "Download models to enable smart features. All processing happens locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (engineService != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AIStatusChip(engineState = engineState)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (catalog.size > 3) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search models...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* no-op */ }),
                )
                Spacer(Modifier.height(8.dp))
            }

            if (storageBytes > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Filled.Memory,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Storage: ${formatBytes(storageBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            if (isLoading && catalog.isEmpty()) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (catalogError != null && catalog.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = catalogError ?: "Error loading catalog",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(
                            onClick = { scope.launch { modelManager.fetchCatalog() } },
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredCatalog, key = { it.id }) { model ->
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
    val downloadSpeed by modelManager.getDownloadSpeed(model.id).collectAsState("")
    val isDownloaded = downloadState is ModelDownloadState.Downloaded
    val isDownloading = downloadState is ModelDownloadState.Downloading || downloadState is ModelDownloadState.Resuming
    val isResuming = downloadState is ModelDownloadState.Resuming

    val borderColor = if (isDefault && isDownloaded) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isDefault && isDownloaded) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDefault && isDownloaded) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                CardDefaults.elevatedCardColors().containerColor
            },
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
                        tint = if (isDefault) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CapabilityChip("Text", AIModelCapability.TEXT_GENERATION in model.capabilities)
                CapabilityChip("Vision", AIModelCapability.VISION in model.capabilities)
                CapabilityChip("Audio", AIModelCapability.AUDIO in model.capabilities)
                CapabilityChip("Tools", AIModelCapability.TOOL_CALLING in model.capabilities)
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val params = extractParameters(model.name)
                if (params != null) {
                    AIMetadataBadge(label = "Params", value = params, highlighted = true)
                }
                val quant = extractQuantization(model.name)
                if (quant != null) {
                    AIMetadataBadge(label = "Quant", value = quant)
                }
                AIMetadataBadge(label = "RAM", value = "~${model.ramRequiredMb}MB")
                AIMetadataBadge(label = "Size", value = formatBytes(model.fileSizeBytes))
            }

            Spacer(Modifier.height(12.dp))

            when (downloadState) {
                is ModelDownloadState.NotDownloaded -> {
                    Button(
                        onClick = { modelManager.downloadModel(model) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Download ${formatBytes(model.fileSizeBytes)}")
                    }
                }
                is ModelDownloadState.Resuming -> {
                    val progress = (downloadState as ModelDownloadState.Resuming).progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${(progress * 100).toInt()}% — Interrupted",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = { modelManager.clearDownload(model.id) }) {
                            Text("Clear")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { modelManager.resumeModel(model) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Resume Download")
                    }
                }
                is ModelDownloadState.Downloading -> {
                    val progress = (downloadState as ModelDownloadState.Downloading).progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${(progress * 100).toInt()}%${if (downloadSpeed.isNotBlank()) " · $downloadSpeed" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
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
                                shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(12.dp),
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
    val bgColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(DailyLifeDuration.SHORT),
        label = "chipText",
    )
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
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

private fun extractParameters(name: String): String? {
    val regex = "(\\d+(?:\\.\\d+)?)B".toRegex(RegexOption.IGNORE_CASE)
    return regex.find(name)?.groupValues?.get(1)?.let { "${it}B" }
}

private fun extractQuantization(name: String): String? {
    val regex = "(Q\\d+(?:[_-][A-Z]+)?)".toRegex(RegexOption.IGNORE_CASE)
    return regex.find(name)?.groupValues?.get(1)?.uppercase()
}
