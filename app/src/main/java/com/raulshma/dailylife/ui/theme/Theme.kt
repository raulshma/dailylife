package com.raulshma.dailylife.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF2763A6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E3FF),
    onPrimaryContainer = Color(0xFF001C3B),
    secondary = Color(0xFF476B58),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC9EBD8),
    onSecondaryContainer = Color(0xFF042116),
    tertiary = Color(0xFF8C4F65),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E5),
    onTertiaryContainer = Color(0xFF38111F),
    background = Color(0xFFFBFCFF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF424752),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF0B4A88),
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = Color(0xFFADD4BD),
    onSecondary = Color(0xFF193528),
    secondaryContainer = Color(0xFF2F4D3E),
    onSecondaryContainer = Color(0xFFC9EBD8),
    tertiary = Color(0xFFFFB0CB),
    onTertiary = Color(0xFF541D33),
    tertiaryContainer = Color(0xFF703348),
    onTertiaryContainer = Color(0xFFFFD8E5),
    background = Color(0xFF101318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF161A20),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF434752),
    onSurfaceVariant = Color(0xFFC3C6D1),
)

private const val PREFS_NAME = "dailylife_prefs"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val KEY_CUSTOM_SEED_COLOR = "custom_seed_color"
private const val KEY_CUSTOM_PALETTE_NAME = "custom_palette_name"

fun SharedPreferences.getCustomPalette(isDark: Boolean = false): PaletteSuggestion? {
    val seedColorInt = getInt(KEY_CUSTOM_SEED_COLOR, -1)
    val paletteName = getString(KEY_CUSTOM_PALETTE_NAME, null)
    if (seedColorInt == -1 || paletteName == null) return null

    val seedColor = Color(seedColorInt)
    val (light, dark) = DynamicColorGenerator.generateFromSeed(seedColor, isDark)
    return PaletteSuggestion(
        source = PaletteSource.Custom,
        name = paletteName,
        seedColor = seedColor,
        lightScheme = light,
        darkScheme = dark,
    )
}

fun SharedPreferences.Editor.saveCustomPalette(palette: PaletteSuggestion?): SharedPreferences.Editor {
    if (palette == null) {
        remove(KEY_CUSTOM_SEED_COLOR)
        remove(KEY_CUSTOM_PALETTE_NAME)
    } else {
        putInt(KEY_CUSTOM_SEED_COLOR, palette.seedColor.toArgb())
        putString(KEY_CUSTOM_PALETTE_NAME, palette.name)
    }
    return this
}

fun Context.getThemeColorScheme(darkTheme: Boolean): ColorScheme {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val useDynamic = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    val customPalette = prefs.getCustomPalette(darkTheme)

    return when {
        customPalette != null -> {
            if (darkTheme) customPalette.darkScheme else customPalette.lightScheme
        }
        useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
}

@Composable
fun DailyLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    paletteKey: Int = 0,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    // paletteKey is read to force recomposition when palette preferences change
    val prefs = remember(paletteKey) { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    val useDynamic = dynamicColor && prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    val customPalette = remember(paletteKey, darkTheme) { prefs.getCustomPalette(darkTheme) }

    val colorScheme = when {
        customPalette != null -> {
            if (darkTheme) customPalette.darkScheme else customPalette.lightScheme
        }
        useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
