package com.miletracker.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun buildMileTrackerDatabase(context: Context): MileTrackerDatabase =
    Room.databaseBuilder<MileTrackerDatabase>(
        context = context.applicationContext,
        name = "miletracker.db"
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        .build()
