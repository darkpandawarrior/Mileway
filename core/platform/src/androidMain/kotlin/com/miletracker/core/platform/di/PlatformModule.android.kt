package com.miletracker.core.platform.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android bindings for the platform services. Concrete implementations
 * (LocationTracker, TextRecognizer, NotificationScheduler, …) are bound here in Phase 2.2.
 */
actual fun platformModule(): Module = module {
    // TODO(2.2): bind Android implementations of the :core:platform service interfaces.
}
