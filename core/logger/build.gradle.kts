plugins {
    id("io.compose.flow.kmp.library")
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.logback.classic)
            implementation(libs.logback.core)
            implementation(libs.slf4j.api)
        }
    }
}
