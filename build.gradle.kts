plugins { id("dev.architectury.loom") version "1.11.458"; id("me.modmuss50.mod-publish-plugin") version "2.1.1" }
val mcVersion = property("vers.mcVersion").toString()
group = property("mod.group").toString()
version = "${property("mod.version")}+$mcVersion"
base.archivesName = "strongerwanderer-forge"
loom {
    silentMojangMappingsLicense()
    mixin { defaultRefmapName = "strongerwanderer.refmap.json" }
    forge { mixinConfig("strongerwanderer.mixins.json") }
    runConfigs.all { runDir("../../run"); ideConfigGenerated(true) }
}
repositories { mavenCentral() }
dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:$mcVersion-${property("vers.deps.fml")}")
}
tasks {
    configureEach { if (name == "createMinecraftArtifacts") dependsOn("stonecutterGenerate") }
    processResources {
        dependsOn("stonecutterGenerate")
        val props = mapOf("version" to project.property("mod.version"), "packFormat" to 15)
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
        exclude("META-INF/neoforge.mods.toml")
    }
    withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.release = 17 }
    jar { manifest.attributes["MixinConfigs"] = "strongerwanderer.mixins.json" }
}
java { withSourcesJar(); toolchain.languageVersion = JavaLanguageVersion.of(17) }

// Isolated integration harness. Never included in a normal build or release JAR.
if (providers.gradleProperty("swGameTest").isPresent) {
    sourceSets.main { java.srcDir(rootProject.file("src/gametest/java")); resources.srcDir(rootProject.file("src/gametest/resources")) }
    loom.runs.create("gameTestServer") {
        server(); runDir("../../build/gametest")
        vmArg("-Dforge.gameTestServer=true")
        vmArg("-Dforge.enabledGameTestNamespaces=strongerwanderer")
    }
}

// Publish only the same release artifact produced by the default build.
val publicationDryRun = providers.gradleProperty("publish.dryRun").map { it.toBooleanStrict() }.orElse(false)
publishMods {
    file = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile }
    version = project.version.toString()
    displayName = "${property("mod.name")} ${property("mod.version")} - MC $mcVersion (forge)"
    modLoaders.add("forge")
    type = providers.gradleProperty("curseforge.releaseType").orElse("release").map {
        when (it) {
            "release" -> STABLE
            "beta" -> BETA
            "alpha" -> ALPHA
            else -> error("curseforge.releaseType must be release, beta or alpha")
        }
    }
    dryRun = publicationDryRun
    changelog = providers.environmentVariable("CHANGELOG").filter { it.isNotBlank() }
        .orElse(providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText)
    curseforge {
        // Pending ID is usable only in dry runs; the shared validation task blocks real uploads.
        projectId = providers.gradleProperty("curseforge.projectId").orElse("").map { it.ifBlank { "UNCONFIGURED" } }
        accessToken = if (publicationDryRun.get()) providers.provider { "dry-run" }
            else providers.environmentVariable("CURSEFORGE_TOKEN").filter { it.isNotBlank() }
                .orElse(providers.gradleProperty("curseforge.token").filter { it.isNotBlank() })
        minecraftVersions.add(mcVersion)
        javaVersions.add(JavaVersion.toVersion(17))
        client = true
        server = true
    }
}
tasks.matching { it.name == "publishCurseforge" }.configureEach {
    dependsOn("build", rootProject.tasks.named("validateCurseForgePublish"))
}
