package com.miletracker.core.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.miletracker.core.data.dao.HardwareEventDao
import com.miletracker.core.data.dao.LocationDao
import com.miletracker.core.data.dao.LogMilesDraftDao
import com.miletracker.core.data.dao.LogMilesFrequentRouteDao
import com.miletracker.core.data.dao.SavedTrackDao
import com.miletracker.core.data.model.db.HardwareEvent
import com.miletracker.core.data.model.db.LocationData
import com.miletracker.core.data.model.db.LogMilesDraftEntity
import com.miletracker.core.data.model.db.LogMilesFrequentRouteEntity
import com.miletracker.core.data.model.db.SavedTrack

@Database(
    entities = [
        LocationData::class,
        SavedTrack::class,
        HardwareEvent::class,
        LogMilesDraftEntity::class,
        LogMilesFrequentRouteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(MileTrackerDatabaseConstructor::class)
abstract class MileTrackerDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun savedTrackDao(): SavedTrackDao
    abstract fun hardwareEventDao(): HardwareEventDao
    abstract fun logMilesDraftDao(): LogMilesDraftDao
    abstract fun logMilesFrequentRouteDao(): LogMilesFrequentRouteDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MileTrackerDatabaseConstructor : RoomDatabaseConstructor<MileTrackerDatabase>
