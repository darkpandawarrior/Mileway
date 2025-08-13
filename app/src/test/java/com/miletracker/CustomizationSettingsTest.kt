package com.miletracker

import com.miletracker.core.ui.theme.AccentPalette
import com.miletracker.core.ui.theme.AppLanguage
import com.miletracker.core.ui.theme.ExperimentalFlags
import com.miletracker.core.ui.theme.ThemeController
import com.miletracker.core.ui.theme.colors
import com.miletracker.feature.profile.repository.FakeProfileRepository
import com.miletracker.feature.profile.viewmodel.ProfileViewModel
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the customization/settings logic added in the parity task.
 *
 * All tests are pure JVM — no Android framework, no Koin, no Compose.
 * Each test is isolated via a fresh [ThemeController] + [ProfileViewModel] pair.
 */
class CustomizationSettingsTest {

    private fun controller() = ThemeController()
    private fun viewModel(tc: ThemeController = controller()) =
        ProfileViewModel(FakeProfileRepository(), tc)

    // =========================================================================
    // 1. Palette selection — mapping to expected color set
    // =========================================================================

    @Test
    fun `default palette starts as DEFAULT`() {
        val tc = controller()
        assertEquals(AccentPalette.DEFAULT, tc.accentPalette.value)
    }

    @Test
    fun `setPalette changes to TEAL`() {
        val tc = controller()
        tc.setPalette(AccentPalette.TEAL)
        assertEquals(AccentPalette.TEAL, tc.accentPalette.value)
    }

    @Test
    fun `setPalette changes to INDIGO`() {
        val tc = controller()
        tc.setPalette(AccentPalette.INDIGO)
        assertEquals(AccentPalette.INDIGO, tc.accentPalette.value)
    }

    @Test
    fun `setPalette changes to AMBER`() {
        val tc = controller()
        tc.setPalette(AccentPalette.AMBER)
        assertEquals(AccentPalette.AMBER, tc.accentPalette.value)
    }

    @Test
    fun `each palette maps to a distinct primary color`() {
        val primaries = AccentPalette.entries.map { it.colors().primary }
        // All primaries should be unique (no two palettes share the same primary)
        assertEquals(primaries.size, primaries.toSet().size,
            "Each AccentPalette must map to a unique primary color")
    }

    @Test
    fun `TEAL palette primary differs from DEFAULT palette primary`() {
        assertNotEquals(
            AccentPalette.DEFAULT.colors().primary,
            AccentPalette.TEAL.colors().primary,
        )
    }

    @Test
    fun `INDIGO palette dark primary differs from DEFAULT palette dark primary`() {
        assertNotEquals(
            AccentPalette.DEFAULT.colors().primaryDark,
            AccentPalette.INDIGO.colors().primaryDark,
        )
    }

    @Test
    fun `AMBER palette onPrimary is white in light mode`() {
        // Amber uses white on the orange primary to ensure contrast
        val amber = AccentPalette.AMBER.colors()
        assertEquals(androidx.compose.ui.graphics.Color.White, amber.onPrimary)
    }

    @Test
    fun `ViewModel exposes palette from ThemeController`() {
        val tc = controller()
        val vm = viewModel(tc)
        tc.setPalette(AccentPalette.INDIGO)
        assertEquals(AccentPalette.INDIGO, vm.accentPalette.value)
    }

