package com.raulshma.dailylife.ui.photos

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.domain.LifeItemType
import com.raulshma.dailylife.domain.inferAudioUrl
import com.raulshma.dailylife.domain.inferImagePreviewUrl
import com.raulshma.dailylife.ui.components.inferLocationPreview
import com.raulshma.dailylife.domain.inferPdfUrl
import com.raulshma.dailylife.domain.inferVideoPlaybackUrl
import com.raulshma.dailylife.ui.LocalSharedTransitionScope
import com.raulshma.dailylife.ui.LocalAnimatedVisibilityScope
import com.raulshma.dailylife.ui.components.PressableCard
import com.raulshma.dailylife.ui.components.SharedElementKeys
import com.raulshma.dailylife.ui.components.TypeBadge

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun MediaMosaicTile(
    item: LifeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val mediaSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.media(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }
    val titleSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.title(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            ).skipToLookaheadSize()
        }
    } else {
        Modifier
    }
    val badgeSharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = SharedElementKeys.typeBadge(item.id)),
                animatedVisibilityScope = animatedVisibilityScope,
            ).skipToLookaheadSize()
        }
    } else {
        Modifier
    }

    PressableCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(item.inferMosaicHeight()),
        shape = RectangleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.then(mediaSharedModifier)) {
                ItemPreview(item = item)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.65f),
                            ),
                        ),
                    )
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TypeBadge(type = item.type, modifier = badgeSharedModifier, boxSize = 28.dp)
                        Text(
                            text = item.title,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = titleSharedModifier,
                        )
                    }
                }
            }
        }
    }
}

internal fun LifeItem.inferMosaicHeight(): androidx.compose.ui.unit.Dp {
    val bucket = ((id % 7L) + 7L) % 7L
    return when (type) {
        LifeItemType.Photo -> if (bucket % 3L == 0L) 222.dp else 164.dp
        LifeItemType.Video -> if (bucket % 2L == 0L) 214.dp else 168.dp
        LifeItemType.Location -> 198.dp
        LifeItemType.Audio -> 156.dp
        LifeItemType.Pdf -> 190.dp
        LifeItemType.Mixed -> if (bucket % 2L == 0L) 228.dp else 172.dp
        else -> {
            when {
                inferImagePreviewUrl() != null -> if (bucket % 3L == 0L) 222.dp else 164.dp
                inferVideoPlaybackUrl() != null -> if (bucket % 2L == 0L) 214.dp else 168.dp
                inferAudioUrl() != null -> 156.dp
                inferPdfUrl() != null -> 190.dp
                inferLocationPreview() != null -> 198.dp
                body.length > 120 -> 198.dp
                else -> 152.dp
            }
        }
    }
}
