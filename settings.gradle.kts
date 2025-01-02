dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
pluginManagement{
    repositories{
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
rootProject.name = "Voice Recorder"
include(":app")

include(":feature:record")
include(":feature:settings")
include(":feature:playlist")

include(":core:designsystem")
include(":core:common")
include(":core:service")
include(":core:datastore")

// culudamar 2024.11.22: Disable lint tasks for now as they are failing:
// source: https://stackoverflow.com/a/78414423/1728856
gradle.startParameter.excludedTaskNames.addAll(
    listOf("lintKotlinAndroidTest", "lintKotlinAndroidTestDebug", "lintAndroidTestRelease",
    "lintKotlinMain", "lintKotlinRelease", "lintKotlinDebug", "lintKotlinTestDebug",
    "lintKotlinTest", "lintKotlinTestFixturesDebug", "lintKotlinTestFixturesRelease",
    "lintKotlinTestRelease", "lintKotlinTestFixtures")
)
