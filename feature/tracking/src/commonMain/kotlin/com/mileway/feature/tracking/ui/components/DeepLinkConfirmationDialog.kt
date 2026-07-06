package com.mileway.feature.tracking.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import com.mileway.core.common.deeplink.DeepLinkAction
import com.mileway.core.ui.components.sheet.ActionConfirmationBottomSheet
import com.mileway.core.ui.components.sheet.ActionConfirmationToneType

/**
 * DL.5: confirmation gate for a destructive [DeepLinkAction] arriving from an external caller
 * (partner integration / Shortcuts / iOS App Intents) — shown before [DeepLinkAction.Stop] or
 * [DeepLinkAction.Discard] is actually dispatched to `TrackingController`. Non-destructive actions
 * (Start/Pause/CheckIn) never reach this composable; see
 * `DeepLinkActionDispatcher.requiresConfirmation`.
 *
 * Reuses [ActionConfirmationBottomSheet] (the project's confirmation idiom — see
 * [DiscardJourneyDialog]) rather than introducing a new dialog shape per action.
 */
@Composable
fun DeepLinkConfirmationDialog(
    action: DeepLinkAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (action) {
        DeepLinkAction.Stop ->
            ActionConfirmationBottomSheet(
                title = "Stop Tracking?",
                description = "This ends the active trip and finalizes the distance recorded so far.",
                confirmLabel = "Stop",
                dismissLabel = "Cancel",
                icon = Icons.Filled.Stop,
                tone = ActionConfirmationToneType.Danger,
                onConfirm = { onConfirm() },
                onDismiss = onDismiss,
            )
        DeepLinkAction.Discard ->
            ActionConfirmationBottomSheet(
                title = "Discard Journey?",
                description = "All tracking data for this journey will be lost. This action cannot be undone.",
                confirmLabel = "Discard",
                dismissLabel = "Cancel",
                icon = Icons.Filled.Delete,
                tone = ActionConfirmationToneType.Danger,
                onConfirm = { onConfirm() },
                onDismiss = onDismiss,
            )
        else -> Unit // ponytail: only destructive actions ever route here (see requiresConfirmation).
    }
}
