package com.miletracker.feature.tracking.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miletracker.core.data.settings.DemoSettingsRepository
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Runs at 22:00 daily. If the auto-discard demo flag is enabled and a journey is
 * still active at that time, the journey is marked as discarded and its location
 * rows are removed to free space.
 */
class AutoDiscardWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {
    private val demoSettingsRepository: DemoSettingsRepository by inject()
    private val currentTrackRepository: CurrentTrackRepository by inject()
    private val savedTrackRepository: SavedTrackRepository by inject()
    private val locationRepository: LocationRepository by inject()

    override suspend fun doWork(): Result {
        val settings = demoSettingsRepository.settings.first()
        if (!settings.autoDiscardEnabled) return Result.success()

        val active = savedTrackRepository.getActiveTrack() ?: return Result.success()
        locationRepository.deleteForToken(active.routeId)
        currentTrackRepository.clearSession()
        return Result.success()
    }

    companion object {
        const val TAG = "auto_discard"
    }
}
