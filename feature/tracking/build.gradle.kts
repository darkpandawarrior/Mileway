plugins {
    id("miletracker.kmp.compose")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "com.miletracker.feature.tracking"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.ui)
            implementation(libs.material3)
            implementation(libs.foundation)
            implementation(libs.material.icons.extended)
            implementation(libs.ui.tooling.preview.mp)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.jb.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:ui"))
            implementation(project(":core:platform"))
        }
        androidMain.dependencies {
            // Material Components — provides the Theme.Material3.DayNight.NoActionBar parent
            // for Theme.MileTracker (res/values/themes.xml). api() so dependents resolve it.
            api(libs.material)
            implementation(libs.core.ktx)
            implementation(libs.activity.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.kotlinx.coroutines.play.services)

            implementation(libs.play.services.location)
            implementation(libs.workmanager.runtime)
            implementation(libs.osmdroid)
            implementation(libs.mlkit.document.scanner)
            implementation(libs.mlkit.text.recognition)
            implementation(libs.coil3.compose)

            implementation(project(":feature:media"))
        }
    }
}
