package com.miletracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.miletracker.ui.toast.AppToast
import ke.don.koffee.annotations.ExperimentalKoffeeApi
import ke.don.koffee.domain.rememberToastHostState
import ke.don.koffee.model.KoffeeDefaults
import ke.don.koffee.model.ToastAnimation
import ke.don.koffee.model.ToastPosition
import ke.don.koffee.ui.KoffeeBar

class KoffeeHostController(private val onReattach: () -> Unit) {
    var isChildActive = false
    fun reattach() = onReattach()
}

val LocalKoffeeHostController = staticCompositionLocalOf<KoffeeHostController?> { null }

@OptIn(ExperimentalKoffeeApi::class)
@Composable
fun KoffeeHost(content: @Composable () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var koffeeKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var attachmentKey by remember { mutableIntStateOf(0) }

    val controller = remember { KoffeeHostController(onReattach = { attachmentKey++ }) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !controller.isChildActive) {
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

    val hostState = rememberToastHostState(
        maxVisibleToasts = config.maxVisibleToasts,
        durationResolver = config.durationResolver,
    )

    CompositionLocalProvider(LocalKoffeeHostController provides controller) {
        Box(modifier = Modifier.fillMaxSize().zIndex(1000f)) {
            content()
            key(koffeeKey, attachmentKey) {
                KoffeeBar(config = config, hostState = hostState) {}
            }
        }
    }
}
