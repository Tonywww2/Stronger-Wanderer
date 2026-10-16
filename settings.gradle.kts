pluginManagement {
    repositories {
        mavenCentral(); gradlePluginPortal()
        maven("https://maven.isxander.dev/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("dev.kikugie.stonecutter") version "0.9.7"
}
rootProject.name = "strongerwanderer"
stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        versions("1.20.1-forge" to "1.20.1")
        versions("1.21.1-neoforge" to "1.21.1", "26.1.2-neoforge" to "26.1.2").buildscript("build-neoforge.gradle.kts")
        vcsVersion = "1.20.1-forge"
    }
}
