plugins {
    id("miletracker.android.application")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.navgraph)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.miletracker"

    defaultConfig {
        applicationId = "com.miletracker"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        // Default placeholder; override in gms flavor with your real key or via local.properties.
        manifestPlaceholders["MAPS_API_KEY"] = ""
    }

    // Maps flavor dimension:
    //   gms   → KrossMap (Google Maps on Android, MapKit on iOS) — requires API key
    //   noGms → MapLibre (open-source tiles, no API key, offline MBTiles capable)
    flavorDimensions += "maps"
    productFlavors {
        create("gms") {
            dimension = "maps"
            // Set your Google Maps API key here or load from local.properties:
            // manifestPlaceholders["MAPS_API_KEY"] = project.properties["MAPS_API_KEY"] ?: ""
        }
        create("noGms") {
            dimension = "maps"
        }
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
            all {
                // JDK 21+ requires these opens for Robolectric + Compose rendering.
                it.jvmArgs(
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.util=ALL-UNNAMED",
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                )
            }
        }
    }
}

navgraph {
    // Flavored app — pin to the gms debug variant so KSP picks the right classpath.
    variant.set("gmsDebug")
}

ksp {
    arg("navgraph.annotatedOnly", "true")
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

    // Map implementations — selected by flavor
    "gmsImplementation"(project(":core:maps-krossmap"))
    "noGmsImplementation"(project(":core:maps-maplibre"))

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
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(project(":core:platform"))
    testImplementation(libs.room.testing)

    // compose-nav-graph — navigation graph visualization (Phase 8)
    implementation(libs.navigation3.runtime)
    implementation(libs.compose.nav.graph.annotations)
    ksp(libs.compose.nav.graph.annotations)

    // Roborazzi — JVM screenshot tests (no device needed)
    // preview-scanner auto-discovers all @Preview functions across all feature modules
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.preview.scanner)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
