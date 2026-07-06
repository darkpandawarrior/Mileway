package com.mileway.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.abnormalDetectionSettingsDataStore by preferencesDataStore(name = "abnormal_detection_settings")

/** Android actual: persists [AbnormalDetectionOverrides] via Jetpack DataStore<Preferences>. */
class AbnormalDetectionSettingsDataStore(private val context: Context) : AbnormalDetectionSettingsSource {
    private val spikeHardGateKey = doublePreferencesKey("abnormal_spike_hard_gate_m")
    private val gapTier5mKey = doublePreferencesKey("abnormal_gap_tier_5m_mps")
    private val gapTier1hKey = doublePreferencesKey("abnormal_gap_tier_1h_mps")
    private val gapTier6hKey = doublePreferencesKey("abnormal_gap_tier_6h_mps")
    private val gapMaxDistanceKey = doublePreferencesKey("abnormal_gap_max_distance_m")

    override val overrides: Flow<AbnormalDetectionOverrides> =
        context.abnormalDetectionSettingsDataStore.data.map { prefs -> prefs.toOverrides() }

    suspend fun setSpikeHardGateM(value: Double?) = setOrClear(spikeHardGateKey, value)

    suspend fun setGapTier5mMps(value: Double?) = setOrClear(gapTier5mKey, value)

    suspend fun setGapTier1hMps(value: Double?) = setOrClear(gapTier1hKey, value)

    suspend fun setGapTier6hMps(value: Double?) = setOrClear(gapTier6hKey, value)

    suspend fun setGapMaxDistanceM(value: Double?) = setOrClear(gapMaxDistanceKey, value)

    private suspend fun setOrClear(
        key: Preferences.Key<Double>,
        value: Double?,
    ) {
        context.abnormalDetectionSettingsDataStore.edit { prefs ->
            if (value != null) prefs[key] = value else prefs.remove(key)
        }
    }

    private fun Preferences.toOverrides() =
        AbnormalDetectionOverrides(
            spikeHardGateM = this[spikeHardGateKey],
            gapTier5mMps = this[gapTier5mKey],
            gapTier1hMps = this[gapTier1hKey],
            gapTier6hMps = this[gapTier6hKey],
            gapMaxDistanceM = this[gapMaxDistanceKey],
        )
}
