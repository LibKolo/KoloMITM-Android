@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
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
        mavenLocal()
        mavenCentral()
        maven {
            name = "opencollabRepositoryMavenSnapshots"
            url = uri("https://repo.opencollab.dev/maven-snapshots")
        }
        maven {
            name = "opencollabRepositoryMavenReleases"
            url = uri("https://repo.opencollab.dev/maven-releases")
        }
    }
}

rootProject.name = "KoloMITM-Android"
include(":app")
