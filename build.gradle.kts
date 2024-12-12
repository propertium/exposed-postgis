plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    version = "0.1"

    repositories {
        mavenCentral()
    }
}