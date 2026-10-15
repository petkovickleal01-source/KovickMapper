pluginManagement {
    repositories {
        google()
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

rootProject.name = "KovickMapper"
include(":app")

// The upstream wireless-debug repository is nested: its Android library
// lives in wirelessdebug/wirelessdebug. Point Gradle directly at that module
// so its own root build.gradle is not evaluated as a Kovick subproject.
include(":wirelessdebug")
project(":wirelessdebug").projectDir = file("wirelessdebug/wirelessdebug")
