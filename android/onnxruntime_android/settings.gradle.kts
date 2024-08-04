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
        maven ("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/service/local/repositories/${System.getenv("DJL_STAGING")}/content/")
    }
}

rootProject.name="android"

include(":object_detection")
