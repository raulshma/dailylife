package com.raulshma.dailylife.ui.theme

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.compose.ui.graphics.Color as ComposeColor

object WallpaperColorExtractor {

    fun extract(context: Context): ExtractedWallpaperColors {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val drawable = try {
            wallpaperManager.drawable
        } catch (_: Exception) {
            null
        } ?: return ExtractedWallpaperColors()

        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> drawable.toBitmap(
                width = drawable.intrinsicWidth.coerceAtMost(512),
                height = drawable.intrinsicHeight.coerceAtMost(512),
            )
        }

        return extractFromBitmap(bitmap)
    }

    fun extractFromBitmap(bitmap: Bitmap): ExtractedWallpaperColors {
        val palette = Palette.from(bitmap)
            .maximumColorCount(32)
            .generate()

        val vibrant = palette.vibrantSwatch?.rgb?.let { ComposeColor(it) }
        val muted = palette.mutedSwatch?.rgb?.let { ComposeColor(it) }
        val lightVibrant = palette.lightVibrantSwatch?.rgb?.let { ComposeColor(it) }
        val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { ComposeColor(it) }
        val dominant = palette.dominantSwatch?.rgb?.let { ComposeColor(it) }

        val allSwatches = palette.swatches
            .sortedByDescending { it.population }
            .map { ComposeColor(it.rgb) }
            .distinct()
            .take(8)

        return ExtractedWallpaperColors(
            vibrant = vibrant,
            muted = muted,
            lightVibrant = lightVibrant,
            darkVibrant = darkVibrant,
            dominant = dominant,
            allSwatches = allSwatches,
        )
    }
}
