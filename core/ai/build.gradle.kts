plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    alias(libs.plugins.kotlin.serialization)
    id("io.compose.flow.publishing")
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:config"))
            implementation(project(":core:serializer"))
            implementation(project(":core:platform"))

            api(project.dependencies.platform(libs.google.cloud.bom))

            api(libs.compose.adaptive)
            api(libs.compose.adaptive.layout)
            api(libs.compose.adaptive.navigation)
            implementation(libs.kotlin.result)
            implementation(libs.ktor.core)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}
