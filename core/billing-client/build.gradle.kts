plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
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
            implementation(project(":core:platform"))
            implementation(project(":core:config"))
            implementation(project(":core:serializer"))
            implementation(project(":core:di"))
            implementation(libs.ktor.core)
            implementation(libs.kotlin.result)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
