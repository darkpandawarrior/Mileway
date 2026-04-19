plugins {
    id("miletracker.android.application")
}

android {
    namespace = "com.miletracker"

    defaultConfig {
        applicationId = "com.miletracker"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    implementation(project(":feature:tracking"))
    implementation(project(":feature:logging"))
    implementation(project(":feature:media"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:approvals"))
    implementation(project(":feature:payables"))
    implementation(project(":feature:travel"))
    implementation(project(":feature:agent"))
    implementation(project(":stub"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.jb.navigation.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.androidx.workmanager)

    // Material (needed for Theme.Material3.DayNight.NoActionBar in themes.xml)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // WorkManager
    implementation(libs.workmanager.runtime)

    // osmdroid — needed for Configuration init in Application class
    implementation(libs.osmdroid)

    // Konnection — KMP network connectivity monitor (init in Application)
    implementation(libs.konnection)

    // Coil — image loading (world map header background, profile avatars)
    implementation(libs.coil3.compose)
    // Coil 3 decoders — GIF animations and SVG assets
    implementation(libs.coil3.gif)
    implementation(libs.coil3.svg)



    // WormaCeptor — HTTP traffic inspector, DEBUG builds only (never in release; Android-only).
    debugImplementation(libs.wormaceptor.api)
    debugImplementation(libs.wormaceptor.impl)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)

    // Roborazzi — JVM screenshot tests (no device needed)
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
