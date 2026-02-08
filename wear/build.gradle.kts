plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.miletracker.wear"
    compileSdk = 37
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.wear.tiles)
    implementation(libs.wear.tiles.material)
    implementation(libs.core.ktx)
    implementation("com.google.guava:guava:33.4.0-android")
}
