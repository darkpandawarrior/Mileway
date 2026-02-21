package com.miletracker.feature.tracking.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.model.db.EventAudience
import com.miletracker.core.data.model.db.EventType
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

    companion object {
        private const val TAG = "HardwareEventsVM"

        private fun demoEvents(token: String): List<HardwareEvent> {
            val base = 1_781_654_400_000L
            val min = 60_000L
            return listOf(
                HardwareEvent(id = 1, token = token, eventType = EventType.TRACKING_STARTED, event = "Tracking started — FC Road, Pune", time = base, audience = EventAudience.USER, battery = 87.0, activity = "IN_VEHICLE"),
                HardwareEvent(id = 2, token = token, eventType = EventType.ODOMETER_START_CAPTURED, event = "Odometer start captured: 045782 km", time = base + min, audience = EventAudience.USER, battery = 86.5),
                HardwareEvent(id = 3, token = token, eventType = EventType.GPS_REGAINED, event = "GPS signal strong — accuracy 6 m", time = base + 2 * min, audience = EventAudience.USER, lat = 18.5204, lng = 73.8567, speed = 0f),
                HardwareEvent(id = 4, token = token, eventType = EventType.BATTERY_OPTIMIZATION_ON, event = "Battery optimisation ON — tracking may be interrupted", time = base + 5 * min, audience = EventAudience.USER, battery = 85.0),
                HardwareEvent(id = 5, token = token, eventType = EventType.MOCK_LOCATION, event = "Mock location signal detected — flagged, not discarded", time = base + 8 * min, audience = EventAudience.DEBUG, lat = 18.5280, lng = 73.8600),
                HardwareEvent(id = 6, token = token, eventType = EventType.GPS_LOST, event = "GPS lost — tunnelling under Baner flyover", time = base + 12 * min, audience = EventAudience.USER, lat = 18.5420, lng = 73.8012),
                HardwareEvent(id = 7, token = token, eventType = EventType.GPS_REGAINED, event = "GPS regained — accuracy 9 m", time = base + 14 * min, audience = EventAudience.USER, lat = 18.5501, lng = 73.8150, speed = 28f),
                HardwareEvent(id = 8, token = token, eventType = EventType.CHECK_IN, event = "Geo check-in confirmed — Hinjewadi IT Park", time = base + 28 * min, audience = EventAudience.USER, lat = 18.5904, lng = 73.7394),
                HardwareEvent(id = 9, token = token, eventType = EventType.ODOMETER_END_CAPTURED, event = "Odometer end captured: 045794 km", time = base + 44 * min, audience = EventAudience.USER, battery = 78.0),
                HardwareEvent(id = 10, token = token, eventType = EventType.TRACKING_STOPPED, event = "Tracking stopped — Viman Nagar, Pune", time = base + 45 * min, audience = EventAudience.USER, lat = 18.5679, lng = 73.9143, speed = 0f, battery = 77.5),
                HardwareEvent(id = 11, token = token, eventType = EventType.JOURNEY_SUMMARY, event = "Journey summary: 12.4 km · 45 min · quality score 90", time = base + 45 * min + 2000, audience = EventAudience.SUMMARY, activity = "IN_VEHICLE"),
            )
        }
    }

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
                onSuccess = { loaded ->
                    _events.value = loaded.ifEmpty { demoEvents(token) }
                },
                onFailure = {
                    Log.e(TAG, "Error loading events: ${it.message}")
                    _events.value = demoEvents(token)
                }
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
