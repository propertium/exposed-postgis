plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.plugin.serialization)
    `maven-publish`
    signing
    id("tech.yanand.maven-central-publish") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.postgis:postgis-jdbc:2023.1.0") {
        exclude(module = "postgresql")
    }
    implementation("org.postgresql:postgresql:42.7.4")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.logback.classic)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.3")
    testImplementation("org.orbisgis:h2gis:2.2.3")
//    testImplementation("org.orbisgis:h2gis-functions:1.3.2")

    implementation("org.jetbrains.dokka:dokka-base:1.9.0")
    implementation("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets {
        configureEach {
            includeNonPublic.set(false)
            skipDeprecated.set(true)
        }
    }
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "exposed-postgis"

            artifact(tasks.named("dokkaJavadocJar"))
            artifact(sourcesJar)

            pom {
                name.set("extension-exposed-postgis")
                description.set("extension-exposed-postgis is a Kotlin library built on top of Exposed to support PostGIS-enabled PostgreSQL databases. This library provides seamless and type-safe integration for spatial data manipulation.")
                url.set("https://github.com/propertium/exposed-postgis.git")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://mit-license.org/")
                    }
                }
                developers {
                    developer {
                        id.set("nikitok")
                        name.set("Nikita Navalikhin")
                        email.set("noviiden@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/propertium/exposed-postgis.git")
                    developerConnection.set("scm:git:git@github.com:propertium.git")
                    url.set("https://github.com/propertium")
                }
            }
        }
    }
}


mavenCentral {
    authToken = project.property("ossrhToken") as String
    publishingType = "AUTOMATIC"
    maxWait = 60
}

signing {
    useInMemoryPgpKeys(
        project.property("signing.keyId") as String,
        file(project.property("signing.secretKeyRingFile") as String).readText(),
        project.property("signing.password") as String
    )
    sign(publishing.publications["maven"])
}