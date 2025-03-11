import java.util.Calendar

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.hildan.kotlin-publish") version "1.7.0"
}

val publishVersion = System.getenv("GH_RELEASE_VERSION")
val calendar = Calendar.getInstance()

group = "dev.fruxz"
version = publishVersion ?: "${calendar[Calendar.YEAR]}.${calendar[Calendar.MONTH] + 1}-dev"


repositories {

    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }

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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Paper
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    // MoltenKt
    api("dev.fruxz:ascend:+")

    // Brigadier
    api("com.mojang:brigadier:1.0.18")

    // JetBrains
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

}

publishing {
    repositories {
        mavenLocal()
        maven("https://repo.fruxz.dev/releases") {
            name = "fruxz.dev"
            credentials {
                username = System.getenv("FRUXZ_DEV_USER")
                password = System.getenv("FRUXZ_DEV_SECRET")
            }
        }
    }
}


tasks {

    compileKotlin {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
        }
    }

    dokkaHtml.configure {
        outputDirectory.set(layout.projectDirectory.dir("docs"))
    }

}

kotlin {
    jvmToolchain(21)
}