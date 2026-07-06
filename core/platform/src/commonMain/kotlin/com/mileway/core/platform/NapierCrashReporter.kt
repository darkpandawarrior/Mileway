package com.mileway.core.platform

import io.github.aakira.napier.Napier
import kotlin.concurrent.Volatile

/**
 * CF.4: crash/breadcrumb reporter that logs locally via Napier — the noGms + iOS + desktop
 * CrashReporter impl (no real backend); the gms flavor binds Firebase Crashlytics
 * ([FirebaseCrashReporter] equivalent, app/src/gms).
 *
 * Telemetry kill switch: [setEnabled] gates [log]/[recordException]/[setCustomKey] the same way
 * Crashlytics' own collection-enabled flag does, so disabled drops silently rather than queuing.
 */
class NapierCrashReporter : CrashReporter {
    @Volatile
    private var enabled = true

    override fun log(message: String) {
        if (!enabled) return
        Napier.d(tag = TAG) { message }
    }

    override fun recordException(throwable: Throwable) {
        if (!enabled) return
        Napier.e(tag = TAG, throwable = throwable) { throwable.message ?: "exception" }
    }

    override fun setCustomKey(
        key: String,
        value: String,
    ) {
        if (!enabled) return
        Napier.d(tag = TAG) { "key $key=$value" }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private companion object {
        const val TAG = "CrashReporter"
    }
}
