package com.miletracker.core.platform

// TODO(ios): BGTaskScheduler register + submit (Phase 4.6)
class IosBackgroundScheduler : BackgroundScheduler {
    override fun schedulePeriodic(
        uniqueName: String,
        intervalMinutes: Long,
    ) {}

    override fun cancel(uniqueName: String) {}
}
