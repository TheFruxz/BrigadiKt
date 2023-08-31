plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.dokka") version "1.8.20"
    `maven-publish`
}

val host = "github.com/TheFruxz/BrigadiKt"

version = "2023.3"
group = "dev.fruxz"

repositories {

    mavenCentral()

    maven("https://jitpack.io") {
        name = "JitPack"
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
    api("com.github.TheFruxz:Ascend:2023.3.3")

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
        mavenLocal()
    }

    publications.create("BrigadiKt", MavenPublication::class) {
        artifactId = "brigadikt"
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
        outputDirectory.set(layout.projectDirectory.dir("build"))
    }

}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}