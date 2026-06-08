package com.miletracker.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// MVI base — S = UiState, E = one-shot Effect (navigation/toast), A = user Action
abstract class BaseViewModel<S : Any, E : Any, A : Any>(initial: S) : ViewModel() {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected fun emitEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun onAction(action: A)
}
