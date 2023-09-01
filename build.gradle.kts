plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

val host = "github.com/TheFruxz/BrigadiKt"

version = "2023.3-RC2"
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

val sourceJar by tasks.register<Jar>("sourceJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {

    repositories {
        mavenLocal()
    }

    publications.create("BrigadiKt", MavenPublication::class) {
        artifactId = name.lowercase()
        version = version.lowercase()

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

}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}