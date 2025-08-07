package com.miletracker.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 1 → 2: introduce the trip_attachments table.
 *
 * Each row stores one photo attachment (receipt or odometer proof) keyed by the
 * track_token (= SavedTrack.routeId). The type column uses the AttachmentType enum
 * name (TEXT): RECEIPT, ODOMETER_START, ODOMETER_END.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token` ON `trip_attachments` (`track_token`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_trip_attachments_track_token_type` ON `trip_attachments` (`track_token`, `type`)"
        )
    }
}
