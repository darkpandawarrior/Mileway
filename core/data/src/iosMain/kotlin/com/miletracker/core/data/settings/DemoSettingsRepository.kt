package com.miletracker.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
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
    val autoDiscardEnabled: Boolean = false,
)

class DemoSettingsRepository {
    private val simulateRootKey = booleanPreferencesKey("demo_simulate_root")
    private val simulateOfflineKey = booleanPreferencesKey("demo_simulate_offline")
    private val biometricGuardKey = booleanPreferencesKey("demo_biometric_guard")
    private val gpsDriftKey = booleanPreferencesKey("demo_simulate_gps_drift")
    private val autoDiscardKey = booleanPreferencesKey("demo_auto_discard")

    private val store: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { (NSTemporaryDirectory() + "demo_settings.preferences_pb").toPath() },
        )

    val settings: Flow<DemoSettings> =
        store.data.map { prefs ->
            DemoSettings(
                simulateRoot = prefs[simulateRootKey] ?: false,
                simulateOffline = prefs[simulateOfflineKey] ?: false,
                biometricGuardEnabled = prefs[biometricGuardKey] ?: false,
                simulateGpsDrift = prefs[gpsDriftKey] ?: false,
                autoDiscardEnabled = prefs[autoDiscardKey] ?: false,
            )
        }

    suspend fun toggleSimulateRoot() = toggle(simulateRootKey)

    suspend fun toggleSimulateOffline() = toggle(simulateOfflineKey)

    suspend fun toggleBiometricGuard() = toggle(biometricGuardKey)

    suspend fun toggleGpsDrift() = toggle(gpsDriftKey)

    suspend fun toggleAutoDiscard() = toggle(autoDiscardKey)

    private suspend fun toggle(key: Preferences.Key<Boolean>) {
        store.edit { prefs -> prefs[key] = !(prefs[key] ?: false) }
    }
}
