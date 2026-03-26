plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    android {
        namespace = "com.miletracker.core.platform"
        compileSdk = 37
        minSdk = 30
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.core.ktx)
            // LocationTracker (Android): fused location + Task.await()
            implementation(libs.play.services.location)
            implementation(libs.kotlinx.coroutines.play.services)
            // TextRecognizer (Android): ML Kit on-device OCR
            implementation(libs.mlkit.text.recognition)
        }
    }
}
