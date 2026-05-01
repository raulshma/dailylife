package com.raulshma.dailylife.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.raulshma.dailylife.domain.LifeItem
import com.raulshma.dailylife.ui.capture.CartoDark
import com.raulshma.dailylife.ui.capture.CartoLight
import com.raulshma.dailylife.ui.capture.EsriSatellite
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val GeoPattern =
    Regex("""geo:\s*([-+]?\d{1,2}(?:\.\d+)?),\s*([-+]?\d{1,3}(?:\.\d+)?)(?:\?[^\s]*)?""", RegexOption.IGNORE_CASE)
private val LatLonPattern =
    Regex("""([-+]?\d{1,2}(?:\.\d+)?)\s*[, ]\s*([-+]?\d{1,3}(?:\.\d+)?)""")
private val OsmMlatPattern =
    Regex("""[?&]mlat=([-+]?\d{1,2}(?:\.\d+)?).*?[?&]mlon=([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
private val MapTilePattern =
    Regex("""[?&]mapTile=([^&\s]+)""", RegexOption.IGNORE_CASE)

internal fun LifeItem.inferLocationPreview(): Pair<Double, Double>? {
    val source = listOf(title, body).joinToString(" ")

    val geoMatch = GeoPattern.find(source)
    if (geoMatch != null) {
        return geoMatch.groupValues[1].toDoubleOrNull()
            ?.let { lat ->
                geoMatch.groupValues[2].toDoubleOrNull()?.let { lon -> lat to lon }
            }
            ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
    }

    val osmMatch = OsmMlatPattern.find(source)
    if (osmMatch != null) {
        return osmMatch.groupValues[1].toDoubleOrNull()
            ?.let { lat ->
                osmMatch.groupValues[2].toDoubleOrNull()?.let { lon -> lat to lon }
            }
            ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
    }

    return LatLonPattern.find(source)
        ?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull() ?: return@let null
            val lon = match.groupValues[2].toDoubleOrNull() ?: return@let null
            lat to lon
        }
        ?.takeIf { (lat, lon) -> lat in -90.0..90.0 && lon in -180.0..180.0 }
}

internal fun LifeItem.inferLocationMapTile(): String? {
    val source = listOf(title, body).joinToString(" ")
    return MapTilePattern.find(source)?.groupValues?.get(1)
}

@Composable
internal fun OpenStreetMapPreview(
    latitude: Double,
    longitude: Double,
    mapTile: String? = null,
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {},
) {
    val isDarkTheme = isSystemInDarkTheme()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).also { mapView ->
                onMapReady(mapView)
                val tileSource = when (mapTile) {
                    "Satellite" -> EsriSatellite
                    "OSM" -> TileSourceFactory.MAPNIK
                    "Auto" -> if (isDarkTheme) CartoDark else CartoLight
                    else -> TileSourceFactory.MAPNIK
                }
                mapView.setTileSource(tileSource)
                mapView.setMultiTouchControls(false)
                mapView.isTilesScaledToDpi = true
                mapView.minZoomLevel = 2.0
                mapView.maxZoomLevel = 19.5
                mapView.setBuiltInZoomControls(false)

                val point = GeoPoint(latitude, longitude)
                mapView.controller.setZoom(14.5)
                mapView.controller.setCenter(point)
                mapView.overlays.removeAll { overlay -> overlay is Marker }
                mapView.overlays.add(
                    Marker(mapView).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = null
                        title = "Saved location"
                    },
                )
                mapView.invalidate()
            }
        },
        update = { mapView ->
            val newSource = when (mapTile) {
                "Satellite" -> EsriSatellite
                "OSM" -> TileSourceFactory.MAPNIK
                "Auto" -> if (isDarkTheme) CartoDark else CartoLight
                else -> TileSourceFactory.MAPNIK
            }
            if (mapView.tileProvider.tileSource != newSource) {
                mapView.setTileSource(newSource)
            }
        },
    )
}
