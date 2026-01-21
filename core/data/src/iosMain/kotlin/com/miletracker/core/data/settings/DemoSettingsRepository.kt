package com.miletracker.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

data class DemoSettings(
    val simulateRoot: Boolean = false,
    val simulateOffline: Boolean = false,
    val biometricGuardEnabled: Boolean = false,
    val simulateGpsDrift: Boolean = false,
    val autoDiscardEnabled: Boolean = false
)

class DemoSettingsRepository {

    private val SIMULATE_ROOT = booleanPreferencesKey("demo_simulate_root")
    private val SIMULATE_OFFLINE = booleanPreferencesKey("demo_simulate_offline")
    private val BIOMETRIC_GUARD = booleanPreferencesKey("demo_biometric_guard")
    private val GPS_DRIFT = booleanPreferencesKey("demo_simulate_gps_drift")
    private val AUTO_DISCARD = booleanPreferencesKey("demo_auto_discard")

    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { (NSTemporaryDirectory() + "demo_settings.preferences_pb").toPath() }
    )

    val settings: Flow<DemoSettings> = store.data.map { prefs ->
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

    private suspend fun toggle(key: Preferences.Key<Boolean>) {
        store.edit { prefs -> prefs[key] = !(prefs[key] ?: false) }
    }
}
