package com.miletracker.core.platform.di

import com.miletracker.core.platform.AndroidLocationTracker
import com.miletracker.core.platform.AndroidNotificationScheduler
import com.miletracker.core.platform.LocationTracker
import com.miletracker.core.platform.NotificationScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android bindings for the platform services.
 * 2.2a: LocationTracker, NotificationScheduler. (Biometric/OCR/DocScan → 2.2b; Permissions → 2.2c.)
 */
actual fun platformModule(): Module = module {
    single<LocationTracker> { AndroidLocationTracker(androidContext()) }
    single<NotificationScheduler> { AndroidNotificationScheduler(androidContext()) }
}
