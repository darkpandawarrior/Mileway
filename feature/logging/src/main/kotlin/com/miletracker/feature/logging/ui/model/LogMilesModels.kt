package com.miletracker.feature.logging.ui.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A single searchable place the user can pick as a travelled stop.
 *
 * The demo ships an offline catalogue of Indian cities/landmarks (see
 * [CityCatalog]) so that location search works with no network and no map
 * provider. Each entry carries real-ish coordinates so the great-circle
 * distance between two stops is plausible.
 */
data class LocationEntry(
    val name: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double
)

/**
 * An ordered stop in the journey itinerary. [order] is the 1-based position the
 * user sees; [entry] is the underlying place. Stops are kept in a list inside the
 * UI state so reordering, insertion and removal are simple list operations.
 */
data class LocationStop(
    val id: Long,
    val entry: LocationEntry
)

/** Offline catalogue of ~15 Indian places used by the location-search sheet. */
object CityCatalog {

    /**
     * The full built-in place list. Coordinates are approximate city/landmark
     * centroids — accurate enough for a believable great-circle distance.
     */
    val all: List<LocationEntry> = listOf(
        LocationEntry("Pune, Maharashtra, India", "Maharashtra, India", 18.5204, 73.8567),
        LocationEntry("Mumbai, Maharashtra, India", "Maharashtra, India", 19.0760, 72.8777),
        LocationEntry("Pune Railway Station", "Agarkar Nagar, Pune, Maharashtra", 18.5286, 73.8743),
        LocationEntry("Pune International Airport (PNQ)", "Lohegaon, Pune, Maharashtra", 18.5793, 73.9089),
        LocationEntry("Nashik, Maharashtra, India", "Maharashtra, India", 19.9975, 73.7898),
        LocationEntry("Nagpur, Maharashtra, India", "Maharashtra, India", 21.1458, 79.0882),
        LocationEntry("Ahmedabad, Gujarat, India", "Gujarat, India", 23.0225, 72.5714),
        LocationEntry("Surat, Gujarat, India", "Gujarat, India", 21.1702, 72.8311),
        LocationEntry("Bengaluru, Karnataka, India", "Karnataka, India", 12.9716, 77.5946),
        LocationEntry("Hyderabad, Telangana, India", "Telangana, India", 17.3850, 78.4867),
        LocationEntry("Chennai, Tamil Nadu, India", "Tamil Nadu, India", 13.0827, 80.2707),
        LocationEntry("New Delhi, Delhi, India", "Delhi, India", 28.6139, 77.2090),
        LocationEntry("Jaipur, Rajasthan, India", "Rajasthan, India", 26.9124, 75.7873),
        LocationEntry("Kolkata, West Bengal, India", "West Bengal, India", 22.5726, 88.3639),
        LocationEntry("Bhopal, Madhya Pradesh, India", "Madhya Pradesh, India", 23.2599, 77.4126)
    )

    /** Case-insensitive prefix/substring search over name and subtitle. */
    fun search(query: String): List<LocationEntry> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        return all.filter {
            it.name.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true)
        }
    }
}

/**
 * Great-circle (haversine) distance in kilometres between two coordinates.
 * Used as the default "Calculated" distance before the user verifies/overrides it.
 */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

/**
 * Total great-circle distance across an ordered list of stops (sum of consecutive
 * leg distances). Round trips add the return leg back to the first stop.
 */
fun totalRouteKm(stops: List<LocationStop>, roundTrip: Boolean): Double {
    if (stops.size < 2) return 0.0
    var total = 0.0
    for (i in 0 until stops.size - 1) {
        val a = stops[i].entry
        val b = stops[i + 1].entry
        total += haversineKm(a.lat, a.lng, b.lat, b.lng)
    }
    if (roundTrip) {
        val first = stops.first().entry
        val last = stops.last().entry
        total += haversineKm(last.lat, last.lng, first.lat, first.lng)
    }
    return total
}
