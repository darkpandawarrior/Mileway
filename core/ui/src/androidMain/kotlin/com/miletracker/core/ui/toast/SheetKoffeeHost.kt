package com.miletracker.core.ui.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ke.don.koffee.annotations.ExperimentalKoffeeApi
import ke.don.koffee.domain.rememberToastHostState
import ke.don.koffee.model.KoffeeDefaults
import ke.don.koffee.model.ToastAnimation
import ke.don.koffee.model.ToastPosition
import ke.don.koffee.ui.KoffeeBar

/**
 * Specialized KoffeeHost for ModalBottomSheet content.
 *
 * ModalBottomSheet renders in a separate Popup composition — outside the main KoffeeHost
 * tree. Without this wrapper, Koffee toasts triggered inside a sheet either don't appear
 * or render behind the modal scrim. This wrapper:
 *
 * 1. Spins up a local KoffeeBar inside the sheet's composition subtree.
 * 2. Uses a custom Layout so KoffeeBar doesn't inflate the sheet's measured height.
 * 3. Coordinates with the parent KoffeeHostController so only one host is active at a time,
 *    preventing duplicate toasts.
 * 4. Re-keys the KoffeeBar on ON_RESUME to survive camera/media activity returns.
 *
 * @param maxHeight Optional cap on sheet content height (e.g. 600.dp for date pickers).
 * @param zIndex Z-index for the toast overlay. Default 1000f clears the modal scrim.
 */
@OptIn(ExperimentalKoffeeApi::class)
@Composable
fun SheetKoffeeHost(
    maxHeight: Dp? = null,
    zIndex: Float = 1000f,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val parentController = LocalKoffeeHostController.current

    var koffeeKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var attachmentKey by remember { mutableIntStateOf(0) }

    val myController = remember { KoffeeHostController(onReattach = { attachmentKey++ }) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !myController.isChildActive) {
                koffeeKey = System.currentTimeMillis()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val config = remember {
        KoffeeDefaults.config.copy(
            layout = { AppToast(it) },
            dismissible = true,
            maxVisibleToasts = 3,
            position = ToastPosition.BottomCenter,
            animationStyle = ToastAnimation.SlideDown,
        )
    }

    val myHostState = rememberToastHostState(
        maxVisibleToasts = config.maxVisibleToasts,
        durationResolver = config.durationResolver,
    )

    DisposableEffect(Unit) {
        parentController?.isChildActive = true
        onDispose {
            parentController?.isChildActive = false
            parentController?.reattach()
        }
    }

    // Custom Layout: measure content at intrinsic height; KoffeeBar overlays at the same
    // size so BottomCenter toasts land at the bottom of the visible content, not the screen.
    // Content is kept OUTSIDE the keyed block to prevent re-measuring focused TextFields on
    // lifecycle events (avoids MagnifierNode crashes in Compose).
    CompositionLocalProvider(LocalKoffeeHostController provides myController) {
        Layout(
            modifier = Modifier.zIndex(zIndex),
            content = {
                content()

                key(koffeeKey, attachmentKey) {
                    KoffeeBar(config = config, hostState = myHostState) {}
                }
            }
        ) { measurables, constraints ->
            val contentMeasurable = measurables[0]
            val koffeeMeasurable = measurables.getOrNull(1)

            val effectiveMaxHeight = if (maxHeight != null) {
                minOf(constraints.maxHeight, maxHeight.roundToPx())
            } else {
                constraints.maxHeight
            }

            val measureConstraints = constraints.copy(
                minHeight = 0,
                maxHeight = effectiveMaxHeight.coerceAtMost(1 shl 22)
            )

            val contentPlaceable = contentMeasurable.measure(measureConstraints)

            val koffeePlaceable = koffeeMeasurable?.measure(
                Constraints.fixed(contentPlaceable.width, contentPlaceable.height)
            )

            layout(contentPlaceable.width, contentPlaceable.height) {
                contentPlaceable.place(0, 0)
                koffeePlaceable?.place(0, 0)
            }
        }
    }
}
