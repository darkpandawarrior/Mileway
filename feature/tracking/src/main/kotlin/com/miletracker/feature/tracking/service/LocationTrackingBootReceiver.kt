package com.miletracker.feature.tracking.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.miletracker.core.data.session.CurrentTrackDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationTrackingBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TrackingBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Boot/update received — checking for active session")
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    try {
                        val dataStore = CurrentTrackDataStore(context)
                        val session = dataStore.currentTrackFlow.first()
                        if (session.isTracking && session.token.isNotEmpty()) {
                            Log.i(TAG, "Active session found (${session.token.take(8)}…) — restarting service")
                            val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
                                action = "RESTORE_SESSION"
                                putExtra(LocationTrackingConstants.EXTRA_TOKEN, session.token)
                            }
                            context.startForegroundService(serviceIntent)
                        } else {
                            Log.d(TAG, "No active session — nothing to restore")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking session on boot", e)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
