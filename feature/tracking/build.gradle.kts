plugins {
    id("miletracker.cmp.feature")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "com.miletracker.feature.tracking"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:ui"))
            implementation(project(":core:platform"))
            implementation(project(":core:maps"))
        }
        androidMain.dependencies {
            // api() so dependents (feature:logging, :app) can resolve Material theme parent
            api(libs.material)
            implementation(libs.koin.androidx.workmanager)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.play.services.location)
            implementation(libs.workmanager.runtime)
            implementation(libs.mlkit.document.scanner)
            implementation(libs.mlkit.text.recognition)
            implementation(libs.coil3.compose)
            implementation(project(":feature:media"))
        }
    }
}
