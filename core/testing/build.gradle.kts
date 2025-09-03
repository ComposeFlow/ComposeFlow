plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    `maven-publish`
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.datastore.preferences.core)
        }
        all {
            optInComposeExperimentalApis()
        }
    }
}
