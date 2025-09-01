plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
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
            implementation(project(":core:platform"))
            api(libs.compose.adaptive)
            api(libs.compose.adaptive.layout)
            api(libs.compose.adaptive.navigation)
            implementation(libs.compose.image.loader)
            implementation(libs.compose.shimmer)
            implementation(libs.kotlin.result)
            implementation(libs.reorderable)
            implementation(libs.kotlin.datetime)
        }
        jvmMain.dependencies {
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
            implementation(compose.material)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}
