package com.miletracker.core.ui.map

import android.database.sqlite.SQLiteDatabase
import org.osmdroid.tileprovider.modules.IArchiveFile
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.util.MapTileIndex
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

/**
 * Reads tiles from an MBTiles SQLite file for OSMDroid.
 *
 * MBTiles uses TMS tile coordinates (Y=0 at south pole).
 * OSMDroid uses XYZ/OSM coordinates (Y=0 at north pole).
 * Conversion: mbtiles_row = (2^zoom − 1) − osm_y
 */
class OfflineMbTilesSource : IArchiveFile {

    private var db: SQLiteDatabase? = null
    private var ignoreTileSource = false

    override fun init(pFile: File) {
        db = SQLiteDatabase.openDatabase(pFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    }

    override fun getInputStream(tileSource: ITileSource, pMapTileIndex: Long): InputStream? {
        val z = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = (1 shl z) - 1 - MapTileIndex.getY(pMapTileIndex)  // TMS Y-flip

        val cursor = db?.rawQuery(
            "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?",
            arrayOf(z.toString(), x.toString(), y.toString())
        ) ?: return null

        return cursor.use {
            if (it.moveToFirst()) ByteArrayInputStream(it.getBlob(0)) else null
        }
    }

    override fun close() {
        db?.close()
        db = null
    }

    override fun getTileSources(): Set<String> =
        if (db != null) setOf("Mapnik") else emptySet()

    override fun setIgnoreTileSource(pIgnoreTileSource: Boolean) {
        ignoreTileSource = pIgnoreTileSource
    }
}
