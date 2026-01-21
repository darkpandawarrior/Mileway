package com.miletracker.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration 1 → 2: introduce the trip_attachments table.
 *
 * Each row stores one photo attachment (receipt or odometer proof) keyed by the
 * track_token (= SavedTrack.routeId). The type column uses the AttachmentType enum
 * name (TEXT): RECEIPT, ODOMETER_START, ODOMETER_END.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `media_library` (
                `id`        TEXT    NOT NULL PRIMARY KEY,
                `uri`       TEXT    NOT NULL,
                `mimeType`  TEXT    NOT NULL,
                `label`     TEXT    NOT NULL,
                `source`    TEXT    NOT NULL,
                `savedAtMs` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `trip_attachments` (
                `id`          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `track_token` TEXT    NOT NULL,
                `type`        TEXT    NOT NULL,
                `uri`         TEXT    NOT NULL,
                `ocr_text`    TEXT,
                `created_at`  INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token` ON `trip_attachments` (`track_token`)"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token_type` ON `trip_attachments` (`track_token`, `type`)"
        )
    }
}
