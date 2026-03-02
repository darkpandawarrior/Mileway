package com.miletracker

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder
import com.miletracker.core.data.di.coreDataModule
import com.miletracker.core.ui.di.coreUiModule
import com.miletracker.feature.agent.di.agentModule
import com.miletracker.feature.approvals.di.approvalsModule
import com.miletracker.feature.payables.di.payablesModule
import com.miletracker.feature.travel.di.travelModule
import com.miletracker.feature.logging.di.loggingModule
import com.miletracker.feature.media.di.mediaModule
import com.miletracker.feature.profile.di.profileModule
import com.miletracker.feature.tracking.checkin.CheckInValidator.CheckInLocation
import com.miletracker.feature.tracking.di.trackingModule
import com.miletracker.feature.tracking.viewmodel.CheckInViewModel
import com.miletracker.seeder.DatabaseSeeder
import com.miletracker.ui.home.homeModule
import com.miletracker.debug.WormaCeptorHelper
import com.miletracker.stub.DemoConfigManager
import com.miletracker.stub.di.stubModule
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.miletracker.feature.tracking.service.MileageMaintenanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import dev.tmapps.konnection.Konnection
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit

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

class MileTrackerApplication : Application(), SingletonImageLoader.Factory {

    // Coil 3: SingletonImageLoader.Factory#newImageLoader now receives a PlatformContext.
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
            add(SvgDecoder.Factory())
        }
        .build()

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Initialize osmdroid with the app's package name as the user-agent string.
        // Must happen before any MapView is inflated.
        Configuration.getInstance().userAgentValue = packageName
        // Initialize WormaCeptor for HTTP inspection in debug builds (no-op in release).
        WormaCeptorHelper.init(this)
        // Initialize konnection for KMP network connectivity monitoring.
        Konnection.createInstance(this)
        if (GlobalContext.getOrNull() != null) stopKoin()
        startKoin {
            androidContext(this@MileTrackerApplication)
            androidLogger(Level.ERROR)
            modules(
                coreDataModule,
                coreUiModule,
                stubModule,
                trackingModule,
                loggingModule,
                mediaModule,
                profileModule,
                approvalsModule,
                payablesModule,
                travelModule,
                agentModule,
                homeModule,
                appModule
            )
        }
        appScope.launch {
            get<DatabaseSeeder>().seedIfEmpty()
            scheduleWeeklyMaintenance()
        }
    }

    private fun scheduleWeeklyMaintenance() {
        val request = PeriodicWorkRequestBuilder<MileageMaintenanceWorker>(7, TimeUnit.DAYS)
            .addTag(MileageMaintenanceWorker.TAG)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MileageMaintenanceWorker.TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
