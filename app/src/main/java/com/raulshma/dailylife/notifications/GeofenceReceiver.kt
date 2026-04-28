package com.raulshma.dailylife.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.raulshma.dailylife.MainActivity
import com.raulshma.dailylife.R

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) return

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            val transitionLabel = when (transitionType) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> "Arrived at"
                Geofence.GEOFENCE_TRANSITION_EXIT -> "Left"
                else -> "Near"
            }

            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, "daily_life_reminders")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("$transitionLabel location")
                .setContentText("Location reminder triggered")
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(
                geofence.requestId.hashCode().coerceAtLeast(1),
                notification,
            )
        }
    }
}
