plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
}

val host = "github.com/TheFruxz/BrigadiKt"

version = "2024-INDEV-1"
group = "dev.fruxz"

repositories {

    mavenCentral()

    maven("https://repo.fruxz.dev/releases") {
        name = "fruxz.dev"
    }

    maven("https://libraries.minecraft.net") {
        name = "Minecraft Libraries"
    }

}

dependencies {

    // Kotlin
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))

    // MoltenKt
    api("dev.fruxz:ascend:2024.1.1")

    // Brigadier
    api("com.mojang:brigadier:1.0.18")

}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourceJar by tasks.register<Jar>("sourceJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {

    repositories {
        maven("https://repo.fruxz.dev/releases") {
            name = "fruxz.dev"
            credentials {
                username = project.findProperty("fruxz.dev.user") as? String? ?: System.getenv("FRUXZ_DEV_USER")
                password = project.findProperty("fruxz.dev.secret") as? String? ?: System.getenv("FRUXZ_DEV_SECRET")
            }
        }
    }

    publications.create("BrigadiKt", MavenPublication::class) {
        artifactId = name.lowercase()
        version = version.lowercase()

        artifact(dokkaJavadocJar)
        artifact(dokkaHtmlJar)
        artifact(sourceJar) {
            classifier = "sources"
        }

        from(components["kotlin"])

    }

}

tasks {

    test {
        useJUnitPlatform()
    }

    dokkaHtml.configure {
        outputDirectory.set(layout.projectDirectory.dir("docs"))
    }

}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}