package com.mileway.core.data.ledger

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApprovalTransitionsTest {
    @Test
    fun `pending to approved is allowed`() = assertTrue(ApprovalTransitions.isAllowed(ApprovalStatus.PENDING, ApprovalStatus.APPROVED))

    @Test
    fun `pending to rejected is allowed`() = assertTrue(ApprovalTransitions.isAllowed(ApprovalStatus.PENDING, ApprovalStatus.REJECTED))

    @Test
    fun `approved to paid is allowed`() = assertTrue(ApprovalTransitions.isAllowed(ApprovalStatus.APPROVED, ApprovalStatus.PAID))

    @Test
    fun `approved to rejected is allowed`() = assertTrue(ApprovalTransitions.isAllowed(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED))

    @Test
    fun `pending to paid is illegal`() = assertFalse(ApprovalTransitions.isAllowed(ApprovalStatus.PENDING, ApprovalStatus.PAID))

    @Test
    fun `paid is terminal`() {
        assertTrue(ApprovalTransitions.allowed(ApprovalStatus.PAID).isEmpty())
        assertFalse(ApprovalTransitions.isAllowed(ApprovalStatus.PAID, ApprovalStatus.APPROVED))
    }

    @Test
    fun `rejected is terminal`() {
        assertTrue(ApprovalTransitions.allowed(ApprovalStatus.REJECTED).isEmpty())
        assertFalse(ApprovalTransitions.isAllowed(ApprovalStatus.REJECTED, ApprovalStatus.PENDING))
    }
}
