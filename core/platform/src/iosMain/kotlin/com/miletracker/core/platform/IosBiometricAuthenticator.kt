package com.miletracker.core.platform

// TODO(ios): LocalAuthentication LAContext.evaluatePolicy (Phase 4.5)
class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun isAvailable(): Boolean = false

    override suspend fun authenticate(reason: String): BiometricResult = BiometricResult.Unavailable
}
