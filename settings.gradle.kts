pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "taskTree"
includeBuild("tasktree")
include("example")
include("example_core")
