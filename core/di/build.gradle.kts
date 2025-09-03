plugins {
    id("io.compose.flow.kmp.library")
    `maven-publish`
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}
