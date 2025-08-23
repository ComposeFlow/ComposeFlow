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
        commonMain.dependencies {
            implementation(project(":core:platform"))
            api(libs.compose.adaptive)
            api(libs.compose.adaptive.layout)
            api(libs.compose.adaptive.navigation)
            implementation(libs.compose.image.loader)
            implementation(libs.compose.shimmer)
            implementation(libs.kotlin.result)
            implementation(libs.reorderable)
        }
        commonTest.dependencies {
            implementation(kotlin("test-junit"))
        }
        jvmMain.dependencies {
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
        }
    }
}
