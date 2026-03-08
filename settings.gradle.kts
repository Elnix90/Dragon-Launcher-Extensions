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

rootProject.name = "Dragon-Launcher-Extensions"

include(":ext-internet-proxy")
include(":ext-auto-update")
include(":ext-shizuku-installer")
include(":ext-additional-fonts")
