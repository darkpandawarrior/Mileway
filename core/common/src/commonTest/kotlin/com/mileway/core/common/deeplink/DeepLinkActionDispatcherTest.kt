package com.mileway.core.common.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DL.5: the tracking control-op deep-link dispatcher + its confirmation policy. */
class DeepLinkActionDispatcherTest {
    @Test
    fun `each control verb resolves to its action`() {
        assertEquals(DeepLinkAction.Start, DeepLinkActionDispatcher.resolve("mileway://track/start"))
        assertEquals(DeepLinkAction.Stop, DeepLinkActionDispatcher.resolve("mileway://track/stop"))
        assertEquals(DeepLinkAction.Pause, DeepLinkActionDispatcher.resolve("mileway://track/pause"))
        assertEquals(DeepLinkAction.Discard, DeepLinkActionDispatcher.resolve("mileway://track/discard"))
        assertEquals(DeepLinkAction.CheckIn, DeepLinkActionDispatcher.resolve("mileway://track/checkin"))
    }

    @Test
    fun `https app-link form resolves the same way`() {
        assertEquals(
            DeepLinkAction.Stop,
            DeepLinkActionDispatcher.resolve("https://mileway.example.com/track/stop"),
        )
    }

    @Test
    fun `unknown or malformed links resolve to a safe Unknown no-op`() {
        assertTrue(DeepLinkActionDispatcher.resolve("mileway://track") is DeepLinkAction.Unknown)
        assertTrue(DeepLinkActionDispatcher.resolve("mileway://track/fly") is DeepLinkAction.Unknown)
        assertTrue(DeepLinkActionDispatcher.resolve("mileway://log/start") is DeepLinkAction.Unknown)
        assertTrue(DeepLinkActionDispatcher.resolve("not a uri") is DeepLinkAction.Unknown)
    }

    @Test
    fun `destructive actions require confirmation, the rest do not`() {
        assertTrue(DeepLinkActionDispatcher.requiresConfirmation(DeepLinkAction.Stop))
        assertTrue(DeepLinkActionDispatcher.requiresConfirmation(DeepLinkAction.Discard))
        assertFalse(DeepLinkActionDispatcher.requiresConfirmation(DeepLinkAction.Start))
        assertFalse(DeepLinkActionDispatcher.requiresConfirmation(DeepLinkAction.Pause))
        assertFalse(DeepLinkActionDispatcher.requiresConfirmation(DeepLinkAction.CheckIn))
    }
}
