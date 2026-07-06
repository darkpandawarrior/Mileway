package com.mileway.core.network.netlog

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkLogStoreTest {
    @Test
    fun `record prepends newest entry and emits on the flow`() {
        val store = NetworkLogStore(capacity = 200)
        store.record(NetworkLogEntry(method = "GET", url = "https://a"))
        store.record(NetworkLogEntry(method = "GET", url = "https://b"))

        val entries = store.entries.value
        assertEquals(2, entries.size)
        assertEquals("https://b", entries.first().url)
    }

    @Test
    fun `ring buffer drops oldest past capacity`() {
        val store = NetworkLogStore(capacity = 3)
        repeat(5) { i -> store.record(NetworkLogEntry(method = "GET", url = "https://$i")) }

        val entries = store.entries.value
        assertEquals(3, entries.size)
        // Newest first; oldest (0, 1) dropped.
        assertEquals(listOf("https://4", "https://3", "https://2"), entries.map { it.url })
    }

    @Test
    fun `clear empties the buffer`() {
        val store = NetworkLogStore()
        store.record(NetworkLogEntry(method = "GET", url = "https://a"))
        store.clear()
        assertEquals(emptyList(), store.entries.value)
    }
}
