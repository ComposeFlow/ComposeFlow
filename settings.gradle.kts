import java.nio.file.Files
import java.nio.file.StandardCopyOption

val sourceVersionCatalogPath = file("$settingsDir/gradle/libs.versions.toml").toPath()
val targetAppTemplateGradleDir =
    file("$settingsDir/feature/app-builder/app-template/gradle").toPath()
val targetVersionCatalogPath = targetAppTemplateGradleDir.resolve("libs.versions.toml")

// Ensure the target directory exists
if (!Files.exists(targetAppTemplateGradleDir)) {
    try {
        Files.createDirectories(targetAppTemplateGradleDir)
    } catch (e: Exception) {
        throw GradleException(
            "Failed to create directory $targetAppTemplateGradleDir: ${e.message}",
            e,
        )
    }
}

// This is to copy the version catalog file from ComposeFlow to the generated apps.
// We used to use symlink for the same purpose, but it has an issue on Windows.
if (Files.exists(sourceVersionCatalogPath)) {
    try {
        Files.copy(
            sourceVersionCatalogPath,
            targetVersionCatalogPath,
            StandardCopyOption.REPLACE_EXISTING,
        )
        println("INFO: Copied $sourceVersionCatalogPath to $targetVersionCatalogPath during initialization.")
    } catch (e: Exception) {
        throw GradleException(
            "Failed to copy $sourceVersionCatalogPath to $targetVersionCatalogPath: ${e.message}",
            e,
        )
    }
} else {
    println("WARNING: Source versions file not found at $sourceVersionCatalogPath. Skipping copy.")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // So that Compose Hot Reload automatically downloads the appropriate JBR
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ComposeFlow"

include(":desktopApp")
include(":generate-jsonschema-cli")
include(":core:ai")
include(":core:analytics")
include(":core:config")
include(":core:di")
include(":core:kxs-ts-gen-core")
include(":core:logger")
include(":core:model")
include(":core:platform")
include(":core:resources")
include(":core:serializer")
include(":core:testing")
include(":core:ui")
include(":core:billing-client")
include(":feature:api-editor")
include(":feature:app-builder")
include(":feature:appstate-editor")
include(":feature:firestore-editor")
include(":feature:asset-editor")
include(":feature:datatype-editor")
include(":feature:uibuilder")
include(":feature:string-editor")
include(":feature:theme-editor")
include(":feature:settings")
include(":feature:top")
include(":ksp-llm-tools")
include(":ksp-llm-tools-annotations")

includeBuild("build-logic")
includeBuild("feature/app-builder/app-template")
