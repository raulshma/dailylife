package com.raulshma.dailylife.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import com.raulshma.dailylife.ui.components.ShimmerBox
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.isSystemInDarkTheme

internal val CartoLight = object : OnlineTileSourceBase(
    "CartoDB Positron", 1, 20, 256, ".png",
    arrayOf("https://a.basemaps.cartocdn.com/light_all/"), "© OpenStreetMap contributors, © CARTO"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}

internal val CartoDark = object : OnlineTileSourceBase(
    "CartoDB Dark Matter", 1, 20, 256, ".png",
    arrayOf("https://a.basemaps.cartocdn.com/dark_all/"), "© OpenStreetMap contributors, © CARTO"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}

internal val EsriSatellite = object : OnlineTileSourceBase(
    "Esri World Imagery", 1, 19, 256, "",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"), "Tiles © Esri"
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    initialTile: String? = null,
    onLocationSelected: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("dailylife_prefs", Context.MODE_PRIVATE)
    var selectedPoint by remember {
        mutableStateOf(
            initialLatitude?.let { lat ->
                initialLongitude?.let { lon -> GeoPoint(lat, lon) }
            } ?: GeoPoint(51.5074, -0.1278),
        )
    }

    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedTile by remember {
        mutableStateOf(
            initialTile ?: prefs.getString("last_map_tile", null) ?: "Auto"
        )
    }
    val isDarkTheme = isSystemInDarkTheme()
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    fun jumpToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            val myLoc = myLocationOverlay?.myLocation
            if (myLoc != null) {
                selectedPoint = GeoPoint(myLoc.latitude, myLoc.longitude)
                return
            }
            val lastFix = myLocationOverlay?.lastFix
            if (lastFix != null) {
                selectedPoint = GeoPoint(lastFix.latitude, lastFix.longitude)
                return
            }
            
            android.widget.Toast.makeText(context, "Locating...", android.widget.Toast.LENGTH_SHORT).show()
            
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation: android.location.Location? = null
            for (provider in providers) {
                try {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy || l.time > bestLocation.time) {
                        bestLocation = l
                    }
                } catch (e: SecurityException) {
                    // Ignore
                }
            }
            bestLocation?.let {
                selectedPoint = GeoPoint(it.latitude, it.longitude)
            }
            
            try {
                val provider = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) LocationManager.NETWORK_PROVIDER else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else null
                if (provider != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        locationManager.getCurrentLocation(provider, null, context.mainExecutor) { newLoc ->
                            if (newLoc != null) selectedPoint = GeoPoint(newLoc.latitude, newLoc.longitude)
                        }
                    } else {
                        val listener = object : android.location.LocationListener {
                            override fun onLocationChanged(newLoc: android.location.Location) {
                                selectedPoint = GeoPoint(newLoc.latitude, newLoc.longitude)
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                        }
                        @Suppress("DEPRECATION")
                        locationManager.requestSingleUpdate(provider, listener, android.os.Looper.getMainLooper())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            jumpToCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        if (initialLatitude == null || initialLongitude == null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                jumpToCurrentLocation()
            } else {
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    fun performSearch() {
        if (searchQuery.isBlank()) return
        isSearching = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context)
                val addresses = geocoder.getFromLocationName(searchQuery, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val point = GeoPoint(address.latitude, address.longitude)
                    withContext(Dispatchers.Main) {
                        selectedPoint = point
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) { isSearching = false }
            }
        }
    }

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, 
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize()
    ) {
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

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        if (isSearching) {
                            ShimmerBox(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp),
                                shape = CircleShape,
                            )
                        } else {
                            IconButton(onClick = ::performSearch) {
                                Icon(Icons.Filled.Search, "Search")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch() })
                )
                
                IconButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        jumpToCurrentLocation()
                    } else {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                }) {
                    Icon(Icons.Filled.MyLocation, "Current Location")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
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

                            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                                enableMyLocation()
                            }
                            overlays.add(myLocationOverlay)

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
                        
                        val newSource = when (selectedTile) {
                            "Auto" -> if (isDarkTheme) CartoDark else CartoLight
                            "Satellite" -> EsriSatellite
                            "OSM" -> TileSourceFactory.MAPNIK
                            else -> TileSourceFactory.MAPNIK
                        }
                        if (mapView.tileProvider.tileSource != newSource) {
                            mapView.setTileSource(newSource)
                        }
                        
                        mapView.invalidate()
                    },
                )
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tileOptions = listOf("Auto", "Satellite", "OSM")
                    tileOptions.forEach { option ->
                        FilterChip(
                            selected = selectedTile == option,
                            onClick = { selectedTile = option },
                            label = { Text(option) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
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
                        onLocationSelected(selectedPoint.latitude, selectedPoint.longitude, selectedTile)
                        prefs.edit().putString("last_map_tile", selectedTile).apply()
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
