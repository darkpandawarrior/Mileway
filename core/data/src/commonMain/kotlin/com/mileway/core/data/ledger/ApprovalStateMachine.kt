package com.mileway.core.data.ledger

/**
 * Approval lifecycle states (parity §3 Wave 4). Lives in `core:data` (alongside
 * [com.mileway.core.data.ledger.PolicyRateEngine], no `core:ledger` module exists) rather than
 * `feature:approvals`, so it stays a pure, feature-agnostic FSM other approval-adjacent features
 * could reuse without depending on the approvals UI module.
 */
enum class ApprovalStatus { PENDING, APPROVED, PAID, REJECTED }

/**
 * Pure approval status-lifecycle graph, mirroring [com.mileway.feature.tracking.repository.VoucherTransitions]'s
 * `from -> allowed-next` idiom exactly.
 *
 * Graph: `PENDING -> {APPROVED, REJECTED}`, `APPROVED -> {PAID, REJECTED}`. [ApprovalStatus.PAID]
 * and [ApprovalStatus.REJECTED] are terminal (no outbound edges).
 */
object ApprovalTransitions {
    private val graph: Map<ApprovalStatus, Set<ApprovalStatus>> =
        mapOf(
            ApprovalStatus.PENDING to setOf(ApprovalStatus.APPROVED, ApprovalStatus.REJECTED),
            ApprovalStatus.APPROVED to setOf(ApprovalStatus.PAID, ApprovalStatus.REJECTED),
            ApprovalStatus.PAID to emptySet(),
            ApprovalStatus.REJECTED to emptySet(),
        )

    /** The set of statuses [from] may legally move to next. Empty for terminal statuses. */
    fun allowed(from: ApprovalStatus): Set<ApprovalStatus> = graph.getValue(from)

    /** Whether moving from [from] directly to [to] is a legal single-step transition. */
    fun isAllowed(
        from: ApprovalStatus,
        to: ApprovalStatus,
    ): Boolean = to in allowed(from)
}
