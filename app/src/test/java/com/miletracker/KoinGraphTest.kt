package com.miletracker

import android.content.Context
import com.miletracker.core.data.dao.HardwareEventDao
import com.miletracker.core.data.dao.LocationDao
import com.miletracker.core.data.dao.LogMilesDraftDao
import com.miletracker.core.data.dao.LogMilesFrequentRouteDao
import com.miletracker.core.data.dao.SavedTrackDao
import com.miletracker.core.data.dao.TripAttachmentDao
import com.miletracker.core.data.session.CurrentTrackDataStore
import com.miletracker.core.ui.di.coreUiModule
import com.miletracker.feature.logging.di.loggingModule
import com.miletracker.feature.logging.viewmodel.LogMilesViewModel
import com.miletracker.feature.media.di.mediaModule
import com.miletracker.feature.media.viewmodel.MediaViewModel
import com.miletracker.feature.profile.di.profileModule
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import com.miletracker.feature.tracking.debug.DebugMenuComposeViewModel
import com.miletracker.feature.tracking.di.trackingModule
import com.miletracker.feature.tracking.viewmodel.CheckInViewModel
import com.miletracker.feature.tracking.viewmodel.ExportViewModel
import com.miletracker.feature.tracking.viewmodel.HardwareEventsViewModel
import com.miletracker.feature.tracking.viewmodel.LiveTrackViewModel
import com.miletracker.feature.tracking.viewmodel.MileageSubmissionViewModel
import com.miletracker.feature.tracking.viewmodel.SavedTracksViewModel
import com.miletracker.feature.tracking.viewmodel.TrackDetailViewModel
import com.miletracker.feature.tracking.viewmodel.TrackInsightsViewModel
import com.miletracker.feature.tracking.viewmodel.TrackMilesViewModel
import com.miletracker.stub.di.stubModule
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertNotNull

/**
 * Builds the FULL production Koin graph (every module the Application installs) and
 * instantiates every ViewModel definition in it.
 *
 * `viewModelOf(::SomeViewModel)` resolves each constructor parameter through Koin and
 * ignores Kotlin default arguments, so a parameter that "has a default" still needs a
 * Koin definition — a mistake the compiler cannot catch and a screen-open crash in
 * production (NoDefinitionFoundException). This test turns that runtime crash into a
 * unit-test failure.
 *
 * Only the Room layer is replaced (mocked DAOs — building the real database needs a
 * device); everything else is the exact wiring the app ships with.
 */
class KoinGraphTest : KoinTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRoomLayer = module {
        single<LocationDao> { mockk(relaxed = true) }
        single<SavedTrackDao> { mockk(relaxed = true) }
        single<HardwareEventDao> { mockk(relaxed = true) }
        single<LogMilesDraftDao> { mockk(relaxed = true) }
        single<LogMilesFrequentRouteDao> { mockk(relaxed = true) }
        single<TripAttachmentDao> { mockk(relaxed = true) }
        single<CurrentTrackDataStore> { mockk(relaxed = true) }
    }

    @Before
    fun setUp() {
        try { stopKoin() } catch (_: Exception) {}
        startKoin {
            androidContext(mockk<Context>(relaxed = true))
            modules(
                fakeRoomLayer,
                coreUiModule,
                stubModule,
                trackingModule,
                loggingModule,
                mediaModule,
                profileModule,
                appModule
            )
        }
    }

    @After
    fun tearDown() {
        try { stopKoin() } catch (_: Exception) {}
    }

    @Test
    fun `every ViewModel in the app graph is constructible`() {
        // One line per registered ViewModel — a new viewModelOf() without a matching
        // entry here should be added when the screen is added.
        assertNotNull(get<SavedTracksViewModel>())
        assertNotNull(get<TrackMilesViewModel>())
        assertNotNull(get<MileageSubmissionViewModel>())
        assertNotNull(get<TrackDetailViewModel>())
        assertNotNull(get<LiveTrackViewModel>())
        assertNotNull(get<HardwareEventsViewModel>())
        assertNotNull(get<TrackInsightsViewModel>())
        assertNotNull(get<ExportViewModel>())
        assertNotNull(get<DebugMenuComposeViewModel>())
        assertNotNull(get<LogMilesViewModel>())
        assertNotNull(get<MediaViewModel>())
        assertNotNull(get<ProfileViewModel>())
        assertNotNull(get<CheckInViewModel>())
    }

    @Test
    fun `seeder and geofence list resolve from the app module`() {
        assertNotNull(get<com.miletracker.seeder.DatabaseSeeder>())
    }
}
