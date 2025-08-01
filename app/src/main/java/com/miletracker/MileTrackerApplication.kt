package com.miletracker

import android.app.Application
import com.miletracker.core.data.di.coreDataModule
import com.miletracker.core.ui.di.coreUiModule
import com.miletracker.feature.logging.di.loggingModule
import com.miletracker.feature.media.di.mediaModule
import com.miletracker.feature.profile.di.profileModule
import com.miletracker.feature.tracking.di.trackingModule
import com.miletracker.seeder.DatabaseSeeder
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
import org.koin.dsl.module
import org.osmdroid.config.Configuration

val appModule = module {
    single { DatabaseSeeder(get(), get()) }
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
                appModule
            )
        }
        appScope.launch { get<DatabaseSeeder>().seedIfEmpty() }
    }
}
