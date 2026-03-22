package com.miletracker.core.platform.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS bindings for the platform services. Concrete implementations backed by CoreLocation, Vision,
 * VisionKit, UNUserNotificationCenter, LocalAuthentication, and BGTaskScheduler are bound here in Phase 4.
 */
// TODO(ios): bind iOS implementations of the :core:platform service interfaces (Phase 4).
actual fun platformModule(): Module = module {
}
