plugins {
    id("io.compose.flow.kmp.library")
    kotlin("plugin.serialization")
    id("io.compose.flow.compose.multiplatform")
    `maven-publish`
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            implementation(libs.kaml)
            implementation(libs.kotlin.datetime)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.serialization.jsonpath)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
