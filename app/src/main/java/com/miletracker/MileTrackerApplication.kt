package com.miletracker

import android.app.Application
import com.miletracker.core.data.di.coreDataModule
import com.miletracker.core.ui.di.coreUiModule
import com.miletracker.feature.approvals.di.approvalsModule
import com.miletracker.feature.logging.di.loggingModule
import com.miletracker.feature.media.di.mediaModule
import com.miletracker.feature.profile.di.profileModule
import com.miletracker.feature.tracking.checkin.CheckInValidator.CheckInLocation
import com.miletracker.feature.tracking.di.trackingModule
import com.miletracker.feature.tracking.viewmodel.CheckInViewModel
import com.miletracker.seeder.DatabaseSeeder
import com.miletracker.ui.home.homeModule
import com.miletracker.stub.DemoConfigManager
import com.miletracker.stub.di.stubModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.osmdroid.config.Configuration

val appModule = module {
    single { DatabaseSeeder(get(), get()) }

    // Geofence location list: convert DemoConfigManager's mock locations into
    // CheckInValidator.CheckInLocation for local offline radius validation.
    single<List<CheckInLocation>> {
        get<DemoConfigManager>().getMockCheckInLocations().map { mock ->
            CheckInLocation(
                id = mock.id,
                name = mock.name,
                lat = mock.lat,
                lng = mock.lng,
                type = mock.type,
                radiusMeters = mock.radiusMeters
            )
        }
    }

    viewModel {
        CheckInViewModel(
            locationRepo = get(),
            hardwareEventRepo = get(),
            currentTrackRepository = get(),
            geoCheckInLocations = get<List<CheckInLocation>>(),
            defaultRadiusMeters = get<DemoConfigManager>().getDefaultGeoCheckInRadiusMeters()
        )
    }
}

class MileTrackerApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Initialize osmdroid with the app's package name as the user-agent string.
        // Must happen before any MapView is inflated.
        Configuration.getInstance().userAgentValue = packageName
        startKoin {
            androidContext(this@MileTrackerApplication)
            androidLogger(Level.DEBUG)
            modules(
                coreDataModule,
                coreUiModule,
                stubModule,
                trackingModule,
                loggingModule,
                mediaModule,
                profileModule,
                approvalsModule,
                homeModule,
                appModule
            )
        }
        appScope.launch { get<DatabaseSeeder>().seedIfEmpty() }
    }
}
