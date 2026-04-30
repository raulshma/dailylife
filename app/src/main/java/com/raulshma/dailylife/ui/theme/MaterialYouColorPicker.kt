package com.raulshma.dailylife.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MaterialYouColorPicker(
    onPaletteSelected: (PaletteSuggestion?) -> Unit,
    selectedPalette: PaletteSuggestion? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var extracted by remember { mutableStateOf(ExtractedWallpaperColors()) }
    var suggestions by remember { mutableStateOf<List<PaletteSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        extracted = WallpaperColorExtractor.extract(context)
        suggestions = DynamicColorGenerator.generateSuggestions(context, extracted, isDark)
        isLoading = false
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Extracted wallpaper palette section
        if (extracted.allSwatches.isNotEmpty()) {
            Text(
                text = "Wallpaper palette",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                items(extracted.allSwatches) { color ->
                    WallpaperColorSwatch(color = color)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Suggested palettes
        Text(
            text = "Color suggestions",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (isLoading) {
            Text(
                text = "Analyzing wallpaper...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (suggestions.isEmpty()) {
            Text(
                text = "No colors extracted. Try changing your wallpaper.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                items(suggestions, key = { it.name }) { suggestion ->
                    PaletteSuggestionCard(
                        suggestion = suggestion,
                        isSelected = selectedPalette?.name == suggestion.name,
                        isDark = isDark,
                        onClick = { onPaletteSelected(suggestion) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    )
}

@Composable
private fun PaletteSuggestionCard(
    suggestion: PaletteSuggestion,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = if (isDark) suggestion.darkScheme else suggestion.lightScheme
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .width(120.dp)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Seed color swatch
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(suggestion.seedColor)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )

        // Mini palette preview
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MiniColorDot(color = scheme.primary)
            MiniColorDot(color = scheme.secondary)
            MiniColorDot(color = scheme.tertiary)
            MiniColorDot(color = scheme.surface)
        }

        Text(
            text = suggestion.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun MiniColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
    )
}

@Composable
fun PalettePreviewStrip(
    scheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        val colors = listOf(
            scheme.primary to scheme.onPrimary,
            scheme.secondary to scheme.onSecondary,
            scheme.tertiary to scheme.onTertiary,
            scheme.error to scheme.onError,
            scheme.surface to scheme.onSurface,
        )
        colors.forEach { (bg, fg) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(fg),
                )
            }
        }
    }
}
