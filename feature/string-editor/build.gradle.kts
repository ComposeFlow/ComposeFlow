plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    id("com.google.devtools.ksp") version libs.versions.ksp
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":ksp-llm-tools-annotations"))
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:resources"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(libs.kaml)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.datastore.preferences.core)
        }

        // Configure KSP for LLM tools
        dependencies {
            add("kspJvm", project(":ksp-llm-tools"))
        }

        // Configure KSP options
        ksp {
            // Set output directory for LLM tool JSON files
            arg("llmToolsOutputDir", "${project.layout.buildDirectory.get()}/generated/llm-tools")
        }

        commonTest.dependencies {
            implementation(project(":core:model"))
            implementation(kotlin("test-junit"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
            implementation(libs.jewel.int.ui.standalone)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
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
        mkdir("${project.layout.buildDirectory.get()}/generated/llm-tools")
    }

    // Depend on the KSP task for the JVM target
    dependsOn("kspKotlinJvm")
}

// Make sure the KSP tasks run
afterEvaluate {
    tasks.withType<com.google.devtools.ksp.gradle.KspAATask>().configureEach {
        // Ensure the KSP task runs
        outputs.upToDateWhen { false }
    }
}
