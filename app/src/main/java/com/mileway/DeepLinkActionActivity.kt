package com.mileway

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mileway.core.common.deeplink.DeepLinkAction
import com.mileway.core.common.deeplink.DeepLinkActionDispatcher
import com.mileway.core.common.deeplink.DeepLinkValidator
import com.mileway.core.ui.theme.MilewayTheme
import com.mileway.core.ui.theme.ThemeController
import com.mileway.feature.tracking.ui.components.DeepLinkConfirmationDialog
import com.mileway.feature.tracking.watch.WatchFacade
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Result extras returned to the caller alongside [Activity.RESULT_OK]/[Activity.RESULT_CANCELED]. */
private const val EXTRA_ACTION = "com.mileway.extra.DEEP_LINK_ACTION"

/**
 * DL.5: entry point for the tracking control-op deep links / App Intents
 * (`mileway://track/{start,stop,pause,discard,checkin}`) — partner integrations and OS
 * Shortcuts/automation launch this directly rather than the full [LauncherActivity] shell, so a
 * caller gets a result back without ever seeing the app UI (except the confirmation sheet for a
 * destructive action).
 *
 * `CheckIn` is not a `TrackingController` op (it's a whole guided flow — see `GeoCheckInScreen`/
 * `ManualCheckInScreen`), so it hands off to [LauncherActivity] with the existing
 * `mileway://track/checkin` nav route instead of trying to complete it headlessly here.
 */
class DeepLinkActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val raw = intent?.data?.toString()
        val action =
            if (raw != null && DeepLinkValidator.isAllowed(raw)) {
                DeepLinkActionDispatcher.resolve(raw)
            } else {
                DeepLinkAction.Unknown(raw.orEmpty())
            }

        if (action is DeepLinkAction.Unknown) {
            finishWithResult(ok = false, action = action)
            return
        }
        if (action == DeepLinkAction.CheckIn) {
            // Sub-flow, not a headless op: forward to the normal app shell's checkin destination.
            startActivity(Intent(this, LauncherActivity::class.java).setData(intent.data))
            finishWithResult(ok = true, action = action)
            return
        }

        setContent { DeepLinkActionEntry(action = action, onFinished = { ok -> finishWithResult(ok, action) }) }
    }

    private fun finishWithResult(
        ok: Boolean,
        action: DeepLinkAction,
    ) {
        setResult(
            if (ok) Activity.RESULT_OK else Activity.RESULT_CANCELED,
            Intent().putExtra(EXTRA_ACTION, action.toString()),
        )
        finish()
    }
}

@Composable
private fun DeepLinkActionEntry(
    action: DeepLinkAction,
    onFinished: (ok: Boolean) -> Unit,
    themeController: ThemeController = koinInject(),
    watchFacade: WatchFacade = koinInject(),
) {
    val systemDark = isSystemInDarkTheme()
    val override by themeController.darkThemeOverride.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var awaitingConfirmation by remember { mutableStateOf(DeepLinkActionDispatcher.requiresConfirmation(action)) }

    MilewayTheme(darkTheme = override ?: systemDark) {
        if (awaitingConfirmation) {
            DeepLinkConfirmationDialog(
                action = action,
                onConfirm = {
                    awaitingConfirmation = false
                    scope.launch {
                        action.dispatchTo(watchFacade)
                        onFinished(true)
                    }
                },
                onDismiss = { onFinished(false) },
            )
        } else {
            // Non-destructive: dispatch immediately, no UI shown.
            LaunchedEffect(action) {
                action.dispatchTo(watchFacade)
                onFinished(true)
            }
        }
    }
}

/** Routes the resolved [DeepLinkAction] to its [WatchFacade] op — the same seam the QS tile uses. */
private suspend fun DeepLinkAction.dispatchTo(watchFacade: WatchFacade) {
    when (this) {
        DeepLinkAction.Start -> watchFacade.startTracking()
        DeepLinkAction.Stop -> watchFacade.stopTracking()
        DeepLinkAction.Pause -> watchFacade.pauseTracking()
        DeepLinkAction.Discard -> watchFacade.discardTracking()
        DeepLinkAction.CheckIn, is DeepLinkAction.Unknown -> Unit
    }
}
