package com.raulshma.dailylife.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

enum class PaletteSource {
    WallpaperVibrant,
    WallpaperMuted,
    WallpaperLight,
    WallpaperDark,
    WallpaperDominant,
    Warm,
    Cool,
    Complementary,
    Spritz,
    TonalSpot,
    Expressive,
    Custom,
}

data class PaletteSuggestion(
    val source: PaletteSource,
    val name: String,
    val seedColor: Color,
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
)

data class ExtractedWallpaperColors(
    val vibrant: Color? = null,
    val muted: Color? = null,
    val lightVibrant: Color? = null,
    val darkVibrant: Color? = null,
    val dominant: Color? = null,
    val allSwatches: List<Color> = emptyList(),
)
