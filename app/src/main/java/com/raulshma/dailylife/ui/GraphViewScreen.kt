package com.raulshma.dailylife.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.LifeItem
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A simple tag-based graph view that places items as nodes and draws edges
 * between items that share at least one tag.
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
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

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
                                val dx = offset.x - node.position.x
                                val dy = offset.y - node.position.y
                                sqrt(dx * dx + dy * dy) < NodeRadius * 2.5f
                            }
                            selectedNodeId = tapped?.itemId
                            tapped?.let { onItemSelected(it.itemId) }
                        }
                    },
            ) {
                // Draw edges
                graph.edges.forEach { edge ->
                    val start = graph.nodes.first { it.itemId == edge.from }.position
                    val end = graph.nodes.first { it.itemId == edge.to }.position
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.6f),
                        start = start,
                        end = end,
                        strokeWidth = 1.5f,
                    )
                }

                // Draw nodes
                graph.nodes.forEach { node ->
                    val isSelected = node.itemId == selectedNodeId
                    val color = if (isSelected) tertiaryColor else primaryColor
                    drawCircle(
                        color = color,
                        radius = NodeRadius,
                        center = node.position,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = NodeRadius * 0.6f,
                        center = node.position,
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = node.label,
                        topLeft = Offset(
                            x = node.position.x - 20f,
                            y = node.position.y - 10f,
                        ),
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                        ),
                    )
                }
            }
        }

        selectedNodeId?.let { id ->
            val item = items.firstOrNull { it.id == id }
            item?.let {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .padding(12.dp),
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
