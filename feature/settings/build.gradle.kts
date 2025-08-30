plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:ui"))
            implementation(project(":core:serializer"))
            implementation(libs.filekit.compose)
            implementation(libs.kotlin.result)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test-junit"))
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
