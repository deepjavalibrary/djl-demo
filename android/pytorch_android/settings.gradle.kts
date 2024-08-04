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
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/service/local/repositories/${System.getenv("DJL_STAGING")}/content/")
    }
}

rootProject.name = "android"

include(":face_detection")
include(":neural_machine_translation")
include(":quickdraw_recognition")
include(":semantic_segmentation")
include(":speech_recognition")
include(":style_transfer_cyclegan")