    @Test
    fun `ViewModel setPalette delegates to ThemeController`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.setPalette(AccentPalette.TEAL)
        assertEquals(AccentPalette.TEAL, tc.accentPalette.value)
    }

    @Test
    fun `palette label is non-blank for all entries`() {
        AccentPalette.entries.forEach { palette ->
            assertTrue(palette.label.isNotBlank(), "Palette ${palette.name} must have a non-blank label")
        }
    }

    // =========================================================================
    // 2. Settings state reducer — experimental flag toggles
    // =========================================================================

    @Test
    fun `experimental flags start all false`() {
        val flags = ExperimentalFlags()
        assertFalse(flags.batteryAwareTracking)
        assertFalse(flags.lowEndDeviceTuning)
        assertFalse(flags.aggressiveGpsFilter)
    }

    @Test
    fun `toggleBatteryAwareTracking flips the flag`() {
        val tc = controller()
        val vm = viewModel(tc)
        assertFalse(vm.experimentalFlags.value.batteryAwareTracking)
        vm.toggleBatteryAwareTracking()
        assertTrue(vm.experimentalFlags.value.batteryAwareTracking)
        vm.toggleBatteryAwareTracking()
        assertFalse(vm.experimentalFlags.value.batteryAwareTracking)
    }

    @Test
    fun `toggleLowEndDeviceTuning flips only that flag`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.toggleBatteryAwareTracking() // set battery flag true
        vm.toggleLowEndDeviceTuning()
        assertTrue(vm.experimentalFlags.value.lowEndDeviceTuning)
        // battery flag should be unaffected
        assertTrue(vm.experimentalFlags.value.batteryAwareTracking)
    }

    @Test
    fun `toggleAggressiveGpsFilter flips only that flag`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.toggleAggressiveGpsFilter()
        assertTrue(vm.experimentalFlags.value.aggressiveGpsFilter)
        assertFalse(vm.experimentalFlags.value.batteryAwareTracking)
        assertFalse(vm.experimentalFlags.value.lowEndDeviceTuning)
    }

    @Test
    fun `all three experimental flags can be enabled independently`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.toggleBatteryAwareTracking()
        vm.toggleLowEndDeviceTuning()
        vm.toggleAggressiveGpsFilter()
        val flags = vm.experimentalFlags.value
        assertTrue(flags.batteryAwareTracking)
        assertTrue(flags.lowEndDeviceTuning)
        assertTrue(flags.aggressiveGpsFilter)
    }

    @Test
    fun `updateExperimentalFlags replaces all flags atomically`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.toggleBatteryAwareTracking()
        tc.updateExperimentalFlags(ExperimentalFlags(aggressiveGpsFilter = true))
        val flags = vm.experimentalFlags.value
        assertFalse(flags.batteryAwareTracking, "Battery flag should be replaced")
        assertTrue(flags.aggressiveGpsFilter)
    }

    // =========================================================================
    // 3. Locale tag validation
    // =========================================================================

    @Test
    fun `ENGLISH language tag is 'en'`() {
        assertEquals("en", AppLanguage.ENGLISH.tag)
    }

    @Test
    fun `HINDI language tag is 'hi'`() {
        assertEquals("hi", AppLanguage.HINDI.tag)
    }

    @Test
    fun `all language tags are non-blank and lowercase BCP-47`() {
        AppLanguage.entries.forEach { lang ->
            assertTrue(lang.tag.isNotBlank(), "${lang.name}: tag must be non-blank")
            assertEquals(lang.tag, lang.tag.lowercase(), "${lang.name}: tag should be lowercase")
        }
    }

    @Test
    fun `all language displayNames are non-blank`() {
        AppLanguage.entries.forEach { lang ->
            assertTrue(lang.displayName.isNotBlank(), "${lang.name}: displayName must be non-blank")
        }
    }

    @Test
    fun `language tags are unique across all entries`() {
        val tags = AppLanguage.entries.map { it.tag }
        assertEquals(tags.size, tags.toSet().size, "All language tags must be distinct")
    }

    @Test
    fun `default language is ENGLISH`() {
        val tc = controller()
        assertEquals(AppLanguage.ENGLISH, tc.language.value)
    }

    @Test
    fun `setLanguage changes to HINDI`() {
        val tc = controller()
        tc.setLanguage(AppLanguage.HINDI)
        assertEquals(AppLanguage.HINDI, tc.language.value)
    }

    @Test
    fun `ViewModel setLanguage delegates to ThemeController`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.setLanguage(AppLanguage.HINDI)
        assertEquals(AppLanguage.HINDI, tc.language.value)
    }

    // =========================================================================
    // 4. Reset customization
    // =========================================================================

    @Test
    fun `resetCustomization restores palette to DEFAULT`() {
        val tc = controller()
        tc.setPalette(AccentPalette.AMBER)
        tc.resetCustomization()
        assertEquals(AccentPalette.DEFAULT, tc.accentPalette.value)
    }

    @Test
    fun `resetCustomization restores language to ENGLISH`() {
        val tc = controller()
        tc.setLanguage(AppLanguage.HINDI)
        tc.resetCustomization()
        assertEquals(AppLanguage.ENGLISH, tc.language.value)
    }

    @Test
    fun `resetCustomization clears all experimental flags`() {
        val tc = controller()
        tc.updateExperimentalFlags(ExperimentalFlags(batteryAwareTracking = true, aggressiveGpsFilter = true))
        tc.resetCustomization()
        assertEquals(ExperimentalFlags(), tc.experimentalFlags.value)
    }

    @Test
    fun `ViewModel resetCustomization delegates to ThemeController`() {
        val tc = controller()
        val vm = viewModel(tc)
        vm.setPalette(AccentPalette.INDIGO)
        vm.setLanguage(AppLanguage.HINDI)
        vm.toggleAggressiveGpsFilter()
        vm.resetCustomization()
        assertEquals(AccentPalette.DEFAULT, vm.accentPalette.value)
        assertEquals(AppLanguage.ENGLISH, vm.language.value)
        assertFalse(vm.experimentalFlags.value.aggressiveGpsFilter)
    }

    @Test
    fun `resetCustomization does not affect dark theme override`() {
        val tc = controller()
        tc.set(true)
        tc.resetCustomization()
        assertEquals(true, tc.darkThemeOverride.value,
            "Dark theme override is separate from customization reset")
    }
}
