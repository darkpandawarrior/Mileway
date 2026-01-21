package com.miletracker.core.data.di

import com.miletracker.core.data.database.MileTrackerDatabase
import com.miletracker.core.data.database.buildMileTrackerDatabase
import com.miletracker.core.data.session.CurrentTrackDataStore
import com.miletracker.core.data.settings.DemoSettingsRepository
import org.koin.dsl.module

val coreDataModule = module {
    single<MileTrackerDatabase> { buildMileTrackerDatabase() }
    single { get<MileTrackerDatabase>().locationDao() }
    single { get<MileTrackerDatabase>().savedTrackDao() }
    single { get<MileTrackerDatabase>().hardwareEventDao() }
    single { get<MileTrackerDatabase>().logMilesDraftDao() }
    single { get<MileTrackerDatabase>().logMilesFrequentRouteDao() }
    single { get<MileTrackerDatabase>().tripAttachmentDao() }
    single { get<MileTrackerDatabase>().mediaLibraryDao() }
    single { CurrentTrackDataStore() }
    single { DemoSettingsRepository() }
}
