package com.raulshma.dailylife.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color

object DynamicColorGenerator {

    fun generateSuggestions(
        context: Context,
        extracted: ExtractedWallpaperColors,
        isDark: Boolean,
    ): List<PaletteSuggestion> {
        val suggestions = mutableListOf<PaletteSuggestion>()

        // System dynamic colors (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val systemLight = dynamicLightColorScheme(context)
            val systemDark = dynamicDarkColorScheme(context)
            suggestions.add(
                PaletteSuggestion(
                    source = PaletteSource.WallpaperDominant,
                    name = "System",
                    seedColor = systemLight.primary,
                    lightScheme = systemLight,
                    darkScheme = systemDark,
                )
            )
        }

        // Wallpaper extracted variants
        extracted.vibrant?.let {
            suggestions.add(createSuggestion(PaletteSource.WallpaperVibrant, "Vibrant", it, isDark))
        }
        extracted.muted?.let {
            suggestions.add(createSuggestion(PaletteSource.WallpaperMuted, "Muted", it, isDark))
        }
        extracted.lightVibrant?.let {
            suggestions.add(createSuggestion(PaletteSource.WallpaperLight, "Light", it, isDark))
        }
        extracted.darkVibrant?.let {
            suggestions.add(createSuggestion(PaletteSource.WallpaperDark, "Dark", it, isDark))
        }
        extracted.dominant?.let {
            if (it != extracted.vibrant && it != extracted.muted) {
                suggestions.add(createSuggestion(PaletteSource.WallpaperDominant, "Dominant", it, isDark))
            }
        }

        // Hue-shifted variants from the dominant/vibrant color
        val baseSeed = extracted.vibrant
            ?: extracted.dominant
            ?: extracted.muted
            ?: return suggestions

        suggestions.add(createSuggestion(PaletteSource.Warm, "Warm", TonalPaletteGenerator.shiftHue(baseSeed, 30f), isDark))
        suggestions.add(createSuggestion(PaletteSource.Cool, "Cool", TonalPaletteGenerator.shiftHue(baseSeed, -30f), isDark))
        suggestions.add(createSuggestion(PaletteSource.Complementary, "Complementary", TonalPaletteGenerator.shiftHue(baseSeed, 180f), isDark))

        // Preset color family variants (like launchers do)
        suggestions.add(createSuggestion(PaletteSource.Spritz, "Spritz", Color(0xFF4285F4), isDark))
        suggestions.add(createVariantSuggestion(PaletteSource.TonalSpot, "Tonal Spot", baseSeed, isDark, PaletteVariant.TONAL_SPOT))
        suggestions.add(createVariantSuggestion(PaletteSource.Expressive, "Expressive", baseSeed, isDark, PaletteVariant.EXPRESSIVE))

        return suggestions.distinctBy { it.name }
    }

    fun generateFromSeed(seedColor: Color, isDark: Boolean): Pair<ColorScheme, ColorScheme> {
        return TonalPaletteGenerator.generateFromSeed(seedColor, isDark)
    }

    private fun createSuggestion(
        source: PaletteSource,
        name: String,
        seed: Color,
        isDark: Boolean,
    ): PaletteSuggestion {
        val (light, dark) = TonalPaletteGenerator.generateFromSeed(seed, isDark)
        return PaletteSuggestion(
            source = source,
            name = name,
            seedColor = seed,
            lightScheme = light,
            darkScheme = dark,
        )
    }

    private fun createVariantSuggestion(
        source: PaletteSource,
        name: String,
        seed: Color,
        isDark: Boolean,
        variant: PaletteVariant,
    ): PaletteSuggestion {
        val (light, dark) = TonalPaletteGenerator.generateWithVariant(seed, variant, isDark)
        return PaletteSuggestion(
            source = source,
            name = name,
            seedColor = seed,
            lightScheme = light,
            darkScheme = dark,
        )
    }
}
