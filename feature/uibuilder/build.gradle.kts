plugins {
    id("io.compose.flow.kmp.library")
    kotlin("plugin.serialization")
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
            implementation(project(":core:ai"))
            implementation(project(":core:analytics"))
            implementation(project(":core:config"))
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:resources"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(project(":feature:api-editor"))
            implementation(project(":feature:app-builder"))
            implementation(project(":feature:appstate-editor"))
            implementation(project(":feature:firestore-editor"))
            implementation(project(":feature:asset-editor"))
            implementation(project(":feature:datatype-editor"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:string-editor"))
            implementation(project(":feature:theme-editor"))
            implementation(libs.compose.color.picker)
            implementation(libs.compose.shimmer)
            implementation(libs.ktor.core)
            implementation(libs.ktor.kotlinx.json)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.kotlinx.serialization.jsonpath)
            implementation(libs.reorderable)
            implementation(libs.kaml)
            api(libs.precompose)
            api(libs.precompose.viewmodel)
        }

        jvmMain.dependencies {
            implementation(libs.compose.code.editor)
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinpoet)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
            implementation(libs.splitpane)
            implementation(compose.desktop.common)
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

        jvmTest.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:testing"))
            implementation(kotlin("test-junit"))
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
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

// Configure test tasks to show detailed stack traces
tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
