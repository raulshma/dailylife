package com.raulshma.dailylife.ui.ai

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.domain.LifeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReflectionDateRange(
    val label: String,
    val start: LocalDate,
    val end: LocalDate,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIReflectionsScreen(
    aiExecutor: AIFeatureExecutor,
    engineService: LiteRTEngineService,
    recentEntries: List<LifeItem>,
    onBack: () -> Unit,
) {
    val today = LocalDate.now()
    val dateRanges = remember {
        listOf(
            ReflectionDateRange("Today", today, today),
            ReflectionDateRange("This Week", today.minusDays(7), today),
            ReflectionDateRange("This Month", today.minusMonths(1), today),
            ReflectionDateRange("Last 3 Months", today.minusMonths(3), today),
        )
    }
    var selectedRange by remember { mutableStateOf(dateRanges[1]) }
    var reflectionText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val isEngineReady by engineService.isEngineReady.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("AI Reflection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isGenerating) {
                        IconButton(onClick = {
                            engineService.cancelGeneration()
                            isGenerating = false
                        }) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop")
                        }
                    } else if (reflectionText.isNotEmpty()) {
                        IconButton(onClick = {
                            reflectionText = ""
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                        }
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
            if (!isEngineReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No AI model loaded. Download a model from Settings > AI Models.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = "Generate an AI reflection based on your journal entries.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dateRanges.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        label = { Text(range.label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val filteredEntries = remember(selectedRange, recentEntries) {
                recentEntries.filter { entry ->
                    val date = entry.createdAt.toLocalDate()
                    !date.isBefore(selectedRange.start) && !date.isAfter(selectedRange.end)
                }
            }

            Text(
                text = "${filteredEntries.size} entries in range",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            if (isGenerating || reflectionText.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                ) {
                    if (reflectionText.isEmpty() && isGenerating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Generating reflection...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        text = reflectionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                    )
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (filteredEntries.isEmpty()) {
                            Text(
                                "No entries in this date range.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    reflectionText = ""
                    isGenerating = true
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            aiExecutor.generateReflection(
                                entries = filteredEntries,
                                startDate = selectedRange.start,
                                endDate = selectedRange.end,
                            ).collect { chunk ->
                                reflectionText = chunk
                            }
                        } catch (e: Exception) {
                            reflectionText = "Error generating reflection: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = isEngineReady && filteredEntries.isNotEmpty() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Text("Generate Reflection")
            }
        }
    }
}
