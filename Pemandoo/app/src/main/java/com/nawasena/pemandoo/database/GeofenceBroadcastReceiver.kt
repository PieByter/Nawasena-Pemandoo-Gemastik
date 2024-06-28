package com.nawasena.pemandoo.database

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.nawasena.pemandoo.R
import com.nawasena.pemandoo.api.ApiConfig
import com.nawasena.pemandoo.api.LandmarkResponseItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                    val geofenceId = geofence.requestId
                    fetchLandmarkDetails(geofenceId, geofenceTransition, context)
                }
            }
        }
    }

    private fun fetchLandmarkDetails(geofenceId: String, transitionType: Int, context: Context) {
        val apiService = ApiConfig.apiService
        val call = apiService.getLandmarkById(geofenceId)

        call.enqueue(object : Callback<LandmarkResponseItem> {
            override fun onResponse(
                call: Call<LandmarkResponseItem>,
                response: Response<LandmarkResponseItem>
            ) {
                if (response.isSuccessful) {
                    val landmark = response.body()
                    if (landmark != null) {
                        saveLandmarkToSharedPreferences(context, landmark)
                        sendNotification(context, "Entered: ${landmark.name}")
                    }
                } else {
                    sendNotification(context, "Failed to fetch landmark details")
                }
            }

            override fun onFailure(call: Call<LandmarkResponseItem>, t: Throwable) {
                sendNotification(context, "Error: ${t.message}")
            }
        })
    }

    private fun saveLandmarkToSharedPreferences(context: Context, landmark: LandmarkResponseItem) {
        val sharedPreferences = context.getSharedPreferences("LandmarkPrefs", Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {
            putString("landmarkId", landmark.id)
            putString("landmarkName", landmark.name)
            apply()
        }
    }

    private fun sendNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Geofence Notification")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
