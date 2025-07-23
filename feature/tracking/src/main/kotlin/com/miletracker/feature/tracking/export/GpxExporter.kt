package com.miletracker.feature.tracking.export

import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.SavedTrack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Pure-Kotlin GPX exporter. Produces a valid GPX 1.1 document as a String.
 */
object GpxExporter {

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(
        track: SavedTrack,
        locations: List<LocationData>,
        @Suppress("UNUSED_PARAMETER") events: List<HardwareEvent>
    ): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine(
            """<gpx version="1.1" creator="MileTracker" xmlns="http://www.topografix.com/GPX/1/1">"""
        )
        appendLine("  <trk>")
        appendLine("    <name>${xmlEsc(track.name)}</name>")
        appendLine("    <desc>routeId: ${xmlEsc(track.routeId)}</desc>")
        appendLine("    <trkseg>")

        for (loc in locations) {
            val time = isoFmt.format(Date(loc.date))
            appendLine("""      <trkpt lat="${loc.lat}" lon="${loc.lng}">""")
            appendLine("        <ele>${loc.altitude}</ele>")
            appendLine("        <time>$time</time>")
            appendLine("        <speed>${loc.speed}</speed>")
            if (loc.accuracy > 0f) {
                appendLine("        <hdop>${loc.accuracy}</hdop>")
            }
            appendLine("      </trkpt>")
        }

        appendLine("    </trkseg>")
        appendLine("  </trk>")
        appendLine("</gpx>")
    }

    private fun xmlEsc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
