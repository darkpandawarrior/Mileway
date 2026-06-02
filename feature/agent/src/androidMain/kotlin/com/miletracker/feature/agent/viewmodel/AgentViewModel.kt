package com.miletracker.feature.agent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.feature.agent.model.AgentConversation
import com.miletracker.feature.agent.model.AgentMessage
import com.miletracker.feature.agent.model.PopularQuestion
import com.miletracker.feature.agent.model.UnansweredQuestion
import com.miletracker.feature.agent.repository.AgentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
private const val DEFAULT_THINKING = "Thinking…"
private const val TIMESTAMP_SEED_MS = 1_700_000_000_000L

data class AgentUiState(
    val messages: List<AgentMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val streamedText: String = "",
    val thinkingPhrase: String = DEFAULT_THINKING,
    val popularTab: List<PopularQuestion> = emptyList(),
    val unansweredTab: List<UnansweredQuestion> = emptyList(),
    val history: List<AgentConversation> = emptyList(),
)

class AgentViewModel(private val repository: AgentRepository) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            AgentUiState(
                popularTab = repository.popularQuestions,
                unansweredTab = repository.unansweredQuestions,
                history = repository.conversations,
            ),
        )
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    private var messageCounter = 0L

    fun sendMessage(text: String) {
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

        _uiState.update { it.copy(messages = it.messages + userMsg, isStreaming = true, streamedText = "", thinkingPhrase = thinking) }

        viewModelScope.launch {
            delay(800L) // thinking delay
            val words = replyText.split(" ")
            var accumulated = ""
            for (word in words) {
                accumulated = if (accumulated.isEmpty()) word else "$accumulated $word"
                _uiState.update { it.copy(streamedText = accumulated) }
                delay(50L)
            }
            val assistantMsg =
                AgentMessage(
                    text = replyText,
                    isUser = false,
                    timestampMs = TIMESTAMP_SEED_MS + messageCounter * 5000L,
                )
            messageCounter++
            _uiState.update { it.copy(messages = it.messages + assistantMsg, isStreaming = false, streamedText = "") }
        }
    }

    fun loadConversation(conversation: AgentConversation) {
        _uiState.update { it.copy(messages = conversation.messages, isStreaming = false, streamedText = "") }
    }
}
