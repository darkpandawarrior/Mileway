package com.miletracker

import com.miletracker.core.platform.AppUpdateManagerFactory
import com.miletracker.core.platform.PlatformBindings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * noGms (FOSS / F-Droid) flavor: no-op platform services — no proprietary deps.
 *
 * In-app update is a no-op on F-Droid (updates come from the store client, not Play-Core). The factory
 * returns the shared [PlatformBindings] no-op so the LocalManagerProvider wiring is identical to gms.
 */
fun platformServicesKoinModule(): Module =
    module {
        single<AppUpdateManagerFactory> { AppUpdateManagerFactory { PlatformBindings().appUpdateManager } }
    }
