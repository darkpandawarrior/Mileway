package com.miletracker.core.security

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RootDetectorTest {

    @Test
    fun `check returns a non-null result`() {
        val result = RootDetector.check()
        assertNotNull(result)
    }

    @Test
    fun `isRooted reflects whether signals list is non-empty`() {
        val result = RootDetector.check()
        // Structural invariant: isRooted must be true iff signals is non-empty
        if (result.signals.isEmpty()) {
            assertFalse(result.isRooted, "isRooted must be false when no signals detected")
        } else {
            assertTrue(result.isRooted, "isRooted must be true when signals are present")
        }
    }

    @Test
    fun `signals list is accessible and typed`() {
        val result = RootDetector.check()
        // Should be a List<String> — check that all entries are strings (always true at compile
        // time, but documents the contract for the test reader)
        result.signals.forEach { signal ->
            assertTrue(signal.isNotBlank(), "Each signal should have descriptive text")
        }
    }

    @Test
    fun `clean JVM test environment reports not rooted`() {
        val result = RootDetector.check()
        // In a standard CI or developer JVM environment (no su, no magisk, no test-keys):
        // - /system/bin/su etc. don't exist → isSuBinaryPresent = false
        // - Build.TAGS is null in JVM unit tests → isTestKeysBuild = false
        // - /sbin/.magisk etc. don't exist → isMagiskPresent = false
        assertFalse(result.isRooted, "Standard test environment should not be flagged as rooted")
        assertTrue(result.signals.isEmpty(), "Signals should be empty in clean test environment")
    }
}
