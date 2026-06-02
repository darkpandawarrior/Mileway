package com.miletracker.core.platform.di

import com.miletracker.core.platform.IosBackgroundScheduler
import com.miletracker.core.platform.IosBiometricAuthenticator
import com.miletracker.core.platform.IosDocumentScanner
import com.miletracker.core.platform.IosLocationTracker
import com.miletracker.core.platform.IosNotificationScheduler
import com.miletracker.core.platform.IosPermissionsProvider
import com.miletracker.core.platform.IosTextRecognizer
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<com.miletracker.core.platform.LocationTracker> { IosLocationTracker() }
        single<com.miletracker.core.platform.TextRecognizer> { IosTextRecognizer() }
        single<com.miletracker.core.platform.DocumentScanner> { IosDocumentScanner() }
        single<com.miletracker.core.platform.NotificationScheduler> { IosNotificationScheduler() }
        single<com.miletracker.core.platform.BiometricAuthenticator> { IosBiometricAuthenticator() }
        single<com.miletracker.core.platform.BackgroundScheduler> { IosBackgroundScheduler() }
        single<com.miletracker.core.platform.PermissionsProvider> { IosPermissionsProvider() }
    }
