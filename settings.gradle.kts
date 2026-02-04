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
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MileTrackerDemo"

include(":app")
include(":core:ui")
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
