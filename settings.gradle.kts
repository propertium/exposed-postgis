enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "propertium"

include("lib")


pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}