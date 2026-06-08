package com.miletracker.core.ui.mvi

/** Sealed state for any screen that loads data. Drives the UI without boolean soup. */
sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>

    data object Empty : ScreenState<Nothing>

    data object NoNetwork : ScreenState<Nothing>

    data class Error(val message: String, val cause: Throwable? = null) : ScreenState<Nothing>

    data class Content<T>(val data: T) : ScreenState<T>
}

val <T> ScreenState<T>.dataOrNull: T?
    get() = (this as? ScreenState.Content)?.data

val ScreenState<*>.isLoading: Boolean
    get() = this is ScreenState.Loading
