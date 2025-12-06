package com.miletracker.core.ui.toast

import androidx.compose.runtime.staticCompositionLocalOf

class KoffeeHostController(private val onReattach: () -> Unit) {
    var isChildActive = false
    fun reattach() = onReattach()
}

val LocalKoffeeHostController = staticCompositionLocalOf<KoffeeHostController?> { null }
