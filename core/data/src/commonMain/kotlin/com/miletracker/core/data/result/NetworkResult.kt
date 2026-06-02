package com.miletracker.core.data.result

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()

    data class Error(
        val exception: Throwable,
        val code: Int = -1,
        val errorBody: String? = null,
    ) : NetworkResult<Nothing>() {
        val message: String get() = errorBody ?: exception.message ?: "Unknown error occurred"
    }

    object Loading : NetworkResult<Nothing>()
}
