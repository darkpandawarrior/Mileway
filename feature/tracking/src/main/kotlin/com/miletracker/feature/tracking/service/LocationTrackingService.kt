package com.miletracker.feature.tracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.miletracker.core.data.dao.HardwareEventDao
import com.miletracker.core.data.dao.LocationDao
import com.miletracker.core.data.dao.SavedTrackDao
import com.miletracker.core.data.model.db.EventAudience
import com.miletracker.core.data.model.db.EventType
import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.session.CurrentTrackDataStore
import com.miletracker.feature.tracking.service.location.FusedLocationSource
import com.miletracker.feature.tracking.service.location.GpsFix
import com.miletracker.feature.tracking.service.location.LocationProcessor
import com.miletracker.feature.tracking.service.location.LocationSource
import com.miletracker.feature.tracking.service.location.ProcessResult
import com.miletracker.feature.tracking.service.location.SimulatedLocationSource
import com.miletracker.feature.tracking.service.location.TrackingSensorMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Advanced foreground location-tracking service.
 *
 * Pulls fixes from a [LocationSource] (real fused GPS in production, a simulated route in the
 * offline demo), captures IMU context via [TrackingSensorMonitor], and runs each fix through
 * [LocationProcessor] for Haversine distance accumulation, jitter suppression, and mock/spike
 * detection. Persists points to Room (`locations`), keeps the `saved_tracks` row + the
 * `current_track_session` DataStore live for the UI, and logs diagnostic [HardwareEvent]s.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationTrackingService : Service() {

    private val locationDao: LocationDao by inject()
    private val savedTrackDao: SavedTrackDao by inject()
    private val hardwareEventDao: HardwareEventDao by inject()
    private val sessionStore: CurrentTrackDataStore by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Serialize DB writes so live-stat updates apply in order.
    private val dbDispatcher = Dispatchers.IO.limitedParallelism(1)

    private lateinit var sensorMonitor: TrackingSensorMonitor
    private var source: LocationSource? = null
    private var processor: LocationProcessor? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var activeToken: String? = null
    private var startTime: Long = 0L
    private var isPaused: Boolean = false
    private var startCoordsWritten = false

    companion object {
        const val EXTRA_TOKEN = "token"
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1001

        /** Demo flag: drive the pipeline from a simulated route (no GPS hardware required). */
        const val SIMULATE_LOCATION = true
    }

    override fun onCreate() {
        super.onCreate()
        sensorMonitor = TrackingSensorMonitor(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val token = intent.getStringExtra(EXTRA_TOKEN) ?: return START_NOT_STICKY
                startTracking(token)
            }
            ACTION_PAUSE -> setPaused(true)
            ACTION_RESUME -> setPaused(false)
            ACTION_STOP -> stopAndFinalize()
        }
        return START_STICKY
    }

    private fun startTracking(token: String) {
        activeToken = token
        startTime = System.currentTimeMillis()
        isPaused = false
        startCoordsWritten = false
        processor = LocationProcessor(
            deviceModel = Build.MODEL ?: "",
            appVersionName = appVersionName()
        )

        try {
            startForeground(NOTIFICATION_ID, buildNotification("Starting GPS tracking…"))
        } catch (e: Exception) {
            // e.g. missing FGS location permission on Android 14+. Don't crash the app.
            android.util.Log.w("LocationTrackingService", "startForeground failed", e)
            stopSelf()
            return
        }
        acquireWakeLock()
        sensorMonitor.start()
        logEvent(token, EventType.TRACKING_STARTED, "Tracking Started")

        source = if (SIMULATE_LOCATION) SimulatedLocationSource() else FusedLocationSource(this)
        source?.start { fix -> onFix(token, fix) }
    }

    private fun onFix(token: String, fix: GpsFix) {
        val proc = processor ?: return
        val result = proc.process(fix, isPaused, sensorMonitor.snapshot) ?: return // jitter-suppressed
        val battery = batteryPercent()
        val row = result.location.copy(token = token, batteryPercentage = battery)
        val stats = proc.stats()
        val durationMs = System.currentTimeMillis() - startTime

        scope.launch(dbDispatcher) {
            locationDao.insertLocation(row)
            savedTrackDao.updateTrackLiveData(token, stats.cleanedDistanceM, durationMs)
            if (!startCoordsWritten && proc.firstLat != null) {
                startCoordsWritten = true
                savedTrackDao.getSavedTrackById(token)?.let { t ->
                    savedTrackDao.updateSavedTrack(
                        t.copy(startLatitude = proc.firstLat!!, startLongitude = proc.firstLng!!)
                    )
                }
            }
            persistSession(token, fix, stats, battery)
        }

        if (result.isMock) logEvent(token, EventType.MOCK_LOCATION, "Mock Location detected", fix)
        if (result.isAbnormal) logEvent(token, EventType.ABNORMAL_LOCATION, "Abnormal Location filtered", fix)

        val speedKmh = fix.speedMps * 3.6
        updateNotification(
            if (isPaused) "Paused · %.2f km".format(stats.cleanedDistanceM / 1000)
            else "%.2f km · %.0f km/h".format(stats.cleanedDistanceM / 1000, speedKmh)
        )
    }

    private fun setPaused(paused: Boolean) {
        val token = activeToken ?: return
        isPaused = paused
        logEvent(
            token,
            if (paused) EventType.TRACKING_PAUSED else EventType.TRACKING_RESUMED,
            if (paused) "Tracking Paused" else "Tracking Resumed"
        )
        updateNotification(if (paused) "Tracking paused" else "Tracking resumed")
    }

    private fun stopAndFinalize() {
        val token = activeToken
        val proc = processor
        source?.stop()
        sensorMonitor.stop()
        if (token != null && proc != null) {
            val stats = proc.stats()
            val endTime = System.currentTimeMillis()
            scope.launch(dbDispatcher) {
                savedTrackDao.finalizeTrack(
                    routeId = token,
                    endTime = endTime,
                    finalDistance = stats.cleanedDistanceM,
                    avgSpeed = stats.avgSpeedMps,
                    maxSpeed = stats.maxSpeedMps
                )
                logEventSuspend(token, EventType.TRACKING_STOPPED, "Tracking Stopped")
                clearSessionTracking()
            }
        }
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ---- session + events -------------------------------------------------

    private suspend fun persistSession(token: String, fix: GpsFix, stats: com.miletracker.feature.tracking.service.location.TrackStats, battery: Double) {
        val current = currentSession()
        sessionStore.saveSession(
            current.copy(
                token = token,
                isTracking = true,
                isPaused = isPaused,
                startLatitude = proc?.firstLat ?: current.startLatitude,
                startLongitude = proc?.firstLng ?: current.startLongitude,
                endLatitude = fix.lat,
                endLongitude = fix.lng,
                startTime = startTime,
                distance = stats.cleanedDistanceM,
                speed = fix.speedMps.toDouble(),
                avgSpeed = stats.avgSpeedMps,
                maxSpeed = stats.maxSpeedMps,
                totalLocationPoints = stats.totalPoints.toLong(),
                wasEverPaused = current.wasEverPaused || isPaused,
                startedAtTimestamp = if (current.startedAtTimestamp == 0L) startTime else current.startedAtTimestamp
            )
        )
    }

    private val proc: LocationProcessor? get() = processor

    private suspend fun currentSession() = sessionStore.currentTrackFlow.first()

    private suspend fun clearSessionTracking() {
        val current = currentSession()
        sessionStore.saveSession(current.copy(isTracking = false, isPaused = false, endTime = System.currentTimeMillis()))
    }

    private fun logEvent(token: String, type: EventType, text: String, fix: GpsFix? = null) {
        scope.launch(dbDispatcher) { logEventSuspend(token, type, text, fix) }
    }

    private suspend fun logEventSuspend(token: String, type: EventType, text: String, fix: GpsFix? = null) {
        hardwareEventDao.insert(
            HardwareEvent(
                token = token,
                eventType = type,
                event = text,
                time = System.currentTimeMillis(),
                lat = fix?.lat,
                lng = fix?.lng,
                speed = fix?.speedMps,
                audience = EventAudience.USER,
                deviceModel = Build.MODEL ?: "",
                appVersionName = appVersionName()
            )
        )
    }

    // ---- device helpers ---------------------------------------------------

    private fun batteryPercent(): Double {
        val bm = getSystemService(BATTERY_SERVICE) as? BatteryManager
        val pct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        return pct.toDouble()
    }

    private fun appVersionName(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: ""
    }.getOrDefault("")

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as? PowerManager ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miletracker:tracking").apply {
            setReferenceCounted(false)
            acquire(60 * 60 * 1000L) // 1h safety cap
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Location Tracking", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Tracks your current journey" }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("MileTracker")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        source?.stop()
        sensorMonitor.stop()
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }
}
