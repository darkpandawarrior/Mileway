plugins {
    id("miletracker.kmp.compose")
}

kotlin {
    android {
        namespace = "com.miletracker.core.maps"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.ui)
            implementation(libs.foundation)
        }
    }
}
