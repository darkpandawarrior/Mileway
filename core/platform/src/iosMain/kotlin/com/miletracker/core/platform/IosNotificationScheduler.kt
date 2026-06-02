package com.miletracker.core.platform

// TODO(ios): UNUserNotificationCenter (Phase 4 / Phase 6.3)
class IosNotificationScheduler : NotificationScheduler {
    override suspend fun ensurePermission(): Boolean = false

    override fun notify(
        id: Int,
        title: String,
        body: String,
    ) {}

    override fun cancel(id: Int) {}
}
