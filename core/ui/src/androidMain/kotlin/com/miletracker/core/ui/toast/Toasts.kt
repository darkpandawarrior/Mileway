package com.miletracker.core.ui.toast

import android.util.Log
import ke.don.koffee.domain.Koffee
import ke.don.koffee.model.ToastAction
import ke.don.koffee.model.ToastType

object Toasts {
    private const val TAG = "Toasts"

    enum class ToastScenario { Gps, Battery, Permissions, Success, Error, Info, Warning }

    private fun emojiFor(type: ToastType): String = when (type) {
        ToastType.Success -> "✅"
        ToastType.Error -> "❌"
        ToastType.Info -> "ℹ️"
        ToastType.Warning -> "⚠️"
        else -> "🔔"
    }

    private fun typeFor(scenario: ToastScenario): ToastType = when (scenario) {
        ToastScenario.Gps -> ToastType.Warning
        ToastScenario.Battery -> ToastType.Warning
        ToastScenario.Permissions -> ToastType.Error
        ToastScenario.Success -> ToastType.Success
        ToastScenario.Error -> ToastType.Error
        ToastScenario.Info -> ToastType.Info
        ToastScenario.Warning -> ToastType.Warning
    }

    fun show(
        scenario: ToastScenario,
        title: String,
        description: String,
        primaryAction: ToastAction? = null,
        secondaryAction: ToastAction? = null,
    ) {
        show(
            title = title,
            description = description,
            type = typeFor(scenario),
            primaryAction = primaryAction,
            secondaryAction = secondaryAction,
        )
    }

    fun show(
        title: String,
        description: String,
        type: ToastType = ToastType.Info,
        primaryAction: ToastAction? = null,
        secondaryAction: ToastAction? = null,
    ) {
        val finalTitle = "${emojiFor(type)}  $title"
        val dismiss = secondaryAction ?: ToastAction("Dismiss", onClick = { })
        try {
            Koffee.show(
                title = finalTitle,
                description = description,
                type = type,
                primaryAction = primaryAction,
                secondaryAction = dismiss,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Toast failed: $title", e)
        }
    }
}
