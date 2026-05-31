plugins {
    id("miletracker.cmp.feature")
}

kotlin {
    android {
        namespace = "com.miletracker.feature.agent"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.markdown.renderer)
            implementation(libs.markdown.renderer.m3)
            implementation(project(":core:ui"))
        }
        androidMain.dependencies {
            implementation(project(":stub"))
        }
    }
}
