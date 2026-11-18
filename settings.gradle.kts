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
        maven {
            // Актуальный репозиторий RuStore SDK (документация: rustore.ru/help/sdk/updates/kotlin-java/10-5-2)
            // Старый artifactory-external.vkpartner.ru отключён.
            url = uri("https://nexus-external.rustore.ru/repository/maven-rustore-exposed")
        }
    }
}

rootProject.name = "VedHelper"
include(":app")