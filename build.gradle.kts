import java.util.*

plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    id("org.jetbrains.dokka") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.2"
    id("org.hildan.kotlin-publish") version "1.7.0"
}

val publishVersion = System.getenv("GH_RELEASE_VERSION")
val calendar = Calendar.getInstance()

group = "dev.fruxz"
version = publishVersion?.plus("-preview") ?: "${calendar[Calendar.YEAR]}.${calendar[Calendar.MONTH] + 1}-dev"


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
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    // MoltenKt
    api("dev.fruxz:ascend:2025.8-013083a")
    api("dev.fruxz:stacked:2025.8-ca42cc0")

    // Brigadier
    api("com.mojang:brigadier:1.0.18")

    // JetBrains
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

}

publishing {
    repositories {
        mavenLocal()
        maven("https://nexus.fruxz.dev/repository/releases/") {
            name = "fruxz.dev"
            credentials {
                username = System.getenv("FXZ_NEXUS_USER")
                password = System.getenv("FXZ_NEXUS_SECRET")
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