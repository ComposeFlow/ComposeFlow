plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.publishing")
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}
