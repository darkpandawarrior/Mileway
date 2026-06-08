package com.miletracker.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// Single facade grouping all platform service interfaces.
// Bound as `single<PlatformBindings>` in each platform's Koin module.
// No-op defaults let commonMain code run in tests without a real platform.
data class PlatformBindings(
    val locationTracker: LocationTracker = NoOpLocationTracker,
    val textRecognizer: TextRecognizer = NoOpTextRecognizer,
    val documentScanner: DocumentScanner = NoOpDocumentScanner,
    val notificationScheduler: NotificationScheduler = NoOpNotificationScheduler,
    val backgroundScheduler: BackgroundScheduler = NoOpBackgroundScheduler,
    val biometricAuthenticator: BiometricAuthenticator = NoOpBiometricAuthenticator,
    val permissionsProvider: PermissionsProvider = NoOpPermissionsProvider,
)

private object NoOpLocationTracker : LocationTracker {
    override val updates: Flow<GeoPoint> = emptyFlow()

    override suspend fun current(): GeoPoint? = null

    override fun start() = Unit

    override fun stop() = Unit
}

private object NoOpTextRecognizer : TextRecognizer {
    override suspend fun recognize(imageBytes: ByteArray): String = ""
}

private object NoOpDocumentScanner : DocumentScanner {
    override suspend fun scan(maxPages: Int): List<ByteArray> = emptyList()
}

private object NoOpNotificationScheduler : NotificationScheduler {
    override suspend fun ensurePermission(): Boolean = false

    override fun notify(
        id: Int,
        title: String,
        body: String,
    ) = Unit

    override fun cancel(id: Int) = Unit
}

private object NoOpBackgroundScheduler : BackgroundScheduler {
    override fun schedulePeriodic(
        uniqueName: String,
        intervalMinutes: Long,
    ) = Unit

    override fun cancel(uniqueName: String) = Unit
}

private object NoOpBiometricAuthenticator : BiometricAuthenticator {
    override fun isAvailable(): Boolean = false

    override suspend fun authenticate(reason: String): BiometricResult = BiometricResult.Unavailable
}

private object NoOpPermissionsProvider : PermissionsProvider {
    override suspend fun isGranted(permission: AppPermission): Boolean = false

    override suspend fun request(permission: AppPermission): PermissionResult = PermissionResult.Denied
}
