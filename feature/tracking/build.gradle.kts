plugins {
    id("miletracker.android.library")
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.miletracker.feature.tracking"
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Material Components — provides the Theme.Material3.DayNight.NoActionBar parent
    // for Theme.MileTracker (res/values/themes.xml). api() so dependents resolve it.
    api(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.jb.navigation.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.play.services.location)
    implementation(libs.workmanager.runtime)
    implementation(libs.osmdroid)
    implementation(libs.mlkit.document.scanner)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.coil3.compose)

    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":feature:media"))
}
