plugins {
    `kotlin-dsl`
}

group = "com.miletracker.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "miletracker.kmp.library"
            implementationClass = "MileTrackerKmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "miletracker.kmp.compose"
            implementationClass = "MileTrackerKmpComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "miletracker.android.library"
            implementationClass = "MileTrackerAndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "miletracker.android.application"
            implementationClass = "MileTrackerAndroidApplicationConventionPlugin"
        }
        register("cmpFeature") {
            id = "miletracker.cmp.feature"
            implementationClass = "MileTrackerCmpFeatureConventionPlugin"
        }
    }
}
