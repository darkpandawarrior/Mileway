package com.miletracker.feature.media.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miletracker.core.data.library.MediaLibraryEntry
import com.miletracker.feature.media.repository.MediaLibraryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudLibraryViewModel(
    private val repository: MediaLibraryRepository
) : ViewModel() {

    val entries: StateFlow<List<MediaLibraryEntry>> = repository
        .observeLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(entry: MediaLibraryEntry) {
        viewModelScope.launch { repository.delete(entry) }
    }
}
