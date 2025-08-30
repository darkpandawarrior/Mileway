package com.miletracker

import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.feature.profile.repository.FakeProfileRepository
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the profile/settings ViewModel, driven through a fake repository.
 */
class ProfileViewModelTest {

    private fun viewModel() = ProfileViewModel(FakeProfileRepository(), ThemeController())

    @Test
    fun `notifications default on and toggles off`() {
        val vm = viewModel()
        assertTrue(vm.notificationsEnabled.value)
        vm.toggleNotifications()
        assertFalse(vm.notificationsEnabled.value)
    }

    @Test
    fun `units default to miles and toggle`() {
        val vm = viewModel()
        assertTrue(vm.useMiles.value)
        vm.toggleUnits()
        assertFalse(vm.useMiles.value)
    }

    @Test
    fun `dark theme override starts null then can be forced`() {
        val vm = viewModel()
        assertEquals(null, vm.darkThemeOverride.value)
        vm.setDarkTheme(true)
        assertEquals(true, vm.darkThemeOverride.value)
    }

    @Test
    fun `header is populated from the repository`() {
        val vm = viewModel()
        assertTrue(vm.uiState.value.header.name.isNotBlank())
        assertTrue(vm.uiState.value.tiles.isNotEmpty())
    }
}
