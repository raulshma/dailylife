package com.raulshma.dailylife.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.raulshma.dailylife.domain.GeofenceTrigger
import com.raulshma.dailylife.domain.LifeItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    private val context: android.app.Application,
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    fun syncGeofences(items: List<LifeItem>) {
        val itemsWithGeofence = items.filter {
            it.notificationSettings.geofenceLatitude != null &&
                it.notificationSettings.geofenceLongitude != null &&
                it.notificationSettings.enabled
        }

        if (itemsWithGeofence.isEmpty()) {
            geofencingClient.removeGeofences(getPendingIntent())
            return
        }

        val geofences = itemsWithGeofence.mapNotNull { item ->
            buildGeofence(item)
        }

        if (geofences.isEmpty()) return

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofences(geofences)
            .build()

        if (!hasLocationPermission()) return

        geofencingClient.removeGeofences(getPendingIntent()).addOnCompleteListener {
            runCatching {
                geofencingClient.addGeofences(
                    geofencingRequest,
                    getPendingIntent(),
                )
            }
        }
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(getPendingIntent())
    }

    private fun buildGeofence(item: LifeItem): Geofence? {
        val lat = item.notificationSettings.geofenceLatitude ?: return null
        val lon = item.notificationSettings.geofenceLongitude ?: return null

        val transitionTypes = when (item.notificationSettings.geofenceTrigger) {
            GeofenceTrigger.Arrival -> Geofence.GEOFENCE_TRANSITION_ENTER
            GeofenceTrigger.Departure -> Geofence.GEOFENCE_TRANSITION_EXIT
            GeofenceTrigger.Both -> Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        }

        return Geofence.Builder()
            .setRequestId(item.id.toString())
            .setCircularRegion(lat, lon, item.notificationSettings.geofenceRadiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(0)
            .build()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED)
    }
}
