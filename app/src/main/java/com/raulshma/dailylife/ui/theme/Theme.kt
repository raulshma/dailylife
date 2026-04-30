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
    primary = Color(0xFF2F5D8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3F7),
    onPrimaryContainer = Color(0xFF102E4A),
    secondary = Color(0xFF4F6F52),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E7D0),
    onSecondaryContainer = Color(0xFF19351C),
    tertiary = Color(0xFF9A5B36),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC7),
    onTertiaryContainer = Color(0xFF3B1D0C),
    background = Color(0xFFFAF9F6),
    onBackground = Color(0xFF1D1C1A),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1D1C1A),
    surfaceVariant = Color(0xFFE4E2DC),
    onSurfaceVariant = Color(0xFF30312B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CCBFF),
    onPrimary = Color(0xFF073258),
    primaryContainer = Color(0xFF214A72),
    onPrimaryContainer = Color(0xFFD4E3F7),
    secondary = Color(0xFFA9D3AE),
    onSecondary = Color(0xFF18351D),
    secondaryContainer = Color(0xFF35513A),
    onSecondaryContainer = Color(0xFFD2E7D0),
    tertiary = Color(0xFFFFB68B),
    onTertiary = Color(0xFF552A0E),
    tertiaryContainer = Color(0xFF783F22),
    onTertiaryContainer = Color(0xFFFFDCC7),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE3E2DD),
    surface = Color(0xFF171A1F),
    onSurface = Color(0xFFE3E2DD),
    surfaceVariant = Color(0xFF42464D),
    onSurfaceVariant = Color(0xFFD2D3DB),
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
