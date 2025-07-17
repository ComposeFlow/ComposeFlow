plugins {
    id("io.compose.flow.kmp.library")
    kotlin("plugin.serialization")
    id("io.compose.flow.compose.multiplatform")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(project(":ksp-llm-tools"))
            implementation(project(":core:ai"))
            implementation(project(":core:di"))
            implementation(project(":core:icons"))
            implementation(project(":core:formatter"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(project(":feature:api-editor"))
            implementation(project(":feature:app-builder"))
            implementation(project(":feature:appstate-editor"))
            implementation(project(":feature:firestore-editor"))
            implementation(project(":feature:asset-editor"))
            implementation(project(":feature:datatype-editor"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:theme-editor"))
            implementation(libs.compose.code.editor)
            implementation(libs.compose.color.picker)
            implementation(libs.compose.shimmer)
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinpoet)
            implementation(libs.ktor.core)
            implementation(libs.ktor.kotlinx.json)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.kotlinx.serialization.jsonpath)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
            implementation(libs.reorderable)
            implementation(libs.kaml)
            implementation(libs.splitpane)
            api(libs.precompose)
            api(libs.precompose.viewmodel)
        }

        // Configure KSP for LLM tools
        dependencies {
            add("kspDesktop", project(":ksp-llm-tools"))
        }

        // Configure KSP options
        ksp {
            // Set output directory for LLM tool JSON files
            arg("llmToolsOutputDir", "${project.buildDir}/generated/llm-tools")
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.common)
            }
        }

        commonTest.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:testing"))
            implementation(kotlin("test-junit"))
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}

// Add a specific task to run KSP
tasks.register("runKsp") {
    group = "ksp"
    description = "Run KSP to generate LLM tool JSON files"

    // Create the output directory
    doFirst {
        mkdir("${project.buildDir}/generated/llm-tools")
    }

    // Depend on the KSP task for the desktop target
    dependsOn("kspKotlinDesktop")
}

// Make sure the KSP tasks run
afterEvaluate {
    tasks.withType<com.google.devtools.ksp.gradle.KspTask>().configureEach {
        // Ensure the KSP task runs
        outputs.upToDateWhen { false }
    }
}

