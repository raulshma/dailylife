package com.raulshma.dailylife.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.ui.theme.DailyLifeDuration
import com.raulshma.dailylife.ui.theme.DailyLifeEasing
import com.raulshma.dailylife.ui.theme.DailyLifeSpring
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A tag-based graph view that places items as nodes and draws edges
 * between items that share at least one tag. Features calm spring-based
 * node layout animation and staggered edge reveal.
 */
@Composable
fun GraphViewScreen(
    items: List<LifeItem>,
    contentPadding: PaddingValues,
    onItemSelected: (Long) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val graph = remember(items) { buildTagGraph(items) }
    var selectedNodeId by remember { mutableStateOf<Long?>(null) }
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onTertiary = MaterialTheme.colorScheme.onTertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Animate nodes from center to target with spring physics
    val center = remember { Offset(400f, 500f) }
    val animatedPositions = graph.nodes.map { node ->
        val animX by animateFloatAsState(
            targetValue = node.position.x,
            animationSpec = spring(stiffness = 280f, dampingRatio = 0.9f, visibilityThreshold = 0.5f),
            label = "nodeX-${node.itemId}"
        )
        val animY by animateFloatAsState(
            targetValue = node.position.y,
            animationSpec = spring(stiffness = 280f, dampingRatio = 0.9f, visibilityThreshold = 0.5f),
            label = "nodeY-${node.itemId}"
        )
        node.itemId to Offset(animX, animY)
    }.toMap()

    // Edges fade in after nodes begin moving
    val edgeAlpha by animateFloatAsState(
        targetValue = if (graph.nodes.isNotEmpty()) 0.6f else 0f,
        animationSpec = tween(
            durationMillis = DailyLifeDuration.MEDIUM,
            delayMillis = DailyLifeDuration.SHORT,
            easing = DailyLifeEasing.Enter,
        ),
        label = "edgeAlpha"
    )

    // Selection pulse ring
    val pulseTransition = rememberInfiniteTransition(label = "nodePulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(900, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(900, easing = DailyLifeEasing.Ambient),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .safeDrawingPadding(),
    ) {
        if (graph.nodes.size < 2) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Add more tagged items to see connections",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceVariant,
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(graph) {
                        detectTapGestures { offset ->
                            val tapped = graph.nodes.find { node ->
                                val pos = animatedPositions[node.itemId] ?: node.position
                                val dx = offset.x - pos.x
                                val dy = offset.y - pos.y
                                sqrt(dx * dx + dy * dy) < NodeRadius * 2.5f
                            }
                            selectedNodeId = tapped?.itemId
                            tapped?.let { onItemSelected(it.itemId) }
                        }
                    },
            ) {
                // Draw edges with animated alpha
                graph.edges.forEach { edge ->
                    val start = animatedPositions[edge.from]
                        ?: graph.nodes.first { it.itemId == edge.from }.position
                    val end = animatedPositions[edge.to]
                        ?: graph.nodes.first { it.itemId == edge.to }.position
                    drawLine(
                        color = outlineVariant.copy(alpha = edgeAlpha),
                        start = start,
                        end = end,
                        strokeWidth = 1.5f,
                    )
                }

                // Draw nodes
                graph.nodes.forEach { node ->
                    val pos = animatedPositions[node.itemId] ?: node.position
                    val isSelected = node.itemId == selectedNodeId

                    // Pulse ring for selected node
                    if (isSelected) {
                        drawCircle(
                            color = tertiaryColor.copy(alpha = pulseAlpha),
                            radius = NodeRadius * pulseScale,
                            center = pos,
                        )
                    }

                    // Dim non-selected nodes when one is selected
                    val dimmed = selectedNodeId != null && !isSelected
                    val nodeColor = when {
                        isSelected -> tertiaryColor
                        dimmed -> primaryColor.copy(alpha = 0.45f)
                        else -> primaryColor
                    }
                    val nodeTextColor = when {
                        isSelected -> onTertiary
                        dimmed -> onPrimary.copy(alpha = 0.6f)
                        else -> onPrimary
                    }

                    drawCircle(
                        color = nodeColor,
                        radius = NodeRadius,
                        center = pos,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = if (dimmed) 0.08f else 0.15f),
                        radius = NodeRadius * 0.6f,
                        center = pos,
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = node.label,
                        topLeft = Offset(
                            x = pos.x - 20f,
                            y = pos.y - 10f,
                        ),
                        style = androidx.compose.ui.text.TextStyle(
                            color = nodeTextColor,
                            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                        ),
                    )
                }
            }
        }

        // Bottom info panel with slide-up animation
        AnimatedVisibility(
            visible = selectedNodeId != null,
            enter = fadeIn(tween(DailyLifeDuration.SHORT, easing = DailyLifeEasing.Enter))
                    + slideInVertically(tween(DailyLifeDuration.MEDIUM, easing = DailyLifeEasing.Enter)) { it / 3 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            selectedNodeId?.let { id ->
                val item = items.firstOrNull { it.id == id }
                item?.let {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                RoundedCornerShape(16.dp),
                            )
                            .padding(16.dp),
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                        if (it.tags.isNotEmpty()) {
                            Text(
                                text = it.tags.joinToString(", ") { tag -> "#$tag" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class GraphNode(
    val itemId: Long,
    val label: String,
    val position: Offset,
)

private data class GraphEdge(
    val from: Long,
    val to: Long,
)

private data class TagGraph(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>,
)

private const val NodeRadius = 28f

private fun buildTagGraph(items: List<LifeItem>): TagGraph {
    if (items.isEmpty()) return TagGraph(emptyList(), emptyList())

    val taggedItems = items.filter { it.tags.isNotEmpty() }
    if (taggedItems.size < 2) {
        return TagGraph(
            nodes = taggedItems.mapIndexed { index, item ->
                GraphNode(
                    itemId = item.id,
                    label = item.title.take(3),
                    position = Offset(400f, 300f + index * 120f),
                )
            },
            edges = emptyList(),
        )
    }

    val tagToItems = mutableMapOf<String, MutableList<Long>>()
    taggedItems.forEach { item ->
        item.tags.forEach { tag ->
            tagToItems.getOrPut(tag) { mutableListOf() }.add(item.id)
        }
    }

    val edges = mutableSetOf<Pair<Long, Long>>()
    tagToItems.values.forEach { itemIds ->
        for (i in itemIds.indices) {
            for (j in i + 1 until itemIds.size) {
                val a = itemIds[i]
                val b = itemIds[j]
                if (a != b) {
                    edges.add(if (a < b) a to b else b to a)
                }
            }
        }
    }

    val nodeCount = taggedItems.size
    val angleStep = 2 * Math.PI / nodeCount
    val radius = 300f
    val center = Offset(400f, 500f)

    val nodes = taggedItems.mapIndexed { index, item ->
        val angle = index * angleStep
        GraphNode(
            itemId = item.id,
            label = item.title.take(3),
            position = Offset(
                x = center.x + (radius * cos(angle)).toFloat(),
                y = center.y + (radius * sin(angle)).toFloat(),
            ),
        )
    }

    return TagGraph(
        nodes = nodes,
        edges = edges.map { GraphEdge(it.first, it.second) },
    )
}
