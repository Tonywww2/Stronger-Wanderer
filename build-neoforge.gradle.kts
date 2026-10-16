plugins { id("dev.isxander.modstitch.base") version "0.8.5"; id("me.modmuss50.mod-publish-plugin") version "2.1.1" }
val mcVersion = property("deps.minecraft").toString()
val targetNeoForgeVersion = property("deps.neoforge").toString()
val javaVersion = if (stonecutter.current.parsed >= "26.1") 25 else 21
val artifactVersion = "${property("mod.version")}+$mcVersion"
group = property("mod.group").toString()
version = artifactVersion
base.archivesName = "strongerwanderer-neoforge"
modstitch {
    minecraftVersion = mcVersion
    moddevgradle { neoForgeVersion = targetNeoForgeVersion; defaultRuns() }
}
tasks {
    processResources {
        dependsOn("stonecutterGenerate")
        val props = mapOf("version" to project.property("mod.version"),
            "packFormat" to project.property("vers.packFormat"),
            "loaderRange" to project.property("vers.loaderRange"),
            "mcRange" to project.property("vers.minecraftRange"), "neoRange" to "[$targetNeoForgeVersion,)")
        inputs.properties(props)
        inputs.property("mixinJavaVersion", javaVersion)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) { expand(props) }
        exclude("META-INF/mods.toml")
        filesMatching("strongerwanderer.mixins.json") {
            filter { line -> line.replace(", \"refmap\": \"strongerwanderer.refmap.json\"", "").replace("JAVA_17", "JAVA_$javaVersion") }
        }
        if (javaVersion == 25) filesMatching("strongerwanderer.mixins.json") {
            filter { line -> line.replace("\"client.WardenModelMixin\"", "\"client.WardenModelMixin\", \"client.WardenStateMixin\", \"client.WardenRendererMixin\"") }
        }
    }
    withType<JavaCompile>().configureEach {
        dependsOn("stonecutterGenerate"); options.encoding = "UTF-8"; options.release = javaVersion
    }
    withType<Jar>().configureEach { archiveVersion = artifactVersion }
}
java { withSourcesJar(); toolchain.languageVersion = JavaLanguageVersion.of(javaVersion) }

// Publish only the same release artifact produced by the default build.
val publicationDryRun = providers.gradleProperty("publish.dryRun").map { it.toBooleanStrict() }.orElse(false)
publishMods {
    file = tasks.named<Jar>("jar").flatMap { it.archiveFile }
    version = project.version.toString()
    displayName = "${property("mod.name")} ${property("mod.version")} - MC $mcVersion (neoforge)"
    modLoaders.add("neoforge")
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
        javaVersions.add(JavaVersion.toVersion(javaVersion))
        client = true
        server = true
    }
}
tasks.matching { it.name == "publishCurseforge" }.configureEach {
    dependsOn("build", rootProject.tasks.named("validateCurseForgePublish"))
}
