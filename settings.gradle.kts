enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Chromatic"
include(":app")
include(":core:color")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":feature:learn")
include(":feature:quiz")
include(":feature:tools")
include(":feature:settings")
include(":wear")
include(":desktop")
include(":baselineprofile")
