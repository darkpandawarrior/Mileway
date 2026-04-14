plugins {
    id("miletracker.kmp.library")
}

kotlin {
    android {
        namespace = "com.miletracker.core.security"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.biometric)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
