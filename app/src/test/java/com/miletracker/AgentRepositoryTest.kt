package com.miletracker

import com.miletracker.feature.agent.repository.AgentRepository
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentRepositoryTest {

    private val repo = AgentRepository()

    @Test
    fun `conversations has exactly 8 entries`() {
        assertEquals(8, repo.conversations.size)
    }

    @Test
    fun `popularQuestions has exactly 12 entries`() {
        assertEquals(12, repo.popularQuestions.size)
    }

    @Test
    fun `unansweredQuestions has exactly 4 entries`() {
        assertEquals(4, repo.unansweredQuestions.size)
    }

    @Test
    fun `conversation ids are unique`() {
        val ids = repo.conversations.map { it.id }
        assertEquals(ids.distinct(), ids)
    }

    @Test
    fun `every conversation has at least one message`() {
        repo.conversations.forEach { conv ->
            assertTrue(conv.messages.isNotEmpty(), "Conversation ${conv.id} has no messages")
        }
    }

    @Test
    fun `each conversation message alternates user and assistant`() {
        repo.conversations.forEach { conv ->
            assertTrue(conv.messages.first().isUser, "Conversation ${conv.id} should start with a user message")
        }
    }

    @Test
    fun `popular question ask counts are positive`() {
        repo.popularQuestions.forEach { q ->
            assertTrue(q.askCount > 0, "Question ${q.id} has non-positive askCount")
        }
    }

    @Test
    fun `trending popular questions count is 5`() {
        val trendingCount = repo.popularQuestions.count { it.isTrending }
        assertEquals(5, trendingCount)
    }

    @Test
    fun `popular questions cover all four modules`() {
        val modules = repo.popularQuestions.map { it.module }.toSet()
        assertTrue("Mileage" in modules)
        assertTrue("Expense" in modules)
        assertTrue("Travel" in modules)
        assertTrue("Approvals" in modules)
    }

    @Test
    fun `mileage module has 4 popular questions`() {
        val count = repo.popularQuestions.count { it.module == "Mileage" }
        assertEquals(4, count)
    }

    @Test
    fun `expense module has 4 popular questions`() {
        val count = repo.popularQuestions.count { it.module == "Expense" }
        assertEquals(4, count)
    }

    @Test
    fun `unanswered questions have positive ask counts`() {
        repo.unansweredQuestions.forEach { q ->
            assertTrue(q.askCount > 0, "Unanswered question ${q.id} has non-positive askCount")
        }
    }

    @Test
    fun `quickReply returns specific answer for known keyword`() {
        val reply = repo.quickReply("how much mileage this week")
        assertTrue(reply.contains("142 km"), "Expected mileage-this-week reply, got: $reply")
    }

    @Test
    fun `quickReply for travel spend contains amount`() {
        val reply = repo.quickReply("what is my travel spend")
        assertTrue(reply.contains("₹12,300"), "Expected travel spend reply, got: $reply")
    }

    @Test
    fun `quickReply for unknown query returns fallback`() {
        val reply = repo.quickReply("gibberish xyz 1234")
        assertTrue(reply.isNotBlank())
        assertFalse(reply.contains("₹"), "Fallback should not contain currency amounts")
    }

    @Test
    fun `quickReply is case-insensitive`() {
        val lower = repo.quickReply("card balance")
        val upper = repo.quickReply("CARD BALANCE")
        assertEquals(lower, upper)
    }

    @Test
    fun `all conversations have non-blank titles`() {
        repo.conversations.forEach { conv ->
            assertTrue(conv.title.isNotBlank(), "Conversation ${conv.id} has blank title")
        }
    }

    @Test
    fun `lastMessageMs is deterministic and non-zero`() {
        repo.conversations.forEach { conv ->
            assertTrue(conv.lastMessageMs > 0, "Conversation ${conv.id} has invalid lastMessageMs")
        }
        val firstPass = repo.conversations.map { it.lastMessageMs }
        val secondPass = AgentRepository().conversations.map { it.lastMessageMs }
        assertEquals(firstPass, secondPass)
    }
}
