package com.miletracker.feature.tracking.di

import com.miletracker.core.network.config.ConfigProvider
import com.miletracker.feature.tracking.debug.DebugMenuComposeViewModel
import com.miletracker.feature.tracking.insights.RouteAnalyzer
import com.miletracker.feature.tracking.manager.LocationTrackingController
import com.miletracker.feature.tracking.manager.TrackingConfigManager
import com.miletracker.feature.tracking.repository.CurrentTrackRepository
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.LogMilesSubmissionRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.repository.TripAttachmentRepository
import com.miletracker.feature.tracking.repository.VehiclePricingRepository
import com.miletracker.feature.tracking.repository.VoucherRepository
import com.miletracker.feature.tracking.viewmodel.CreateVoucherViewModel
import com.miletracker.feature.tracking.viewmodel.ExportViewModel
import com.miletracker.feature.tracking.viewmodel.HardwareEventsViewModel
import com.miletracker.feature.tracking.viewmodel.LiveTrackViewModel
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
import com.miletracker.feature.tracking.viewmodel.SavedTracksViewModel
import com.miletracker.feature.tracking.viewmodel.TrackDetailViewModel
import com.miletracker.feature.tracking.viewmodel.TrackInsightsViewModel
import com.miletracker.feature.tracking.viewmodel.TrackMilesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val trackingModule =
    module {
        single { TrackingConfigManager(get<ConfigProvider>()) }
        single { SavedTrackRepository(get()) }
        single { LocationRepository(get()) }
        single { VehiclePricingRepository(get()) }
        single { LogMilesSubmissionRepository(get()) }
        single { CurrentTrackRepository(get()) }
        single { HardwareEventRepository(get()) }
        single { TripAttachmentRepository(get()) }
        single { VoucherRepository() }
        single { LocationTrackingController(androidContext()) }

        // viewModelOf resolves every constructor parameter through Koin — Kotlin default
        // arguments are NOT applied — so the analyzer needs an explicit definition.
        single { RouteAnalyzer() }

        viewModelOf(::SavedTracksViewModel)
        viewModelOf(::TrackMilesViewModel)
        viewModelOf(::MileageSubmissionViewModel)
        viewModelOf(::TrackDetailViewModel)
        viewModelOf(::LiveTrackViewModel)
        viewModelOf(::HardwareEventsViewModel)
        viewModelOf(::TrackInsightsViewModel)
        viewModelOf(::ExportViewModel)
        viewModelOf(::DebugMenuComposeViewModel)
        viewModelOf(::CreateVoucherViewModel)
    }
