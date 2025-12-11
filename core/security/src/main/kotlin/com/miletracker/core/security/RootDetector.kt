package com.miletracker.core.security

import android.os.Build

object RootDetector {

    data class RootCheckResult(
        val isRooted: Boolean,
        val signals: List<String>
    )

    fun check(): RootCheckResult {
        val signals = mutableListOf<String>()
        if (isSuBinaryPresent()) signals += "su binary found"
        if (isTestKeysBuild()) signals += "test-keys build"
        if (isMagiskPresent()) signals += "Magisk detected"
        return RootCheckResult(isRooted = signals.isNotEmpty(), signals = signals)
    }

    private fun isSuBinaryPresent(): Boolean {
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/su", "/data/local/bin/su", "/data/local/xbin/su"
        )
        return paths.any { java.io.File(it).exists() }
    }

    private fun isTestKeysBuild(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    private fun isMagiskPresent(): Boolean =
        java.io.File("/sbin/.magisk").exists() ||
        java.io.File("/data/adb/magisk").exists()
}
