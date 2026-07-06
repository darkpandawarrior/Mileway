package com.mileway.core.platform

import io.github.aakira.napier.Napier
import kotlin.concurrent.Volatile

/**
 * CF.2: analytics sink that logs (self-clamped) events via Napier. This is the noGms + iOS + desktop
 * analytics impl (no real backend); the gms flavor binds a Firebase-backed helper (CF.3).
 *
 * Telemetry kill switch: [setEnabled] gates both [log] and [setUserProperty] — disabled drops every
 * call silently rather than queuing it, so flipping the switch off mid-session has immediate effect.
 */
class LoggingAnalyticsHelper : AnalyticsHelper {
    @Volatile
    private var enabled = true

    override fun log(event: AnalyticsEvent) {
        if (!enabled) return
        Napier.d(tag = TAG) { "${event.safeType} ${event.safeParams}" }
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        if (!enabled) return
        Napier.d(tag = TAG) { "userProperty ${name.take(AnalyticsEvent.MAX_NAME)}=$value" }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private companion object {
        const val TAG = "Analytics"
    }
}
