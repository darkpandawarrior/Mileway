import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Opt-in convention plugin adding a `jvm("desktop")` target on top of [MilewayKmpLibraryConventionPlugin]
 * (PLAN_V23 Phase D — Compose Desktop dashboard).
 *
 * Mirrors [MilewayKmpLibraryWatchosConventionPlugin]: a *separate* plugin id rather than a flag on the
 * base plugin, so the desktop target stays an explicit per-module opt-in (only the `core:{common,data,
 * platform,ui}` graph a dashboard-only app needs, per Option b — NOT `feature:tracking`/maps). Must be
 * `jvm("desktop")` (the Compose-desktop-attributed target), not a bare `jvm()`, or AndroidX
 * lifecycle/Compose variant resolution fails with misleading "unresolved reference" errors.
 */
class MilewayKmpDesktopConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // mileway.kmp.library (now shared.kmp.library, in the external kmp-build-logic composite
        // build) already applies org.jetbrains.kotlin.multiplatform + the AGP KMP-library plugin.
        // A composite build's plugin markers aren't visible to a sibling included build's own
        // pluginManager.apply() calls, so this can no longer resolve "shared.kmp.library" by id here —
        // but every consumer of mileway.kmp.desktop (core:ui, core:platform, core:common, core:data)
        // already applies shared.kmp.compose/shared.kmp.library or mileway.kmp.library.watchos
        // alongside it, which applies those plugins itself. Nothing left to re-apply here.
        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()
            jvm("desktop")
        }
    }
}
