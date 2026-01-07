package com.miletracker.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.settings.DemoSettings
import com.miletracker.core.data.settings.DemoSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DemoSettingsViewModel(
    private val repository: DemoSettingsRepository
) : ViewModel() {

    val settings: StateFlow<DemoSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DemoSettings())

    fun toggleSimulateRoot() = viewModelScope.launch { repository.toggleSimulateRoot() }
    fun toggleSimulateOffline() = viewModelScope.launch { repository.toggleSimulateOffline() }
    fun toggleBiometricGuard() = viewModelScope.launch { repository.toggleBiometricGuard() }
    fun toggleGpsDrift() = viewModelScope.launch { repository.toggleGpsDrift() }
    fun toggleAutoDiscard() = viewModelScope.launch { repository.toggleAutoDiscard() }
}
