package com.raulshma.dailylife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun TimelineSkeletonItem(modifier: Modifier = Modifier) {
    PressableCard(
        onClick = { },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                )
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                }
                ShimmerBox(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                )
                ShimmerBox(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                )
            }

            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
            )

            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp),
                shape = RoundedCornerShape(4.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(52.dp)
                            .height(18.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonMosaicTile(
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )
    }
}

@Composable
fun SkeletonSnapshotPill(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )
    }
}
