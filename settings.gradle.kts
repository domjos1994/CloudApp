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
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "CloudApp"
include(":app")
include(":NotificationFeature")
include(":CalendarFeature")
include(":ContactFeature")
include(":DataFeature")
include(":ChatFeature")
include(":CalDav")
include(":CarDav")
include(":REST")
include(":AppBasics")
include(":Database")
include(":Data")
include(":WebDav")
include(":NotesFeature")
include(":ToDoFeature")
