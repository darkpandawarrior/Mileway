import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin adding `watchos*` targets on top of the shared KMP-library base
 * (`shared.kmp.library`, in the external kmp-build-logic composite build).
 *
 * Applied standalone (no separate library plugin alongside it, e.g. `core:data`), so it inlines the
 * same Kotlin Multiplatform + AGP KMP-library applies and iOS target declarations
 * `SharedKmpLibraryConventionPlugin` makes — a composite build's plugin markers are only visible to
 * the *root* build's `plugins{}` resolution, not to a sibling included build's own
 * `pluginManager.apply()` calls, so `apply("shared.kmp.library")` can't resolve from here.
 *
 * Deliberately a *separate* plugin id (mirroring how `shared.kmp.compose` layers on
 * `shared.kmp.library`) rather than a flag on the base plugin, so watchOS targets stay an explicit
 * per-module opt-in: `core:ui`/Compose and Wear/phone-only modules apply only `shared.kmp.library`
 * and never see `watchos*` (PLAN_V23 §2/§6 — SQLCipher-backed/Compose modules must not compile for
 * watchOS). Also applies `applyDefaultHierarchyTemplate()` so the `apple`/`native` intermediate
 * source sets exist for `watchos*` to share `actual`s with `ios*` via `appleMain`.
 */
class MilewayKmpLibraryWatchosConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
        }
        extensions.configure<KotlinMultiplatformExtension> {
            iosArm64()
            iosSimulatorArm64()
            applyDefaultHierarchyTemplate()
            watchosArm64()
            watchosSimulatorArm64()
            watchosDeviceArm64()
        }
    }
}
