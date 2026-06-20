package com.miletracker.feature.agent.viewmodel

import androidx.lifecycle.viewModelScope
import com.miletracker.core.ui.mvi.BaseViewModel
import com.miletracker.feature.agent.model.AgentConversation
import com.miletracker.feature.agent.model.AgentMessage
import com.miletracker.feature.agent.repository.AgentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val THINKING_PHRASES =
    mapOf(
        "travel" to "Checking your travel spend…",
        "expense" to "Analysing your expenses…",
        "mileage" to "Reviewing mileage data…",
        "approval" to "Looking up approval status…",
        "advance" to "Checking advance records…",
        "card" to "Fetching card balance…",
        "policy" to "Reviewing policy rules…",
    )
private const val TIMESTAMP_SEED_MS = 1_700_000_000_000L

class AgentViewModel(
    private val repository: AgentRepository,
) : BaseViewModel<AgentUiState, AgentEffect, AgentAction>(
        AgentUiState(
            popularTab = repository.popularQuestions,
            unansweredTab = repository.unansweredQuestions,
            history = repository.conversations,
        ),
    ) {
    /** Backwards-compatible alias; screens read [state]. */
    val uiState: StateFlow<AgentUiState> = state

    private var messageCounter = 0L

    override fun onAction(action: AgentAction) {
        when (action) {
            is AgentAction.SendMessage -> sendMessage(action.text)
            is AgentAction.LoadConversation -> loadConversation(action.conversation)
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg =
            AgentMessage(
                text = text.trim(),
                isUser = true,
                timestampMs = TIMESTAMP_SEED_MS + messageCounter * 5000L,
            )
        messageCounter++

        val thinking =
            THINKING_PHRASES.entries.firstOrNull { text.lowercase().contains(it.key) }?.value
                ?: DEFAULT_THINKING
        val replyText = repository.quickReply(text)

        setState { copy(messages = messages + userMsg, isStreaming = true, streamedText = "", thinkingPhrase = thinking) }
        emitEffect(AgentEffect.ScrollToBottom)

        viewModelScope.launch {
            delay(800L) // thinking delay
            val words = replyText.split(" ")
            var accumulated = ""
            for (word in words) {
                accumulated = if (accumulated.isEmpty()) word else "$accumulated $word"
                setState { copy(streamedText = accumulated) }
                delay(50L)
            }
            val assistantMsg =
                AgentMessage(
                    text = replyText,
                    isUser = false,
                    timestampMs = TIMESTAMP_SEED_MS + messageCounter * 5000L,
                )
            messageCounter++
            setState { copy(messages = messages + assistantMsg, isStreaming = false, streamedText = "") }
            emitEffect(AgentEffect.ScrollToBottom)
        }
    }

    private fun loadConversation(conversation: AgentConversation) {
        setState { copy(messages = conversation.messages, isStreaming = false, streamedText = "") }
    }
}
