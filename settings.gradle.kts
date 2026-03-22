pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Storytale (multiplatform component gallery) publishes dev builds here, not yet on Maven Central.
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Storytale runtime artifacts (org.jetbrains.compose.storytale) resolve from JetBrains Space.
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "MileTrackerDemo"

include(":app")
include(":core:ui")
include(":core:platform")
include(":core:data")
include(":core:network")
include(":core:security")
include(":feature:tracking")
include(":feature:logging")
include(":feature:media")
include(":feature:profile")
include(":feature:approvals")
include(":feature:payables")
include(":feature:travel")
include(":feature:agent")
include(":stub")
include(":wear")
