pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    plugins {
        id("com.android.application") version "8.9.2"
        id("com.android.library") version "8.9.2"
        kotlin("android") version "2.1.21"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "smart-sticky-wallpaper"
include(":client:shared", ":client:desktop")
if (providers.gradleProperty("enableAndroid").orNull == "true") include(":client:android")
