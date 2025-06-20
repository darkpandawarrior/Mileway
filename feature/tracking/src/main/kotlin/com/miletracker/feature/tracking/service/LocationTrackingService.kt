package com.miletracker.feature.tracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.miletracker.core.data.dao.LocationDao
import com.miletracker.core.data.dao.SavedTrackDao
import com.miletracker.core.data.model.db.LocationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationTrackingService : Service() {

    private val locationDao: LocationDao by inject()
    private val savedTrackDao: SavedTrackDao by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var fusedClient: FusedLocationProviderClient
    private var activeToken: String? = null
    private var lastLocation: Location? = null
    private var totalDistanceM = 0.0

    companion object {
        const val EXTRA_TOKEN = "token"
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "LocationTrackingService"
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                activeToken = intent.getStringExtra(EXTRA_TOKEN) ?: return START_NOT_STICKY
                startForeground(NOTIFICATION_ID, buildNotification("Tracking active…"))
                startLocationUpdates()
            }
            ACTION_STOP -> stopSelfAndCleanup()
            ACTION_PAUSE -> stopLocationUpdates()
            ACTION_RESUME -> startLocationUpdates()
        }
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()
        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.w(TAG, "Location permission not granted", e)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val token = activeToken ?: return
            result.lastLocation?.let { loc -> processLocation(token, loc) }
        }
    }

    private fun processLocation(token: String, location: Location) {
        val displacement = lastLocation?.distanceTo(location)?.toDouble() ?: 0.0
        if (lastLocation != null && displacement < 5.0) return

        totalDistanceM += displacement
        lastLocation = location

        val data = LocationData(
            activity = "DRIVING", speed = location.speed,
            lat = location.latitude, lng = location.longitude,
            token = token, date = System.currentTimeMillis(),
            displacement = displacement, accuracy = location.accuracy,
            isMock = location.isFromMockProvider,
            batteryPercentage = 100.0
        )
        scope.launch {
            locationDao.insertLocation(data)
            savedTrackDao.updateTrackLiveData(token, totalDistanceM, System.currentTimeMillis())
        }
        updateNotification("${String.format("%.2f", totalDistanceM / 1000)} km tracked")
    }

    private fun stopSelfAndCleanup() {
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Location Tracking",
            NotificationManager.IMPORTANCE_LOW).apply {
            description = "Tracks your current journey"
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification =
        android.app.Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("MileTracker")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        scope.cancel()
        super.onDestroy()
    }
}
