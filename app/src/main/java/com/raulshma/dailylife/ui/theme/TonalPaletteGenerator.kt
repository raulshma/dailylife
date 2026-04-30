package com.raulshma.dailylife.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TonalPaletteGenerator {

    fun generateFromSeed(seedColor: Color, isDark: Boolean): Pair<ColorScheme, ColorScheme> {
        val light = generateLightScheme(seedColor)
        val dark = generateDarkScheme(seedColor)
        return light to dark
    }

    fun generateWithVariant(
        seedColor: Color,
        variant: PaletteVariant,
        isDark: Boolean,
    ): Pair<ColorScheme, ColorScheme> {
        val adjustedSeed = when (variant) {
            PaletteVariant.CONTENT -> seedColor
            PaletteVariant.TONAL_SPOT -> seedColor.withSaturation(seedColor.saturation() * 0.85f)
            PaletteVariant.EXPRESSIVE -> {
                // More hue separation for expressive
                seedColor
            }
        }
        val light = generateLightScheme(adjustedSeed, variant)
        val dark = generateDarkScheme(adjustedSeed, variant)
        return light to dark
    }

    private fun generateLightScheme(seed: Color, variant: PaletteVariant = PaletteVariant.CONTENT): ColorScheme {
        val hsl = seed.toHsl()
        val hue = hsl[0]
        val sat = hsl[1]
        val light = hsl[2]

        val secondaryHue = when (variant) {
            PaletteVariant.CONTENT -> hue + 30f
            PaletteVariant.TONAL_SPOT -> hue + 15f
            PaletteVariant.EXPRESSIVE -> hue + 60f
        }
        val tertiaryHue = when (variant) {
            PaletteVariant.CONTENT -> hue - 30f
            PaletteVariant.TONAL_SPOT -> hue - 15f
            PaletteVariant.EXPRESSIVE -> hue - 60f
        }

        val secondarySat = when (variant) {
            PaletteVariant.CONTENT -> sat * 0.75f
            PaletteVariant.TONAL_SPOT -> sat * 0.6f
            PaletteVariant.EXPRESSIVE -> sat * 0.9f
        }
        val tertiarySat = when (variant) {
            PaletteVariant.CONTENT -> sat * 0.7f
            PaletteVariant.TONAL_SPOT -> sat * 0.55f
            PaletteVariant.EXPRESSIVE -> sat * 0.85f
        }

        val primary = seed
        val primaryContainer = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.90f)
        val onPrimaryContainer = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.10f)
        val onPrimary = if (light > 0.55f) Color.Black else Color.White

        val secondary = fromHsl(secondaryHue, secondarySat.coerceIn(0.2f, 0.8f), 0.40f)
        val secondaryContainer = fromHsl(secondaryHue, secondarySat.coerceIn(0.15f, 0.6f), 0.90f)
        val onSecondaryContainer = fromHsl(secondaryHue, secondarySat.coerceIn(0.15f, 0.6f), 0.10f)
        val onSecondary = Color.White

        val tertiary = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.2f, 0.8f), 0.40f)
        val tertiaryContainer = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.15f, 0.6f), 0.90f)
        val onTertiaryContainer = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.15f, 0.6f), 0.10f)
        val onTertiary = Color.White

        val error = Color(0xFFB3261E)
        val errorContainer = Color(0xFFF9DEDC)
        val onError = Color.White
        val onErrorContainer = Color(0xFF410E0B)

        val background = fromHsl(hue, sat * 0.08f, 0.99f)
        val onBackground = fromHsl(hue, sat * 0.15f, 0.10f)
        val surface = fromHsl(hue, sat * 0.06f, 0.99f)
        val onSurface = fromHsl(hue, sat * 0.15f, 0.10f)
        val surfaceVariant = fromHsl(hue, sat * 0.10f, 0.90f)
        val onSurfaceVariant = fromHsl(hue, sat * 0.12f, 0.30f)
        val outline = fromHsl(hue, sat * 0.10f, 0.50f)
        val outlineVariant = fromHsl(hue, sat * 0.08f, 0.80f)
        val scrim = Color.Black
        val inverseSurface = fromHsl(hue, sat * 0.10f, 0.20f)
        val inverseOnSurface = fromHsl(hue, sat * 0.05f, 0.95f)
        val inversePrimary = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.80f)
        val surfaceTint = primary

        return lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceTint = surfaceTint,
        )
    }

    private fun generateDarkScheme(seed: Color, variant: PaletteVariant = PaletteVariant.CONTENT): ColorScheme {
        val hsl = seed.toHsl()
        val hue = hsl[0]
        val sat = hsl[1]
        val light = hsl[2]

        val secondaryHue = when (variant) {
            PaletteVariant.CONTENT -> hue + 30f
            PaletteVariant.TONAL_SPOT -> hue + 15f
            PaletteVariant.EXPRESSIVE -> hue + 60f
        }
        val tertiaryHue = when (variant) {
            PaletteVariant.CONTENT -> hue - 30f
            PaletteVariant.TONAL_SPOT -> hue - 15f
            PaletteVariant.EXPRESSIVE -> hue - 60f
        }

        val secondarySat = when (variant) {
            PaletteVariant.CONTENT -> sat * 0.75f
            PaletteVariant.TONAL_SPOT -> sat * 0.6f
            PaletteVariant.EXPRESSIVE -> sat * 0.9f
        }
        val tertiarySat = when (variant) {
            PaletteVariant.CONTENT -> sat * 0.7f
            PaletteVariant.TONAL_SPOT -> sat * 0.55f
            PaletteVariant.EXPRESSIVE -> sat * 0.85f
        }

        val primary = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.80f)
        val primaryContainer = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.30f)
        val onPrimaryContainer = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.90f)
        val onPrimary = Color(0xFF003258)

        val secondary = fromHsl(secondaryHue, secondarySat.coerceIn(0.2f, 0.8f), 0.80f)
        val secondaryContainer = fromHsl(secondaryHue, secondarySat.coerceIn(0.15f, 0.6f), 0.30f)
        val onSecondaryContainer = fromHsl(secondaryHue, secondarySat.coerceIn(0.15f, 0.6f), 0.90f)
        val onSecondary = Color(0xFF1D3525)

        val tertiary = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.2f, 0.8f), 0.80f)
        val tertiaryContainer = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.15f, 0.6f), 0.30f)
        val onTertiaryContainer = fromHsl(tertiaryHue, tertiarySat.coerceIn(0.15f, 0.6f), 0.90f)
        val onTertiary = Color(0xFF3B1D0C)

        val error = Color(0xFFF2B8B5)
        val errorContainer = Color(0xFF8C1D18)
        val onError = Color(0xFF601410)
        val onErrorContainer = Color(0xFFF9DEDC)

        val background = fromHsl(hue, sat * 0.06f, 0.10f)
        val onBackground = fromHsl(hue, sat * 0.08f, 0.90f)
        val surface = fromHsl(hue, sat * 0.05f, 0.10f)
        val onSurface = fromHsl(hue, sat * 0.08f, 0.90f)
        val surfaceVariant = fromHsl(hue, sat * 0.08f, 0.30f)
        val onSurfaceVariant = fromHsl(hue, sat * 0.06f, 0.80f)
        val outline = fromHsl(hue, sat * 0.06f, 0.60f)
        val outlineVariant = fromHsl(hue, sat * 0.04f, 0.30f)
        val scrim = Color.Black
        val inverseSurface = fromHsl(hue, sat * 0.08f, 0.90f)
        val inverseOnSurface = fromHsl(hue, sat * 0.06f, 0.20f)
        val inversePrimary = fromHsl(hue, sat.coerceAtLeast(0.3f), 0.40f)
        val surfaceTint = primary

        return darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceTint = surfaceTint,
        )
    }

    private fun Color.toHsl(): FloatArray {
        val hsv = FloatArray(3)
        AndroidColor.RGBToHSV(
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt(),
            hsv,
        )
        // HSV to HSL approximation
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val l = (max + min) / 2f
        val s = if (max == min) {
            0f
        } else {
            val d = max - min
            if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        }
        return floatArrayOf(hsv[0], s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
    }

    private fun fromHsl(hue: Float, saturation: Float, lightness: Float): Color {
        val h = ((hue % 360f + 360f) % 360f)
        val s = saturation.coerceIn(0f, 1f)
        val l = lightness.coerceIn(0f, 1f)

        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = (r1 + m).coerceIn(0f, 1f),
            green = (g1 + m).coerceIn(0f, 1f),
            blue = (b1 + m).coerceIn(0f, 1f),
        )
    }

    private fun Color.saturation(): Float {
        return toHsl()[1]
    }

    private fun Color.withSaturation(newSat: Float): Color {
        val hsl = toHsl()
        return fromHsl(hsl[0], newSat.coerceIn(0f, 1f), hsl[2])
    }

    fun shiftHue(color: Color, degrees: Float): Color {
        val hsl = color.toHsl()
        return fromHsl(hsl[0] + degrees, hsl[1], hsl[2])
    }
}

enum class PaletteVariant {
    CONTENT,
    TONAL_SPOT,
    EXPRESSIVE,
}
