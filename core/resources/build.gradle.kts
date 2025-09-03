plugins {
    id("io.compose.flow.kmp.library")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
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
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.kotlinpoet)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.composeflow"
    generateResClass = always
}
