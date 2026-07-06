package com.mileway.core.network.netlog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory ring buffer of the last [capacity] [NetworkLogEntry]s. Pure and platform-agnostic —
 * no Room needed, log history doesn't need to survive process death. Newest entry first.
 */
class NetworkLogStore(
    private val capacity: Int = 200,
) {
    private val _entries = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val entries: StateFlow<List<NetworkLogEntry>> = _entries

    fun record(entry: NetworkLogEntry) {
        _entries.update { (listOf(entry) + it).take(capacity) }
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
