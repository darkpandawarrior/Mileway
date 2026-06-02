package com.miletracker.core.platform

// TODO(ios): moko-permissions or AVAuthorizationStatus / CLAuthorizationStatus per AppPermission
class IosPermissionsProvider : PermissionsProvider {
    override suspend fun isGranted(permission: AppPermission): Boolean = false

    override suspend fun request(permission: AppPermission): PermissionResult = PermissionResult.Denied
}
