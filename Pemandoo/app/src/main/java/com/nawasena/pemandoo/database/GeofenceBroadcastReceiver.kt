package com.nawasena.pemandoo.database

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.nawasena.pemandoo.R

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcast"
        const val ACTION_GEOFENCE_EVENT = "GeofenceEvent"
        private const val CHANNEL_ID = "1"
        private const val CHANNEL_NAME = "Geofence Channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

            // Check for errors in geofencing event
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                sendNotification(context, "Error: $errorMessage")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                triggeringGeofences?.forEach { geofence ->
                    val geofenceId = geofence.requestId.toInt()
                    val repository = LandmarkRepository(AppDatabase.getDatabase(context.applicationContext).landmarkDao())

                    repository.getLandmarkById(geofenceId).observeForever { landmark ->
                        landmark?.let {
                            val geofenceTransitionString = when (geofenceTransition) {
                                Geofence.GEOFENCE_TRANSITION_ENTER -> "${it.name}: Anda telah memasuki area"
                                Geofence.GEOFENCE_TRANSITION_DWELL -> "${it.name}: Anda telah di dalam area"
                                else -> "Invalid transition type"
                            }
                            Log.i(TAG, geofenceTransitionString)
                            sendNotification(context, geofenceTransitionString)
                        }
                    }
                }
            } else {
                val errorMessage = "Invalid transition type : $geofenceTransition"
                Log.e(TAG, errorMessage)
                sendNotification(context, errorMessage)
            }
        }
    }

    private fun sendNotification(context: Context, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Geofence Channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Anda memasuki area")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set priority for high visibility
            .setAutoCancel(true) // Automatically dismiss when tapped

        // Send the notification
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
