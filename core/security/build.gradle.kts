plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.miletracker.core.security"
    compileSdk = 37
    defaultConfig { minSdk = 30 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.biometric)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}
