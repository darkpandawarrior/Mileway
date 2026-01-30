package com.miletracker.core.ui.map

import android.content.Context
import android.graphics.Color
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.FileOutputStream

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

/**
 * Switches the map to serve tiles from the bundled demo_region.mbtiles asset,
 * with the online MAPNIK source as a fallback for tiles not in the archive.
 *
 * The MBTiles file is copied to the cache dir on first call (16 KB schema only
 * in the demo — no actual tile blobs). Tiles missing from the archive fall through
 * to the download provider transparently.
 */
fun MapView.enableOfflineTiles(context: Context) {
    val assetName = "demo_region.mbtiles"
    val cacheFile = File(context.cacheDir, assetName)
    if (!cacheFile.exists()) {
        try {
            context.assets.open(assetName).use { ins ->
                FileOutputStream(cacheFile).use { out -> ins.copyTo(out) }
            }
        } catch (e: Exception) {
            return  // asset unavailable — leave online tiles unchanged
        }
    }

    val receiver = SimpleRegisterReceiver(context)
    val tileSource = TileSourceFactory.MAPNIK

    val archive = OfflineMbTilesSource().also { it.setIgnoreTileSource(true) }
    archive.init(cacheFile)

    val offlineProvider = MapTileFileArchiveProvider(receiver, tileSource, arrayOf(archive))
    val onlineProvider = MapTileDownloader(tileSource, null, NetworkAvailabliltyCheck(context))

    setTileProvider(MapTileProviderArray(tileSource, receiver, arrayOf(offlineProvider, onlineProvider)))
}

/** Restores the default MapTileProviderBasic (MAPNIK online) tile provider. */
fun MapView.disableOfflineTiles(context: Context) {
    setTileProvider(MapTileProviderBasic(context, TileSourceFactory.MAPNIK))
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
