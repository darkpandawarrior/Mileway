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
    }
}

rootProject.name = "MileTrackerDemo"

include(":app")
include(":core:ui")
include(":core:data")
include(":core:network")
include(":feature:tracking")
include(":feature:logging")
include(":feature:media")
include(":feature:profile")
include(":feature:approvals")
include(":feature:payables")
include(":feature:travel")
include(":stub")
