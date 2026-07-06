package com.mileway.core.platform

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CF.2/CF.4: the telemetry kill switch — [LoggingAnalyticsHelper.setEnabled] and
 * [NapierCrashReporter.setEnabled] must drop every call while disabled and resume passing them
 * through once re-enabled. A recording [Antilog] stands in for the real Napier backend so the
 * assertions don't depend on log output formatting.
 */
class TelemetryKillSwitchTest {
    private class RecordingAntilog : Antilog() {
        val messages = mutableListOf<String>()

        override fun performLog(
            priority: LogLevel,
            tag: String?,
            throwable: Throwable?,
            message: String?,
        ) {
            message?.let { messages.add(it) }
        }
    }

    private val recorder = RecordingAntilog()

    @BeforeTest
    fun setUp() {
        Napier.base(recorder)
    }

    @AfterTest
    fun tearDown() {
        Napier.takeLogarithm()
    }

    @Test
    fun disabled_analytics_sink_drops_events() {
        val sink = LoggingAnalyticsHelper()
        sink.setEnabled(false)

        sink.log(AnalyticsEvent("trip_started"))
        sink.setUserProperty("plan", "pro")

        assertTrue(recorder.messages.isEmpty())
    }

    @Test
    fun enabled_analytics_sink_passes_events_through() {
        val sink = LoggingAnalyticsHelper()

        sink.log(AnalyticsEvent("trip_started"))

        assertEquals(1, recorder.messages.size)
        assertTrue(recorder.messages.single().contains("trip_started"))
    }

    @Test
    fun re_enabling_the_analytics_sink_resumes_delivery() {
        val sink = LoggingAnalyticsHelper()
        sink.setEnabled(false)
        sink.log(AnalyticsEvent("dropped"))
        sink.setEnabled(true)
        sink.log(AnalyticsEvent("delivered"))

        assertEquals(1, recorder.messages.size)
        assertTrue(recorder.messages.single().contains("delivered"))
    }

    @Test
    fun disabled_crash_reporter_drops_breadcrumbs_and_exceptions() {
        val reporter = NapierCrashReporter()
        reporter.setEnabled(false)

        reporter.log("breadcrumb")
        reporter.recordException(IllegalStateException("boom"))
        reporter.setCustomKey("k", "v")

        assertTrue(recorder.messages.isEmpty())
    }

    @Test
    fun enabled_crash_reporter_passes_breadcrumbs_through() {
        val reporter = NapierCrashReporter()

        reporter.log("breadcrumb")

        assertEquals(1, recorder.messages.size)
        assertTrue(recorder.messages.single().contains("breadcrumb"))
    }
}
