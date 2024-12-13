plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
    signing
}

subprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}

allprojects {
    group = "io.github.nikitok"
    version = "0.3"

    repositories {
        mavenCentral()
    }
}