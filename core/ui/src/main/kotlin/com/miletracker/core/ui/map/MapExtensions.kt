package com.miletracker.core.ui.map

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

fun MapView.configure(context: Context, zoomLevel: Double = 14.0) {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    controller.setZoom(zoomLevel)
    minZoomLevel = 4.0
    maxZoomLevel = 21.0
}

fun MapView.centerOn(lat: Double, lng: Double, zoom: Double = 15.0) {
    controller.setZoom(zoom)
    controller.setCenter(GeoPoint(lat, lng))
}

fun MapView.drawPolyline(points: List<GeoPoint>, color: Int = Color.parseColor("#1565C0"), width: Float = 6f): Polyline {
    val line = Polyline().apply {
        setPoints(points)
        outlinePaint.color = color
        outlinePaint.strokeWidth = width
    }
    overlays.add(line)
    invalidate()
    return line
}

fun MapView.fitBounds(points: List<GeoPoint>) {
    if (points.isEmpty()) return
    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLng = points.minOf { it.longitude }
    val maxLng = points.maxOf { it.longitude }
    val center = GeoPoint((minLat + maxLat) / 2, (minLng + maxLng) / 2)
    controller.setCenter(center)
    val latSpan = maxLat - minLat
    val lngSpan = maxLng - minLng
    val span = maxOf(latSpan, lngSpan)
    val zoom = when {
        span < 0.01 -> 16.0
        span < 0.05 -> 14.0
        span < 0.2 -> 12.0
        span < 1.0 -> 10.0
        else -> 8.0
    }
    controller.setZoom(zoom)
}
