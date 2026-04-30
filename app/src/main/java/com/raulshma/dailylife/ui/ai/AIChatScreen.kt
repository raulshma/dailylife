package com.raulshma.dailylife.ui.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.data.ai.AIFeatureExecutor
import com.raulshma.dailylife.data.ai.LiteRTEngineService
import com.raulshma.dailylife.domain.ChatMessage
import com.raulshma.dailylife.domain.ChatRole
import com.raulshma.dailylife.domain.EngineState
import com.raulshma.dailylife.ui.ai.components.AIAvatar
import com.raulshma.dailylife.ui.ai.components.AIGradientAccent
import com.raulshma.dailylife.ui.ai.components.AIModelLoadingOverlay
import com.raulshma.dailylife.ui.ai.components.AINoModelCard
import com.raulshma.dailylife.ui.ai.components.AISuggestionChip
import com.raulshma.dailylife.ui.ai.components.AIStatusChip
import com.raulshma.dailylife.ui.ai.components.AITypingIndicator
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIChatScreen(
    aiExecutor: AIFeatureExecutor,
    engineService: LiteRTEngineService,
    onBack: () -> Unit,
    onNavigateToModelManager: () -> Unit = {},
) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var streamingText by remember { mutableStateOf("") }
    var modelLoadInitiated by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val engineState by engineService.engineState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Track whether user is near bottom for auto-scroll
    var userAtBottom by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        modelLoadInitiated = true
        aiExecutor.ensureModelForFeature(com.raulshma.dailylife.domain.AIFeature.CHAT)
    }

    LaunchedEffect(messages.size, streamingText) {
        if (userAtBottom && (messages.isNotEmpty() || streamingText.isNotEmpty())) {
            listState.animateScrollToItem(maxOf(0, messages.size - 1))
        }
    }

    val suggestedPrompts = listOf(
        "Summarize last week",
        "What did I do on Monday?",
        "Find my task entries",
        "Reflect on my mood",
    )

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
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "AI Chat",
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
                        AIStatusChip(
                            engineState = engineState,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    },
                    windowInsets = WindowInsets.safeDrawing,
                )
                AIGradientAccent()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            val isModelLoading = engineState is EngineState.LoadingModel ||
                engineState is EngineState.Initializing ||
                (modelLoadInitiated && engineState is EngineState.Idle)

            if (isModelLoading) {
                AIModelLoadingOverlay(engineState = engineState)
            } else if (engineState is EngineState.Error || engineState is EngineState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    AINoModelCard(onNavigateToModelManager = onNavigateToModelManager)
                }
            } else {
                if (messages.isEmpty() && streamingText.isEmpty()) {
                    EmptyChatState(
                        suggestedPrompts = suggestedPrompts,
                        onPromptClick = { prompt ->
                            messages.add(ChatMessage(ChatRole.USER, prompt))
                            isGenerating = true
                            streamingText = ""
                            scope.launch {
                                try {
                                    aiExecutor.chatWithJournal(prompt, emptyList()).collect { chunk ->
                                        streamingText = chunk
                                    }
                                    if (streamingText.isNotBlank()) {
                                        messages.add(ChatMessage(ChatRole.ASSISTANT, streamingText))
                                    }
                                } catch (e: Exception) {
                                    messages.add(ChatMessage(ChatRole.ASSISTANT, "Error: ${e.message}"))
                                } finally {
                                    streamingText = ""
                                    isGenerating = false
                                }
                            }
                        },
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { Spacer(Modifier.height(8.dp)) }

                        items(messages, key = { it.timestamp }) { message ->
                            ChatBubble(
                                message = message,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Copied to clipboard")
                                    }
                                },
                            )
                        }

                        if (streamingText.isNotEmpty()) {
                            item {
                                ChatBubble(
                                    message = ChatMessage(
                                        role = ChatRole.ASSISTANT,
                                        content = streamingText,
                                    ),
                                    isStreaming = true,
                                )
                            }
                        } else if (isGenerating) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    AIAvatar(isUser = false)
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 80.dp, max = 120.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = 16.dp,
                                                    bottomEnd = 16.dp,
                                                )
                                            )
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                            .padding(12.dp),
                                    ) {
                                        AITypingIndicator()
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about your journal...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = engineState is EngineState.Ready && !isGenerating,
                )
                if (isGenerating) {
                    FilledTonalIconButton(
                        onClick = {
                            engineService.cancelGeneration()
                            isGenerating = false
                            if (streamingText.isNotEmpty()) {
                                messages.add(ChatMessage(ChatRole.ASSISTANT, streamingText))
                                streamingText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop")
                    }
                } else {
                    FilledIconButton(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isEmpty()) return@FilledIconButton
                            messages.add(ChatMessage(ChatRole.USER, text))
                            inputText = ""
                            isGenerating = true
                            streamingText = ""
                            scope.launch {
                                try {
                                    aiExecutor.chatWithJournal(text, emptyList()).collect { chunk ->
                                        streamingText = chunk
                                    }
                                    if (streamingText.isNotBlank()) {
                                        messages.add(ChatMessage(ChatRole.ASSISTANT, streamingText))
                                    }
                                } catch (e: Exception) {
                                    messages.add(ChatMessage(ChatRole.ASSISTANT, "Error: ${e.message}"))
                                } finally {
                                    streamingText = ""
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = engineState is EngineState.Ready && inputText.isNotBlank(),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    suggestedPrompts: List<String>,
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.weight(1f))
        com.raulshma.dailylife.ui.ai.components.AIEmptyState(
            title = "Ask about your journal",
            subtitle = "Find entries, summarize periods, or reflect on patterns",
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            suggestedPrompts.forEach { prompt ->
                AISuggestionChip(
                    label = prompt,
                    onClick = { onPromptClick(prompt) },
                )
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isStreaming: Boolean = false,
    onCopy: (() -> Unit)? = null,
) {
    val isUser = message.role == ChatRole.USER
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bubbleMaxWidth = screenWidth * 0.82f
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isUser) {
            AIAvatar(isUser = false)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 60.dp, max = bubbleMaxWidth)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isUser) 20.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp,
                        )
                    )
                    .background(
                        if (isUser) {
                            val primary = MaterialTheme.colorScheme.primary
                            val primaryDark = MaterialTheme.colorScheme.primaryContainer
                            Brush.verticalGradient(colors = listOf(primary, primaryDark))
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surfaceContainer,
                                )
                            )
                        }
                    )
                    .padding(14.dp),
            ) {
                Column {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isStreaming) {
                        Spacer(Modifier.height(4.dp))
                        AITypingIndicator()
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            ) {
                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
                if (!isUser && onCopy != null) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            AIAvatar(isUser = true)
        }
    }
}
