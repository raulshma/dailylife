package com.raulshma.dailylife.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedPoint by remember {
        mutableStateOf(
            initialLatitude?.let { lat ->
                initialLongitude?.let { lon -> GeoPoint(lat, lon) }
            } ?: GeoPoint(51.5074, -0.1278),
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Pick a location",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
            Text(
                text = "Tap on the map to select a location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 8.dp),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            isTilesScaledToDpi = true
                            minZoomLevel = 2.0
                            maxZoomLevel = 19.5
                            controller.setZoom(14.0)
                            controller.setCenter(selectedPoint)

                            val marker = Marker(this).apply {
                                position = selectedPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected"
                            }
                            overlays.add(marker)

                            overlays.add(
                                MapEventsOverlay(
                                    object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                            p?.let {
                                                selectedPoint = it
                                                marker.position = it
                                                invalidate()
                                            }
                                            return true
                                        }

                                        override fun longPressHelper(p: GeoPoint?): Boolean {
                                            return false
                                        }
                                    },
                                ),
                            )
                        }
                    },
                    update = { mapView ->
                        mapView.controller.setCenter(selectedPoint)
                        val marker = mapView.overlays.filterIsInstance<Marker>().firstOrNull()
                        marker?.position = selectedPoint
                        mapView.invalidate()
                    },
                )
            }

            Text(
                text = "Lat: %.5f, Lon: %.5f".format(selectedPoint.latitude, selectedPoint.longitude),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onLocationSelected(selectedPoint.latitude, selectedPoint.longitude)
                        onDismiss()
                    },
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Select")
                }
            }
        }
    }
}
