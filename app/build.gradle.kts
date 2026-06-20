import java.io.FileInputStream
import java.util.Properties

plugins {
    id("miletracker.android.application")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.navgraph)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.dependency.guard)
}

// Release signing — reads from keystore.properties (gitignored) or env vars (CI).
// Falls back to debug signing if neither is present, so `assembleGmsRelease` still
// succeeds locally and in CI without secrets configured.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties =
    Properties().apply {
        if (keystorePropertiesFile.exists()) {
            FileInputStream(keystorePropertiesFile).use { load(it) }
        }
    }
val hasReleaseSigning =
    keystorePropertiesFile.exists() || System.getenv("RELEASE_STORE_FILE") != null

// FLA.1: single-source versioning. The repo-root VERSION + BUILD_NUMBER files (bumped by
// scripts/bump_version.sh) are the only place versions change. versionCode = base + BUILD_NUMBER.
val VERSION_CODE_BASE = 1

fun readVersionName(): String =
    rootProject.file("VERSION").takeIf { it.exists() }?.readText()?.trim()?.ifEmpty { null } ?: "1.0.0"

fun readBuildNumber(): Int =
    rootProject.file("BUILD_NUMBER").takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: 0

android {
    namespace = "com.miletracker"

    defaultConfig {
        applicationId = "com.miletracker"
        minSdk = 30
        targetSdk = 36
        // FLA.1: single-source versioning. VERSION (semver) + BUILD_NUMBER files at the repo root are the
        // ONE place versions change (via scripts/bump_version.sh). versionCode = VERSION_CODE_BASE + BUILD_NUMBER.
        versionCode = VERSION_CODE_BASE + readBuildNumber()
        versionName = readVersionName()
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

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile =
                    file(
                        keystoreProperties.getProperty("storeFile")
                            ?: System.getenv("RELEASE_STORE_FILE"),
                    )
                storePassword =
                    keystoreProperties.getProperty("storePassword")
                        ?: System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias =
                    keystoreProperties.getProperty("keyAlias")
                        ?: System.getenv("RELEASE_KEY_ALIAS")
                keyPassword =
                    keystoreProperties.getProperty("keyPassword")
                        ?: System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Use the real release keystore when available; otherwise fall back to the
            // debug key so CI/local release builds still produce an installable APK.
            signingConfig =
                if (hasReleaseSigning) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
        // QA build: release-like (minified, non-debuggable) but installable alongside
        // the debug build via a distinct applicationId. Always debug-signed so QA can
        // sideload without release secrets. matchingFallbacks lets library modules that
        // only define debug/release resolve their `release` variant for `staging`.
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            isDebuggable = false
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    sourceSets {
        // staging has no source set of its own; reuse the release no-op stubs for
        // WormaCeptorHelper / ShowcaseLauncher (the real impls are debug-only).
        getByName("staging").kotlin.srcDir("src/release/java")
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
    // Room — faster incremental processing; only reprocesses changed DAOs.
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

// Compose compiler stability/recomposition reports — written to build/compose_metrics/.
// Trigger via: ./gradlew assembleGmsRelease -PenableComposeMetrics=true
// (debug builds add Live Literals noise; always run on release variant)
if (project.findProperty("enableComposeMetrics") == "true") {
    composeCompiler {
        metricsDestination = layout.buildDirectory.dir("compose_metrics")
        reportsDestination = layout.buildDirectory.dir("compose_metrics")
        // Stability config: teach the compiler about third-party immutable types
        // to prevent false "unstable" labels and unnecessary recompositions.
        stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability.conf"))
    }
}

// Kover — line-coverage floor enforced by koverVerifyNoGmsDebugCoverage. Coverage is
// measured on the noGms (JVM-safe) variant; the gms flavor's Play Services maps crash
// the Robolectric fork. Floor is a regression guard kept below the current ~38% line
// coverage; ratchet up as the suite grows (PLAN B.3a / H.6).
kover {
    currentProject {
        createVariant("noGmsDebugCoverage") {
            add("noGmsDebug")
        }
    }
    reports {
        variant("noGmsDebugCoverage") {
            filters {
                excludes {
                    packages("*.BuildConfig", "*.R")
                }
            }
            verify {
                rule {
                    minBound(30)
                }
            }
        }
    }
}

// Dependency Guard — baseline snapshot of releaseRuntimeClasspath to catch
// silent transitive version bumps in CI. Run `./gradlew dependencyGuardBaseline`
// after any intentional dep change, then commit the updated baseline file.
dependencyGuard {
    configuration("gmsReleaseRuntimeClasspath")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    // V15: app directly references core:platform service interfaces + factories (flavor impls).
    implementation(project(":core:platform"))
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

    // V15 platform services — Play-Core update + review, gms flavor ONLY (proprietary).
    // noGms gets no-op impls; the VerifyDependencyPrefixes guard (FLFD.2) keeps these out of FOSS.
    "gmsImplementation"(libs.play.app.update)
    "gmsImplementation"(libs.play.app.update.ktx)
    "gmsImplementation"(libs.play.review)
    "gmsImplementation"(libs.play.review.ktx)

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

    // Baseline profiles — installs AOT-compiled Dex profile at install time for cold-start wins.
    // The actual profile lives in :baselineprofile module (add when ready to generate).
    implementation(libs.profileinstaller)

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
