package com.miletracker.feature.tracking.di

import com.miletracker.core.network.config.ConfigProvider
import com.miletracker.feature.tracking.manager.LocationTrackingController
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import org.koin.android.ext.koin.androidContext
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.LogMilesSubmissionRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import com.miletracker.feature.tracking.viewmodel.HardwareEventsViewModel
import com.miletracker.feature.tracking.viewmodel.LiveTrackViewModel
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
import com.miletracker.feature.tracking.viewmodel.SavedTracksViewModel
import com.miletracker.feature.tracking.viewmodel.TrackDetailViewModel
import com.miletracker.feature.tracking.viewmodel.TrackInsightsViewModel
import com.miletracker.feature.tracking.viewmodel.TrackMilesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val trackingModule = module {
    single { TrackingConfigManager(get<ConfigProvider>()) }
    single { SavedTrackRepository(get()) }
    single { LocationRepository(get()) }
    single { VehiclePricingRepository(get()) }
    single { LogMilesSubmissionRepository(get()) }
    single { CurrentTrackRepository(get()) }
    single { HardwareEventRepository(get()) }
    single { LocationTrackingController(androidContext()) }

    viewModelOf(::SavedTracksViewModel)
    viewModelOf(::TrackMilesViewModel)
    viewModelOf(::MileageSubmissionViewModel)
    viewModelOf(::TrackDetailViewModel)
    viewModelOf(::LiveTrackViewModel)
    viewModelOf(::HardwareEventsViewModel)
    viewModelOf(::TrackInsightsViewModel)
}
