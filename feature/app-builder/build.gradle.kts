plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:config"))
            implementation(project(":core:di"))
            implementation(project(":core:formatter"))
            implementation(project(":core:logger"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:resources"))
            implementation(project(":core:serializer"))
            implementation(libs.kermit)
            implementation(libs.commons.compress)
            implementation(libs.coroutines.core)
            implementation(libs.gradle.tooling.api)
            implementation(libs.kotlinpoet)
            implementation(libs.kotlin.result)
            implementation(libs.ktlint.core)
            implementation(libs.ktlint.ruleset.standard)
        }
    }
}

tasks {
    val compressAppTemplateZip by registering(Zip::class) {
        into("app-template") {
            from("app-template")
            exclude("gradle/libs.versions.toml")
            // For Gradle cache. You can check if this works by running with --info
            exclude(".gradle/**")
        }
        into("app-template/gradle") {
            from(rootProject.file("gradle/libs.versions.toml"))
            val composeFlowOnlyDependencies =
                listOf(
                    "compose-code-editor",
                    "compose-color-picker",
                    "commons-compress",
                    "conveyor",
                    "atomicfu",
                    "buildconfig-plugin",
                    "filekit-compose",
                    "jewel",
                    "logback",
                    "kaml",
                    "kermit-test",
                    "kotlinpoet",
                    "ktlint",
                    "logback-core",
                    "google-cloud",
                    "gradle-tooling",
                    "material-kolor",
                    "reorderable",
                    "roborazzi",
                    "slf4j",
                    "sentry",
                    "testparameterinjector",
                )
            filter { line ->
                if (composeFlowOnlyDependencies.any { line.contains(it) || line.startsWith("#") }) {
                    logger.lifecycle("Removing \"$line\" for app-template")
                    null
                } else {
                    line
                }
            }
        }
        destinationDirectory.set(layout.projectDirectory.file("src/commonMain/resources").asFile)
        archiveFileName.set("app-template.zip")
        doLast {
            logger.lifecycle("app-template packed to ${archiveFile.get().asFile.absolutePath}")
        }
    }

    withType<ProcessResources> {
        dependsOn(compressAppTemplateZip)
    }
}
