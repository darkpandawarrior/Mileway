package com.miletracker.feature.tracking.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.EventAudience
import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.feature.tracking.repository.HardwareEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

enum class ExportFormat { CSV, JSON }

data class EventStats(
    val totalCount: Int = 0,
    val audienceCounts: Map<EventAudience, Int> = emptyMap(),
    val timeRange: Pair<Long, Long>? = null
)

class HardwareEventsViewModel(
    private val eventRepo: HardwareEventRepository
) : ViewModel() {

    companion object { private const val TAG = "HardwareEventsVM" }

    private val _events = MutableStateFlow<List<HardwareEvent>>(emptyList())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedAudiences = MutableStateFlow<Set<EventAudience>>(setOf(EventAudience.USER))
    val selectedAudiences = _selectedAudiences.asStateFlow()

    val filteredEvents = combine(_events, _searchQuery, _selectedAudiences) { events, query, audiences ->
        events.filter { event ->
            val matchesSearch = query.isBlank() ||
                    event.event.contains(query, ignoreCase = true) ||
                    event.activity?.contains(query, ignoreCase = true) == true
            val matchesAudience = audiences.isEmpty() || audiences.contains(event.audience)
            matchesSearch && matchesAudience
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventStats = events.map { list ->
        EventStats(
            totalCount = list.size,
            audienceCounts = list.groupBy { it.audience }.mapValues { it.value.size },
            timeRange = if (list.isNotEmpty())
                (list.minOf { it.time }) to (list.maxOf { it.time })
            else null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventStats())

    fun loadEventsByToken(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            eventRepo.getEventsForRoute(token).fold(
                onSuccess = { _events.value = it },
                onFailure = { Log.e(TAG, "Error loading events: ${it.message}") }
            )
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun toggleAudienceFilter(audience: EventAudience) {
        val current = _selectedAudiences.value.toMutableSet()
        if (current.contains(audience)) current.remove(audience) else current.add(audience)
        _selectedAudiences.value = current
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedAudiences.value = setOf(EventAudience.USER)
    }

    fun exportEvents(context: Context, format: ExportFormat): Uri? {
        val list = filteredEvents.value
        if (list.isEmpty()) return null
        return try {
            val (data, filename) = buildPayload(list, format)
            val file = File(context.cacheDir, filename)
            FileWriter(file).use { it.write(String(data, Charsets.UTF_8)) }
            FileProvider.getUriForFile(context, context.packageName, file)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            null
        }
    }

    fun prepareExportPayload(format: ExportFormat): Triple<ByteArray, String, String>? {
        val list = filteredEvents.value
        if (list.isEmpty()) return null
        val (data, filename) = buildPayload(list, format)
        val mime = if (format == ExportFormat.CSV) "text/csv" else "application/json"
        return Triple(data, filename, mime)
    }

    private fun buildPayload(events: List<HardwareEvent>, format: ExportFormat): Pair<ByteArray, String> {
        return when (format) {
            ExportFormat.CSV -> {
                val sb = StringBuilder("id,token,eventType,event,time,audience,battery,activity\n")
                events.forEach { e ->
                    sb.append("${e.id},${e.token},${e.eventType},\"${e.event}\",${e.time},${e.audience},${e.battery ?: ""},${e.activity ?: ""}\n")
                }
                Pair(sb.toString().toByteArray(), "hardware_events.csv")
            }
            ExportFormat.JSON -> {
                val sb = StringBuilder("[")
                events.forEachIndexed { i, e ->
                    if (i > 0) sb.append(",")
                    sb.append("""{"id":${e.id},"token":"${e.token}","eventType":"${e.eventType}","event":"${e.event.replace("\"", "\\\"")}","time":${e.time},"audience":"${e.audience}"}""")
                }
                sb.append("]")
                Pair(sb.toString().toByteArray(), "hardware_events.json")
            }
        }
    }
}
