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
        // Add this line for Kotlin Multiplatform native dependencies
        maven("https://maven.pkg.jetbrains.space/public/p/kotlin/dev")
    }
}

rootProject.name = "clap-app-demo"
include(":app")
include(":shared")