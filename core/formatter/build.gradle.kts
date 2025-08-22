plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.compose.code.editor)
            implementation(libs.kotlinpoet)
            implementation(libs.ktlint.core)
            implementation(libs.ktlint.ruleset.standard)
        }
    }
}
