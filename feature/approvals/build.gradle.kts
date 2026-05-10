plugins {
    id("miletracker.kmp.compose")
}

kotlin {
    android {
        namespace = "com.miletracker.feature.approvals"
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
            implementation(libs.kotlinx.datetime)

            implementation(project(":core:common"))
            implementation(project(":core:ui"))
        }
        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.activity.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.koin.android)
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}
