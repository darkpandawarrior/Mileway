package com.miletracker.feature.agent.viewmodel

import com.miletracker.feature.agent.model.AgentConversation
import com.miletracker.feature.agent.model.AgentMessage
import com.miletracker.feature.agent.model.PopularQuestion
import com.miletracker.feature.agent.model.UnansweredQuestion

internal const val DEFAULT_THINKING = "Thinking…"

data class AgentUiState(
    val messages: List<AgentMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val streamedText: String = "",
    val thinkingPhrase: String = DEFAULT_THINKING,
    val popularTab: List<PopularQuestion> = emptyList(),
    val unansweredTab: List<UnansweredQuestion> = emptyList(),
    val history: List<AgentConversation> = emptyList(),
)

sealed interface AgentAction {
    data class SendMessage(val text: String) : AgentAction

    data class LoadConversation(val conversation: AgentConversation) : AgentAction
}

sealed interface AgentEffect {
    /** Emitted after a message is appended so the chat scrolls to the latest bubble. */
    data object ScrollToBottom : AgentEffect
}
