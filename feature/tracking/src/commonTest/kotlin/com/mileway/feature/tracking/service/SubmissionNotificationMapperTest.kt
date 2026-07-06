package com.mileway.feature.tracking.service

import com.mileway.core.data.model.network.PolicyViolation
import com.mileway.core.data.model.network.ViolationSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Wave-3 notification depth: SubmissionNotificationMapper score bands, violation priority, summary (parity §3). */
class SubmissionNotificationMapperTest {
    // ── Quality-score content across bands ───────────────────────────────────

    @Test
    fun `excellent band for score 95`() {
        val content = SubmissionNotificationMapper.qualityContent(95)
        assertEquals("Great trip data", content.title)
        assertTrue(content.body.contains("95/100"))
    }

    @Test
    fun `good band for score 80`() {
        val content = SubmissionNotificationMapper.qualityContent(80)
        assertEquals("Great trip data", content.title)
    }

    @Test
    fun `acceptable band for score 65`() {
        val content = SubmissionNotificationMapper.qualityContent(65)
        assertEquals("Trip recorded", content.title)
    }

    @Test
    fun `fair band for score 45`() {
        val content = SubmissionNotificationMapper.qualityContent(45)
        assertEquals("Trip data needs a look", content.title)
    }

    @Test
    fun `poor band for score 25`() {
        val content = SubmissionNotificationMapper.qualityContent(25)
        assertEquals("Trip data needs a look", content.title)
    }

    @Test
    fun `very poor band for score 5`() {
        val content = SubmissionNotificationMapper.qualityContent(5)
        assertEquals("Trip quality issue", content.title)
        assertTrue(content.body.contains("5/100"))
    }

    // ── Violation alerts ordered by priority ─────────────────────────────────

    @Test
    fun `no violations returns null`() {
        assertNull(SubmissionNotificationMapper.violationContent(emptyList()))
    }

    @Test
    fun `single violation picks that violation as the Fix-Issue action`() {
        val v = PolicyViolation(id = "v1", title = "Late submission", message = "Submit within 7 days", severity = ViolationSeverity.VIOLATION)
        val (content, fixIssue) = SubmissionNotificationMapper.violationContent(listOf(v))!!
        assertEquals("Policy violation", content.title)
        assertEquals("v1", fixIssue.violationId)
        assertEquals("Fix Issue", fixIssue.label)
    }

    @Test
    fun `hardstop violation is titled as blocked and outranks a plain violation`() {
        val reimbursable = PolicyViolation(id = "r1", title = "Minor note", severity = ViolationSeverity.REIMBURSABLE)
        val violation = PolicyViolation(id = "v1", title = "Missing receipt", severity = ViolationSeverity.VIOLATION)
        val hardstop = PolicyViolation(id = "h1", title = "Exceeds monthly cap", severity = ViolationSeverity.HARDSTOP)

        val (content, fixIssue) = SubmissionNotificationMapper.violationContent(listOf(reimbursable, violation, hardstop))!!

        assertEquals("Submission blocked", content.title)
        assertEquals("h1", fixIssue.violationId, "highest-severity violation must win the Fix-Issue action")
    }

    @Test
    fun `multiple violations summarize the count beyond the top one`() {
        val a = PolicyViolation(id = "a", title = "A", severity = ViolationSeverity.VIOLATION)
        val b = PolicyViolation(id = "b", title = "B", severity = ViolationSeverity.VIOLATION)
        val c = PolicyViolation(id = "c", title = "C", severity = ViolationSeverity.HARDSTOP)

        val (content, fixIssue) = SubmissionNotificationMapper.violationContent(listOf(a, b, c))!!

        assertEquals("c", fixIssue.violationId)
        assertTrue(content.body.contains("2 more issues"))
    }

    // ── Completion summary ────────────────────────────────────────────────────

    @Test
    fun `completion summary includes distance, reimbursement and quality band`() {
        val content = SubmissionNotificationMapper.completionSummary(distanceKm = 12.5, reimbursableAmount = 250.0, score = 92)
        assertEquals("Trip submitted", content.title)
        assertTrue(content.body.contains("12.50 km"))
        assertTrue(content.body.contains("₹250"))
        assertTrue(content.body.contains("excellent"))
    }
}
