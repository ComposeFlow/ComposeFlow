plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ai"))
            implementation(project(":core:analytics"))
            implementation(project(":core:di"))
            implementation(project(":core:kxs-ts-gen-core"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:ui"))
            implementation(project(":core:serializer"))
            implementation(project(":core:billing-client"))
            implementation(project(":feature:uibuilder"))
            implementation(project(":feature:settings"))
            implementation(project(":core:config"))
            implementation(libs.compose.image.loader)
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.filekit.compose)
            implementation(libs.kotlin.result)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
            implementation(libs.conveyor.control)
            api(libs.precompose)
            api(libs.precompose.viewmodel)
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
