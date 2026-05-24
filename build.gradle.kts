plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.navgraph) apply false
    // Storytale 0.0.4-alpha01+dev19 is incompatible with Kotlin 2.4.0 (HostManager.getHostIsSupported removed).
    // Deferred until a Kotlin-2.4.0-compatible Storytale release is published. See PLAN_V11 Phase 7.
    // alias(libs.plugins.storytale) apply false
}
