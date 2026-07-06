package com.mileway.core.network.netlog

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkLogEntryTest {
    @Test
    fun `toCurl renders plain GET`() {
        val entry = NetworkLogEntry(method = "GET", url = "https://example.com/api")
        assertEquals("curl -X GET 'https://example.com/api'", entry.toCurl())
    }

    @Test
    fun `toCurl renders POST with body`() {
        val entry =
            NetworkLogEntry(
                method = "POST",
                url = "https://example.com/api",
                requestBody = """{"a":1}""",
            )
        assertEquals("""curl -X POST -d '{"a":1}' 'https://example.com/api'""", entry.toCurl())
    }

    @Test
    fun `toCurl renders headers`() {
        val entry =
            NetworkLogEntry(
                method = "GET",
                url = "https://example.com/api",
                requestHeaders = linkedMapOf("Authorization" to "Bearer xyz"),
            )
        assertEquals(
            "curl -X GET -H 'Authorization: Bearer xyz' 'https://example.com/api'",
            entry.toCurl(),
        )
    }
}
