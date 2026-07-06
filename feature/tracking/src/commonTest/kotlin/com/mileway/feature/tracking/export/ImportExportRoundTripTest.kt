package com.mileway.feature.tracking.export

import com.mileway.core.data.model.db.LocationData
import com.mileway.core.data.model.db.SavedTrack
import com.mileway.feature.tracking.repository.ImportRepository
import com.mileway.feature.tracking.repository.ImportRepository.CurrentAccount
import com.mileway.feature.tracking.repository.ImportRepository.ImportResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers the versioned JSON export -> import pipeline and the Excel exporter.
 * Pure JVM, no Room — [ImportRepository]'s three suspend seams are backed by an in-memory fake.
 */
class ImportExportRoundTripTest {
    private fun sampleTrack(
        routeId: String = "route-1",
        accountId: String? = "acct-A",
        tenant: String = "DEMO-TENANT",
    ) = SavedTrack(
        routeId = routeId,
        name = "Morning Commute",
        startedByAccountId = accountId,
        startedByTenant = tenant,
        startLatitude = 12.9,
        startLongitude = 77.5,
        endLatitude = 13.0,
        endLongitude = 77.6,
        pausedLatitude = 0.0,
        pausedLongitude = 0.0,
        startTime = 1_000L,
        endTime = 2_000L,
        distance = 4200.0,
        duration = 900_000L,
        avgSpeed = 5.5,
        maxSpeed = 12.0,
        selectedVehicleType = "CAR",
        isCompleted = true,
    )

    private fun samplePoint(
        token: String = "route-1",
        date: Long = 1_500L,
        lat: Double = 12.95,
        lng: Double = 77.55,
    ) = LocationData(
        activity = "IN_VEHICLE",
        speed = 6.1f,
        lat = lat,
        lng = lng,
        token = token,
        date = date,
        accuracy = 8f,
        provider = "gps",
        batteryPercentage = 88.0,
        altitude = 900.0,
        bearing = 45f,
        wasCheckInPoint = true,
    )

    /** In-memory fake standing in for SavedTrack/Location repositories. */
    private class FakeStore {
        val tracks = mutableMapOf<String, SavedTrack>()
        val points = mutableListOf<LocationData>()

        fun repo() =
            ImportRepository(
                trackExists = { routeId -> tracks.containsKey(routeId) },
                insertTrack = { t -> tracks[t.routeId] = t },
                insertPoints = { ps -> points += ps },
            )
    }

    private val current = CurrentAccount(accountId = "acct-A", tenant = "DEMO-TENANT")

    @Test
    fun `export then import restores the track and its points`() =
        runTest {
            val track = sampleTrack()
            val points = listOf(samplePoint(date = 1_500L), samplePoint(date = 1_600L, lat = 12.96))
            val json = JsonExporter.export(track, points, emptyList())

            val store = FakeStore()
            val result = store.repo().import(json, current)

            assertEquals(ImportResult.Restored(routeId = "route-1", pointCount = 2), result)
            val restored = store.tracks.getValue("route-1")
            assertEquals("Morning Commute", restored.name)
            assertEquals(4200.0, restored.distance)
            assertEquals(900_000L, restored.duration)
            assertEquals(12.9, restored.startLatitude)
            assertEquals("CAR", restored.selectedVehicleType)
            assertTrue(restored.isCompleted)
            assertEquals(2, store.points.size)
            assertEquals("route-1", store.points[0].token)
            assertEquals(1_500L, store.points[0].date)
            assertEquals(12.96, store.points[1].lat)
            assertTrue(store.points[0].wasCheckInPoint)
        }

    @Test
    fun `import rejects an export from a different account`() =
        runTest {
            val foreign = sampleTrack(accountId = "acct-B", tenant = "OTHER-TENANT")
            val json = JsonExporter.export(foreign, listOf(samplePoint()), emptyList())

            val store = FakeStore()
            val result = store.repo().import(json, current)

            assertTrue(result is ImportResult.AccountMismatch, "expected mismatch, got $result")
            assertEquals("acct-B", (result as ImportResult.AccountMismatch).exportAccountId)
            assertEquals(0, store.tracks.size)
            assertEquals(0, store.points.size)
        }

    @Test
    fun `import skips a track that already exists`() =
        runTest {
            val json = JsonExporter.export(sampleTrack(), listOf(samplePoint()), emptyList())
            val store = FakeStore()
            store.tracks["route-1"] = sampleTrack() // pre-existing

            val result = store.repo().import(json, current)

            assertEquals(ImportResult.Skipped("route-1"), result)
            assertEquals(0, store.points.size, "no points written on a skip")
        }

    @Test
    fun `excel export produces well-formed spreadsheetml with key cells`() {
        val track = sampleTrack()
        val points = listOf(samplePoint(), samplePoint(date = 1_700L))
        val xml = ExcelExporter.export(track, points, emptyList())

        assertTrue(xml.startsWith("<?xml"), "should be XML")
        assertTrue(xml.contains("<Workbook"), "should have a Workbook root")
        assertTrue(xml.contains("""ss:Name="Summary""""), "should have a Summary sheet")
        assertTrue(xml.contains("""ss:Name="Points""""), "should have a Points sheet")
        assertTrue(xml.contains("Morning Commute"), "track name cell present")
        assertTrue(xml.contains("""<Data ss:Type="Number">4200.0</Data>"""), "distance cell present")
        assertTrue(xml.trimEnd().endsWith("</Workbook>"), "document closed")
        // one header row + two data rows on the Points sheet -> 3 <Row> after the Points marker.
        val pointsSection = xml.substringAfter("""ss:Name="Points"""")
        assertEquals(3, pointsSection.split("<Row>").size - 1, "header + 2 point rows")
    }
}
