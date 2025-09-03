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
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(project(":core:di"))
        }
        jvmMain.dependencies {
            implementation(libs.posthog.java)
        }
    }
}
