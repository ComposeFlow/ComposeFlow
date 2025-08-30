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
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(project(":feature:app-builder"))
            implementation(libs.kaml)
            implementation(libs.kotlinx.serialization.jsonpath)
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.kotlinx.json)
            implementation(libs.kotlin.result)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
        }

        commonTest.dependencies {
            implementation(project(":core:model"))
            implementation(kotlin("test-junit"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
            implementation(libs.jewel.int.ui.standalone)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.serialization.jsonpath)
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
