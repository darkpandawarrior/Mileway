package com.miletracker

import com.miletracker.core.platform.AppReviewManagerFactory
import com.miletracker.core.platform.AppUpdateManagerFactory
import com.miletracker.platform.gms.PlayAppReviewManagerFactoryImpl
import com.miletracker.platform.gms.PlayAppUpdateManagerFactoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * gms flavor: real Play-Core / Firebase platform services.
 *
 * Mirrors [mapsKoinModule] — one per-flavor entry point. Review / FCM / analytics / crash factories are
 * bound here as their phases land (RV.2 / FCM.2 / CF.3 / CF.4).
 */
fun platformServicesKoinModule(): Module =
    module {
        single<AppUpdateManagerFactory> { PlayAppUpdateManagerFactoryImpl() }
        single<AppReviewManagerFactory> { PlayAppReviewManagerFactoryImpl() }
    }
