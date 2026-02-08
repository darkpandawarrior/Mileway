plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.miletracker.core.ui"
        compileSdk = 37
        minSdk = 30
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries {
            framework {
                baseName = "MileTracker"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.datastore.preferences.core)
            implementation(libs.materialkolor)
            implementation(libs.colorpicker.compose)
            implementation(project(":core:data"))
        }
        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.activity.compose)
            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.datastore.preferences)
            implementation(libs.osmdroid)
            implementation(libs.coil.compose)
            implementation(libs.exifinterface)
            implementation(libs.picktime.compose)
            implementation(libs.compose.webview)
            implementation(libs.koffee)
        }
    }
}
