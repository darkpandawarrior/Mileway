package com.mileway.feature.tracking.model

import com.mileway.core.data.model.db.SavedTrack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun track(
    claimedByVoucherNumber: String? = null,
    tripId: String? = null,
    tripV2Id: String? = null,
    itineraryId: String? = null,
    pettyId: Long = -1L,
) = SavedTrack(
    routeId = "route-1",
    name = "Trip",
    startLatitude = 0.0,
    startLongitude = 0.0,
    endLatitude = 0.0,
    endLongitude = 0.0,
    pausedLatitude = 0.0,
    pausedLongitude = 0.0,
    startTime = 0L,
    endTime = 0L,
    distance = 0.0,
    duration = 0L,
    claimedByVoucherNumber = claimedByVoucherNumber,
    tripId = tripId,
    tripV2Id = tripV2Id,
    itineraryId = itineraryId,
    pettyId = pettyId,
)

class LinkedContextTest {
    @Test
    fun `voucher number maps to Voucher context`() {
        val context = track(claimedByVoucherNumber = "V-123").toLinkedContext()
        assertEquals(LinkedContextKind.Voucher("V-123"), context?.kind)
        assertEquals("V-123", context?.value)
    }

    @Test
    fun `no linkage fields maps to null`() {
        assertNull(track().toLinkedContext())
    }

    @Test
    fun `tripId maps to Trip context`() {
        val context = track(tripId = "T-1").toLinkedContext()
        assertEquals(LinkedContextKind.Trip("T-1"), context?.kind)
    }

    @Test
    fun `tripV2Id is used when tripId is absent`() {
        val context = track(tripV2Id = "T2-1").toLinkedContext()
        assertEquals(LinkedContextKind.Trip("T2-1"), context?.kind)
    }

    @Test
    fun `itineraryId maps to Booking context`() {
        val context = track(itineraryId = "I-1").toLinkedContext()
        assertEquals(LinkedContextKind.Booking("I-1"), context?.kind)
    }

    @Test
    fun `non-negative pettyId maps to Event context`() {
        val context = track(pettyId = 42L).toLinkedContext()
        assertEquals(LinkedContextKind.Event("42"), context?.kind)
        assertEquals("PC-42", context?.value)
    }

    @Test
    fun `blank linkage strings are treated as absent`() {
        assertNull(track(tripId = "", itineraryId = "  ").toLinkedContext())
    }

    @Test
    fun `voucher takes precedence over trip, booking, and event`() {
        val context =
            track(
                claimedByVoucherNumber = "V-1",
                tripId = "T-1",
                itineraryId = "I-1",
                pettyId = 1L,
            ).toLinkedContext()
        assertEquals(LinkedContextKind.Voucher("V-1"), context?.kind)
    }

    @Test
    fun `trip takes precedence over booking and event when no voucher`() {
        val context = track(tripId = "T-1", itineraryId = "I-1", pettyId = 1L).toLinkedContext()
        assertEquals(LinkedContextKind.Trip("T-1"), context?.kind)
    }

    @Test
    fun `booking takes precedence over event when no voucher or trip`() {
        val context = track(itineraryId = "I-1", pettyId = 1L).toLinkedContext()
        assertEquals(LinkedContextKind.Booking("I-1"), context?.kind)
    }
}
