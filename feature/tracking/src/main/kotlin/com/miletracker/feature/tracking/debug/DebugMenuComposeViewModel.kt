package com.miletracker.feature.tracking.debug

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.miletracker.feature.tracking.export.TrackExportManager
import com.miletracker.feature.tracking.repository.LocationRepository
import com.miletracker.feature.tracking.repository.SavedTrackRepository
import com.miletracker.feature.tracking.ui.components.ExportFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Compose-based Debug Menu.
 * Manages state and business logic for debug options.
 * Accepts repositories so the location-export action can reach local storage.
 */
@OptIn(FlowPreview::class)
class DebugMenuComposeViewModel(
    private val savedTrackRepository: SavedTrackRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    // --- Location Tracking Fine-tuning Config Flows ---
    private val _abnormalConfigValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val abnormalConfigValues: StateFlow<Map<String, String>> = _abnormalConfigValues.asStateFlow()

    private val _abnormalConfigEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private fun loadAbnormalConfigValues() {
        scope.launch {
            // Stub: no AbnormalDetectionConfig available in MileTrackerDemo
            _abnormalConfigValues.value = emptyMap()
        }
    }

    fun updateAbnormalConfigEntry(key: String, value: String, context: Context) {
        scope.launch {
            val updated = _abnormalConfigValues.value.toMutableMap()
            updated[key] = value
            _abnormalConfigValues.value = updated
        }
    }

    fun resetAbnormalConfigToDefaults(context: Context) {
        scope.launch {
            loadAbnormalConfigValues()
        }
    }

    private val TAG = "DebugMenuViewModel"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // UI State for the debug menu
    private val _uiState = MutableStateFlow(DebugMenuUiState())
    val uiState: StateFlow<DebugMenuUiState> = _uiState.asStateFlow()

    // Search query for filtering options
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Available debug profiles
    private val _availableProfiles = MutableStateFlow(DebugProfiles.allProfiles)
    val availableProfiles: StateFlow<List<DebugProfile>> = _availableProfiles.asStateFlow()

    // Currently selected profile
    private val _selectedProfile = MutableStateFlow<DebugProfile?>(null)
    val selectedProfile: StateFlow<DebugProfile?> = _selectedProfile.asStateFlow()

    // MutableStateFlow for observing login status
    private val _loginStatus = MutableStateFlow(false)
    val loginStatus: StateFlow<Boolean> = _loginStatus.asStateFlow()

    // Combined screen state for more efficient UI updates
    private val _screenState = MutableStateFlow(
        DebugScreenState(
            uiState = DebugMenuUiState(),
            searchQuery = "",
            availableProfiles = emptyList(),
            selectedProfile = null,
            isLoggedIn = false
        )
    )
    val screenState = _screenState.asStateFlow()

    // Login events flow to trigger login status checks
    private val _loginEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // Reactive count of enabled options
    private val enabledOptionsCount = _uiState.map { state ->
        countEnabledOptions(state)
    }

    // Initialize state
    init {
        loadAbnormalConfigValues()
        loadDebugOptions()

        // Set up abnormal config reload observer
        scope.launch {
            _abnormalConfigEvents
                .debounce(200)
                .distinctUntilChanged()
                .collect { loadAbnormalConfigValues() }
        }

        // Set up login status observers
        scope.launch {
            _loginEvents
                .debounce(300)
                .distinctUntilChanged()
                .collect { refreshLoginStatus() }
        }

        // Trigger initial login check
        checkLoginStatusAsync()

        // Set up combined state updates
        scope.launch {
            uiState.collect { updateScreenState() }
        }
        scope.launch {
            searchQuery.collect { updateScreenState() }
        }
        scope.launch {
            availableProfiles.collect { updateScreenState() }
        }
        scope.launch {
            selectedProfile.collect { updateScreenState() }
        }
        scope.launch {
            loginStatus.collect { updateScreenState() }
        }

        // Add observer for enabled options count updates
        scope.launch {
            enabledOptionsCount.collect { count ->
                val currentCount = _uiState.value.enabledOptionsCount
                if (currentCount != count) {
                    _uiState.update { it.copy(enabledOptionsCount = count) }
                }
            }
        }
    }

    /**
     * Update the combined screen state
     */
    private fun updateScreenState() {
        _screenState.update { current ->
            current.copy(
                uiState = _uiState.value,
                searchQuery = _searchQuery.value,
                availableProfiles = _availableProfiles.value,
                selectedProfile = _selectedProfile.value,
                isLoggedIn = _loginStatus.value
            )
        }
    }

    /**
     * Call this method when login status might have changed
     */
    fun checkLoginStatusAsync() {
        scope.launch { _loginEvents.emit(Unit) }
    }

    /**
     * Load all debug options — using in-memory defaults (no DebugDataStore in MileTrackerDemo)
     */
    private fun loadDebugOptions() {
        scope.launch {
            try {
                _uiState.update { currentState ->
                    currentState.copy(
                        coreOptions = loadCoreOptions(),
                        originOptions = loadOriginOptions(),
                        authOptions = loadAuthOptions(),
                        featureOptions = loadFeatureOptions(),
                        trackingOptions = loadTrackingOptions(),
                        customValues = loadCustomValues(),
                        expandedSections = mapOf(
                            DebugSection.CORE to false,
                            DebugSection.ORIGIN to false,
                            DebugSection.AUTH to false,
                            DebugSection.FEATURES to false,
                            DebugSection.TRACKING to false,
                            DebugSection.PROFILES to false
                        ),
                        apiOrigin = "Not set",
                        referralParams = getReferralParams(),
                        isLoggedIn = false,
                        hasSavedConfig = false,
                        savedConfigSummary = ""
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading debug options: ${e.message}", e)
            }
        }
    }

    private fun loadCoreOptions(): Map<String, Boolean> = mapOf(
        "Enable Logging For Release" to false,
        "Force Logout Enabled" to false,
        "Force Super Delegate Mode" to false,
        "Enable Tracking Overlay" to false,
        "Save Debug Configuration" to false
    )

    private fun loadOriginOptions(): Map<String, Boolean> = mapOf(
        "Force UAT" to false,
        "Force Prod" to false,
        "Force Dev Environment" to false,
        "Force Custom Origin" to false,
        "Force MENA Login" to false,
        "Force File Prod" to false
    )

    private fun loadAuthOptions(): Map<String, Boolean> = mapOf(
        "Use Custom OTP" to false,
        "Skip OTP Pin Screen" to false
    )

    private fun loadFeatureOptions(): Map<String, Boolean> = mapOf(
        "Enable Custom Banner" to false,
        "Hide Debug Banner" to false,
        "Enable Trip Deactivation" to false,
        "Force Editable Form" to false,
        "Force Expense" to false,
        "Force Corporate" to false,
        "Force Travel" to false,
        "Force Travel Old UI" to false,
        "Force Track Miles V2 UI" to false,
        "Force Single Invoice" to false,
        "Force Multi Invoice" to false,
        "Force PR" to false,
        "Force Events" to false,
        "Force Cards" to false,
        "Force Cards V2 UI" to false,
        "Force Club" to false,
        "Force Fave App Redirection" to false,
        "Enable Draft Log Miles" to false,
        "Enable Track Miles to Log Miles Draft" to false
    )

    private fun loadTrackingOptions(): Map<String, Boolean> = mapOf(
        "Skip Odometer" to false,
        "Toggle Odometer OCR" to false,
        "Allow Mock Locations" to false,
        "Enable Location Dump Creation" to false,
        "Force BE Distance Calculation" to false,
        "Bypass Battery Level Check" to false,
        "Bypass Battery Optimization Check" to false,
        "Use V2 Location Sync API" to false
    )

    private fun loadCustomValues(): Map<String, String> = mapOf(
        "Custom Origin" to "",
        "Custom Banner Text" to "",
        "Custom OTP" to ""
    )

    /**
     * Check if the user is logged in — stub, always false in demo
     */
    private fun checkLoginStatus(): Boolean = false

    /**
     * Refresh the login status
     */
    fun refreshLoginStatus() {
        scope.launch {
            val isLoggedIn = checkLoginStatus()
            _loginStatus.value = isLoggedIn
            _uiState.update {
                it.copy(
                    isLoggedIn = isLoggedIn,
                    apiOrigin = if (isLoggedIn) "Not set" else "Not logged in"
                )
            }
        }
    }

    /**
     * Count the total number of enabled options
     */
    private fun countEnabledOptions(state: DebugMenuUiState): Int {
        return with(state) {
            coreOptions.count { it.value } +
                    originOptions.count { it.value } +
                    authOptions.count { it.value } +
                    featureOptions.count { it.value } +
                    trackingOptions.count { it.value }
        }
    }

    /**
     * Get referral parameters
     */
    private fun getReferralParams(): String {
        return "client_code=-, login_mode=-, region_code=-"
    }

    /**
     * Toggle section expansion state
     */
    fun toggleSection(section: DebugSection) {
        _uiState.update { currentState ->
            val updatedSections = currentState.expandedSections.toMutableMap()
            updatedSections[section] = !(updatedSections[section] ?: false)
            currentState.copy(expandedSections = updatedSections)
        }
    }

    /**
     * Update search query for filtering options
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Generic unified toggle function for all option types
     */
    private inline fun toggleOption(
        optionName: String,
        currentOptions: Map<String, Boolean>,
        updateState: (Map<String, Boolean>) -> Unit,
        mutuallyExclusive: Boolean = false,
        postAction: (String, Boolean) -> Unit = { _, _ -> }
    ) {
        val currentValue = currentOptions[optionName] ?: false
        val newValue = !currentValue
        val newOptions = currentOptions.toMutableMap()

        if (mutuallyExclusive && newValue) {
            newOptions.keys.forEach { key -> newOptions[key] = false }
        }

        newOptions[optionName] = newValue
        updateState(newOptions)
        postAction(optionName, newValue)
        _selectedProfile.value = null
    }

    fun toggleCoreOption(optionName: String) {
        toggleOption(
            optionName = optionName,
            currentOptions = _uiState.value.coreOptions,
            updateState = { newOptions -> _uiState.update { it.copy(coreOptions = newOptions) } }
        )
    }

    fun toggleOriginOption(optionName: String) {
        toggleOption(
            optionName = optionName,
            currentOptions = _uiState.value.originOptions,
            updateState = { newOptions -> _uiState.update { it.copy(originOptions = newOptions) } },
            mutuallyExclusive = true
        )
    }

    fun toggleAuthOption(optionName: String) {
        toggleOption(
            optionName = optionName,
            currentOptions = _uiState.value.authOptions,
            updateState = { newOptions -> _uiState.update { it.copy(authOptions = newOptions) } }
        )
    }

    fun toggleFeatureOption(optionName: String) {
        toggleOption(
            optionName = optionName,
            currentOptions = _uiState.value.featureOptions,
            updateState = { newOptions -> _uiState.update { it.copy(featureOptions = newOptions) } }
        )
    }

    fun toggleTrackingOption(optionName: String) {
        toggleOption(
            optionName = optionName,
            currentOptions = _uiState.value.trackingOptions,
            updateState = { newOptions -> _uiState.update { it.copy(trackingOptions = newOptions) } }
        )
    }

    /**
     * Update a custom input value
     */
    fun updateCustomValue(key: String, value: String) {
        val currentValues = _uiState.value.customValues.toMutableMap()
        currentValues[key] = value
        _uiState.update { it.copy(customValues = currentValues) }
        _selectedProfile.value = null
    }

    /**
     * Apply a profile's options to the UI state
     */
    private fun applyProfileToUiState(profile: DebugProfile) {
        val newCoreOptions = _uiState.value.coreOptions.toMutableMap()
        val newOriginOptions = _uiState.value.originOptions.toMutableMap()
        val newAuthOptions = _uiState.value.authOptions.toMutableMap()
        val newFeatureOptions = _uiState.value.featureOptions.toMutableMap()
        val newTrackingOptions = _uiState.value.trackingOptions.toMutableMap()
        val newCustomValues = _uiState.value.customValues.toMutableMap()

        // Reset all options to false first
        newCoreOptions.keys.forEach { newCoreOptions[it] = false }
        newOriginOptions.keys.forEach { newOriginOptions[it] = false }
        newAuthOptions.keys.forEach { newAuthOptions[it] = false }
        newFeatureOptions.keys.forEach { newFeatureOptions[it] = false }
        newTrackingOptions.keys.forEach { newTrackingOptions[it] = false }

        // Apply profile options
        profile.options.forEach { (key, value) ->
            when {
                newCoreOptions.containsKey(key) -> newCoreOptions[key] = value
                newOriginOptions.containsKey(key) -> newOriginOptions[key] = value
                newAuthOptions.containsKey(key) -> newAuthOptions[key] = value
                newFeatureOptions.containsKey(key) -> newFeatureOptions[key] = value
                newTrackingOptions.containsKey(key) -> newTrackingOptions[key] = value
            }
        }

        // Apply custom values from profile
        profile.customValues.forEach { (key, value) -> newCustomValues[key] = value }

        _uiState.update { currentState ->
            currentState.copy(
                coreOptions = newCoreOptions,
                originOptions = newOriginOptions,
                authOptions = newAuthOptions,
                featureOptions = newFeatureOptions,
                trackingOptions = newTrackingOptions,
                customValues = newCustomValues
            )
        }
    }

    /**
     * Select and apply a debug profile
     */
    fun selectProfile(profile: DebugProfile) {
        applyProfileToUiState(profile)
        _selectedProfile.value = profile
    }

    /**
     * Apply all changes — in demo, applies to in-memory state only
     */
    fun applyChanges(): ApplyResult {
        val changesMade = _uiState.value.enabledOptionsCount
        return ApplyResult(
            changesApplied = changesMade > 0,
            restartRequired = true,
            changeCount = changesMade
        )
    }

    /**
     * Perform app restart
     */
    fun performAppRestart(context: Context) {
        AppRestartUtils.performAppRestart(context)
    }

    /**
     * Clear all debug settings
     */
    fun clearAllDebugSettings() {
        scope.launch {
            loadDebugOptions()
            _selectedProfile.value = null
        }
    }

    /**
     * Run garbage collection and show memory stats
     */
    fun runGarbageCollection() {
        scope.launch {
            try {
                val runtime = Runtime.getRuntime()
                val beforeMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                System.gc()
                delay(100)
                val afterMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val freedMemory = beforeMemory - afterMemory
                Log.d(TAG, "GC completed: freed ${freedMemory}MB, current usage: ${afterMemory}MB")
            } catch (e: Exception) {
                Log.e(TAG, "Error running GC: ${e.message}", e)
            }
        }
    }

    /**
     * Get current user session info for display
     */
    fun getUserSessionInfo(): Map<String, String> {
        return mapOf(
            "Login Status" to "Logged Out",
            "API Origin" to "Not set",
            "Debug Mode" to "Enabled",
            "Save Config" to "Disabled"
        )
    }

    /**
     * Perform API health check
     */
    fun performApiHealthCheck() {
        Log.d(TAG, "API health check requested (stub in MileTrackerDemo)")
    }

    /**
     * Clear app cache data
     */
    fun clearAppCache(context: Context) {
        scope.launch {
            try {
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
                cacheDir.mkdirs()
                Log.d(TAG, "App cache cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache: ${e.message}", e)
            }
        }
    }

    /**
     * Export the most recent saved track as a GPX file and fire a share chooser.
     * Falls back gracefully when no track is found.
     */
    fun exportMostRecentTrack(context: Context) {
        scope.launch {
            try {
                val tracks = savedTrackRepository.count()
                if (tracks == 0L) {
                    Log.w(TAG, "No saved tracks to export")
                    return@launch
                }
                // Load the most recently active/completed track
                val track = savedTrackRepository.getActiveTrack()
                    ?: return@launch Unit.also { Log.w(TAG, "No active track found") }

                val locations = locationRepository.getForToken(track.routeId)
                val content = TrackExportManager.buildContent(
                    format = ExportFormat.GPX,
                    track = track,
                    locations = locations,
                    events = emptyList(),
                )
                val intent = TrackExportManager.buildShareIntent(
                    context = context,
                    format = ExportFormat.GPX,
                    trackName = track.routeId,
                    content = content,
                )
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting track: ${e.message}", e)
            }
        }
    }

    fun saveDebugConfiguration() {
        // Stub: no persistent DebugDataStore in MileTrackerDemo
        _uiState.update { it.copy(hasSavedConfig = true, savedConfigSummary = "In-memory only") }
    }

    fun restoreDebugConfiguration() {
        // Stub
        loadDebugOptions()
    }
}

/**
 * UI State for the Debug Menu
 */
data class DebugMenuUiState(
    val coreOptions: Map<String, Boolean> = emptyMap(),
    val originOptions: Map<String, Boolean> = emptyMap(),
    val authOptions: Map<String, Boolean> = emptyMap(),
    val featureOptions: Map<String, Boolean> = emptyMap(),
    val trackingOptions: Map<String, Boolean> = emptyMap(),
    val customValues: Map<String, String> = emptyMap(),
    val expandedSections: Map<DebugSection, Boolean> = emptyMap(),
    val apiOrigin: String = "",
    val referralParams: String = "",
    val isLoggedIn: Boolean = false,
    val enabledOptionsCount: Int = 0,
    val hasSavedConfig: Boolean = false,
    val savedConfigSummary: String = ""
)

/**
 * Debug Menu Sections
 */
enum class DebugSection {
    CORE, ORIGIN, AUTH, FEATURES, TRACKING, PROFILES
}

/**
 * Result of applying changes
 */
data class ApplyResult(
    val changesApplied: Boolean,
    val restartRequired: Boolean,
    val changeCount: Int
)

data class DebugScreenState(
    val uiState: DebugMenuUiState,
    val searchQuery: String,
    val availableProfiles: List<DebugProfile>,
    val selectedProfile: DebugProfile?,
    val isLoggedIn: Boolean
)
