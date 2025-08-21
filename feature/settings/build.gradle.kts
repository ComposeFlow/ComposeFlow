plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:ui"))
            implementation(project(":core:serializer"))
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.filekit.compose)
            implementation(libs.kotlin.result)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test-junit"))
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
