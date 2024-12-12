plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "io.propertium"

java.sourceCompatibility = JavaVersion.VERSION_11
kotlin {
    jvmToolchain(11)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("net.postgis:postgis-jdbc:2023.1.0") {
        exclude(module = "postgresql")
    }
    implementation("org.postgresql:postgresql:42.6.0")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("org.testcontainers:postgresql:1.20.4")
}

tasks.test {
    useJUnitPlatform()
}