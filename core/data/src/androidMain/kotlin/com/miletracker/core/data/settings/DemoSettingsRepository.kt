package com.miletracker.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.demoSettingsDataStore by preferencesDataStore(name = "demo_settings")

data class DemoSettings(
    val simulateRoot: Boolean = false,
    val simulateOffline: Boolean = false,
    val biometricGuardEnabled: Boolean = false,
    val simulateGpsDrift: Boolean = false,
    val autoDiscardEnabled: Boolean = false
)

class DemoSettingsRepository(private val context: Context) {

    private val SIMULATE_ROOT = booleanPreferencesKey("demo_simulate_root")
    private val SIMULATE_OFFLINE = booleanPreferencesKey("demo_simulate_offline")
    private val BIOMETRIC_GUARD = booleanPreferencesKey("demo_biometric_guard")
    private val GPS_DRIFT = booleanPreferencesKey("demo_simulate_gps_drift")
    private val AUTO_DISCARD = booleanPreferencesKey("demo_auto_discard")

    val settings: Flow<DemoSettings> = context.demoSettingsDataStore.data.map { prefs ->
        DemoSettings(
            simulateRoot = prefs[SIMULATE_ROOT] ?: false,
            simulateOffline = prefs[SIMULATE_OFFLINE] ?: false,
            biometricGuardEnabled = prefs[BIOMETRIC_GUARD] ?: false,
            simulateGpsDrift = prefs[GPS_DRIFT] ?: false,
            autoDiscardEnabled = prefs[AUTO_DISCARD] ?: false
        )
    }

    suspend fun toggleSimulateRoot() = toggle(SIMULATE_ROOT)
    suspend fun toggleSimulateOffline() = toggle(SIMULATE_OFFLINE)
    suspend fun toggleBiometricGuard() = toggle(BIOMETRIC_GUARD)
    suspend fun toggleGpsDrift() = toggle(GPS_DRIFT)
    suspend fun toggleAutoDiscard() = toggle(AUTO_DISCARD)

    private suspend fun toggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>) {
        context.demoSettingsDataStore.edit { prefs -> prefs[key] = !(prefs[key] ?: false) }
    }
}
