plugins {
    id("io.compose.flow.kmp.library")
    kotlin("plugin.serialization")
    id("io.compose.flow.compose.multiplatform")
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
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.kaml)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlinx.serialization.jsonpath)
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
