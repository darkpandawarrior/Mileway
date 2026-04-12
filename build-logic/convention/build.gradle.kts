plugins {
    `kotlin-dsl`
}

group = "com.miletracker.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "miletracker.kmp.library"
            implementationClass = "MileTrackerKmpLibraryConventionPlugin"
        }
    }
}
