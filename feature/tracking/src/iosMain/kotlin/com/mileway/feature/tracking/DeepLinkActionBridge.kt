package com.mileway.feature.tracking

import com.mileway.core.common.deeplink.DeepLinkAction
import com.mileway.core.common.deeplink.DeepLinkActionDispatcher
import com.mileway.core.common.deeplink.DeepLinkValidator
import com.mileway.feature.tracking.watch.WatchFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

/**
 * DL.5: Swift → KMP bridge for the tracking control-op URL scheme
 * (`mileway://track/{start,stop,pause,discard}`), exported in the Mileway framework. Mirrors
 * `core:ui`'s `DeepLinkBridge` (DL.3, navigation-target links) but dispatches straight to
 * [WatchFacade] instead of routing to a screen — there is no confirmation UI on this path yet
 * (App Intents can prompt on the OS side before invoking `handle`), so [handle] runs the
 * confirmation-required actions (Stop/Discard) exactly like the non-destructive ones. Full parity
 * with Android's confirmation-sheet gate is a documented follow-up (see .ralph/PROGRESS.md), not
 * attempted here since it would mean standing up SwiftUI presentation from a KMP framework export
 * — disproportionate for this pass.
 *
 * `iOSApp.swift`'s `.onOpenURL` calls `DeepLinkActionBridge.shared.handle(url:)` for `track/<verb>`
 * URLs; `CheckIn` and unrecognised links are ignored here (checkin is a nav destination — see
 * `DeepLinkBridge` — and App Intents for it are a documented stub, not full headless wiring).
 */
object DeepLinkActionBridge {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun handle(url: String) {
        if (!DeepLinkValidator.isAllowed(url)) return
        val action = DeepLinkActionDispatcher.resolve(url)
        val facade = KoinPlatform.getKoin().getOrNull<WatchFacade>() ?: return
        scope.launch {
            when (action) {
                DeepLinkAction.Start -> facade.startTracking()
                DeepLinkAction.Stop -> facade.stopTracking()
                DeepLinkAction.Pause -> facade.pauseTracking()
                DeepLinkAction.Discard -> facade.discardTracking()
                DeepLinkAction.CheckIn, is DeepLinkAction.Unknown -> Unit
            }
        }
    }
}
