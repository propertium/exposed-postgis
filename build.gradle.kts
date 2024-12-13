plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
    signing
}

allprojects {
    group = "io.github.nikitok"
    version = "0.1"

    repositories {
        mavenCentral()
    }
}