package com.mileway.core.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS runtime permissions (F), queries/requests the per-capability authorization APIs (camera =
 * AVCaptureDevice, location = CLLocationManager, notifications = UNUserNotificationCenter). Storage has no
 * broad user-facing prompt on iOS, so it is always granted. Location grants surface asynchronously through
 * the CLLocationManager delegate, so [request] for it is fire-and-forget and the result is read back later
 * via [isGranted].
 */
class IosPermissionsProvider : PermissionsProvider {
    override suspend fun isGranted(permission: AppPermission): Boolean =
        when (permission) {
            AppPermission.CAMERA ->
                AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
            AppPermission.LOCATION -> {
                val status = CLLocationManager().authorizationStatus
                status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways
            }
            // iOS has no separate background-location prompt (UX.3 tiering is Android-only for this
            // tier) — "always" authorization already covers background, so treat it as the same check.
            AppPermission.LOCATION_BACKGROUND -> CLLocationManager().authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
            AppPermission.NOTIFICATIONS -> notificationsAuthorized()
            // No user-facing prompt on iOS distinct from CoreMotion's own opaque authorization; treated
            // as always-available here since Mileway's iOS activity detection does not gate on it yet.
            AppPermission.ACTIVITY_RECOGNITION -> true
            AppPermission.STORAGE -> true
        }

    override suspend fun request(permission: AppPermission): PermissionResult =
        when (permission) {
            AppPermission.CAMERA ->
                suspendCancellableCoroutine { continuation ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                        continuation.resume(if (granted) PermissionResult.Granted else PermissionResult.Denied)
                    }
                }
            AppPermission.NOTIFICATIONS ->
                suspendCancellableCoroutine { continuation ->
                    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                        UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                    ) { granted, _ ->
                        continuation.resume(if (granted) PermissionResult.Granted else PermissionResult.Denied)
                    }
                }
            AppPermission.LOCATION ->
                if (isGranted(AppPermission.LOCATION)) {
                    PermissionResult.Granted
                } else {
                    CLLocationManager().requestWhenInUseAuthorization()
                    PermissionResult.Denied
                }
            AppPermission.LOCATION_BACKGROUND ->
                if (isGranted(AppPermission.LOCATION_BACKGROUND)) {
                    PermissionResult.Granted
                } else {
                    CLLocationManager().requestAlwaysAuthorization()
                    PermissionResult.Denied
                }
            AppPermission.ACTIVITY_RECOGNITION -> PermissionResult.Granted
            AppPermission.STORAGE -> PermissionResult.Granted
        }

    private suspend fun notificationsAuthorized(): Boolean =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
                continuation.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
            }
        }
}
