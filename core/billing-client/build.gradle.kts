plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:platform"))
            implementation(project(":core:config"))
            implementation(project(":core:serializer"))
            implementation(project(":core:di"))
            implementation(libs.okhttp)
            implementation(libs.ktor.core)
            implementation(libs.kotlin.result)
            implementation(libs.ktor.client.okhttp)
        }

        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
