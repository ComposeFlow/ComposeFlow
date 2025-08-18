plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:di"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(libs.compose.shimmer)
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.filekit.compose)
            implementation(libs.kaml)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.kotlinpoet)
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
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
