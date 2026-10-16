plugins { id("dev.kikugie.stonecutter") }
stonecutter active "1.20.1-forge"
stonecutter parameters {
    constants { match(current.project.substringAfterLast('-'), "forge", "neoforge") }
}

// Validation is shared by aggregate and directly invoked per-node upload tasks.
tasks.register("validateCurseForgePublish") {
    group = "publishing"
    description = "Checks publishing configuration without uploading."
    doLast {
        check(!providers.gradleProperty("swGameTest").isPresent) {
            "GameTest builds cannot be published. Use the default build."
        }
        val dryRun = providers.gradleProperty("publish.dryRun").map { it.toBooleanStrict() }.getOrElse(false)
        val id = providers.gradleProperty("curseforge.projectId").getOrElse("").trim()
        val token = providers.environmentVariable("CURSEFORGE_TOKEN").filter { it.isNotBlank() }
            .orElse(providers.gradleProperty("curseforge.token").filter { it.isNotBlank() })
        if (!dryRun) {
            check(id.matches(Regex("[1-9][0-9]*"))) {
                "Set curseforge.projectId in gradle.properties before publishing."
            }
            check(token.isPresent) {
                "Set CURSEFORGE_TOKEN or curseforge.token in user-level Gradle properties."
            }
        }
        val changelog = providers.environmentVariable("CHANGELOG").getOrNull()
            ?.takeIf { it.isNotBlank() } ?: file("CHANGELOG.md").readText()
        check(changelog.isNotBlank()) { "CHANGELOG.md must not be empty." }
        logger.lifecycle("CurseForge project: ${id.ifBlank { "pending (dry run only)" }}; dry run: $dryRun")
    }
}
tasks.register("publishAllVersions") {
    group = "publishing"
    description = "Publishes all three default-build release JARs to CurseForge."
    dependsOn(stonecutter.tasks.named("publishMods").map { it.values })
}
// Serial uploads avoid cross-node API bursts.
stonecutter.tasks.order("publishCurseforge")
