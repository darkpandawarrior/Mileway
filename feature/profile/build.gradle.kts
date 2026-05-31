plugins {
    id("miletracker.cmp.feature")
}

kotlin {
    android {
        namespace = "com.miletracker.feature.profile"
        compileSdk = 37
        minSdk = 30
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:ui"))
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:security"))
        }
        androidMain.dependencies {
            implementation("androidx.appcompat:appcompat:1.7.0")
            implementation(project(":stub"))
        }
    }
}
