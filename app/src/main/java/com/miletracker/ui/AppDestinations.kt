package com.miletracker.ui

/**
 * Top-level nav-graph routes for the bottom-navigation tabs. The tab set mirrors the
 * source app's information architecture: Travel, Spends, Payables, centre logo (home),
 * Approvals, Account. Functional tabs map to feature-module nested graphs; the rest are
 * illustrative placeholders until Phase 6 builds them out (see [MileTrackerAppRoot]).
 */
object AppGraph {
    /** Centre logo action — hosts the tracking feature until the home screen lands (Phase 6). */
    const val TRACK = "track_graph"

    /** Spends tab — manual mileage logging / expenses. */
    const val LOG = "log_graph"

    /** Media capture graph. No longer a top-level tab; entered from logging/submission flows. */
    const val MEDIA = "media_graph"

    /** Account tab — profile feature. */
    const val PROFILE = "profile_graph"

    /** Illustrative shell tabs (placeholder destinations). */
    const val TRAVEL = "travel_graph"
    const val PAYABLES = "payables_graph"
    const val APPROVALS = "approvals_graph"
}

/**
 * Top-level destinations that live outside the bottom-nav nested graphs.
 */
object AppRoutes {
    const val DEBUG_MENU = "debug_menu"
}
