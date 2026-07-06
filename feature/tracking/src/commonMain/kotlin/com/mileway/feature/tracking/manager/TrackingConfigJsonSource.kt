package com.mileway.feature.tracking.manager

import kotlinx.serialization.json.Json

/**
 * Parses [DEFAULT_TRACKING_CONFIG_JSON] into a typed [TrackingConfig] — the local stand-in for a
 * server plugin-config fetch. `ignoreUnknownKeys` + per-field defaults make this tolerant of both
 * missing fields (old JSON, new field added later) and unknown ones (new JSON, old client), so
 * swapping this parse call for a real network response later needs no shape change.
 */
object TrackingConfigJsonSource {
    private val json = Json { ignoreUnknownKeys = true }

    /** Falls back to [TrackingConfig.DEFAULT] if the JSON is malformed rather than throwing. */
    fun load(rawJson: String = DEFAULT_TRACKING_CONFIG_JSON): TrackingConfig =
        runCatching { json.decodeFromString(TrackingConfig.serializer(), rawJson) }
            .getOrDefault(TrackingConfig.DEFAULT)
}
