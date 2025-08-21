plugins {
    id("io.compose.flow.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    id("io.compose.flow.compose.multiplatform")
}

kotlin {
    jvm()
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ai"))
            implementation(project(":core:di"))
            implementation(project(":core:formatter"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(project(":core:kxs-ts-gen-core"))
            implementation(libs.compose.color.picker)
            implementation(libs.compose.navigation)
            implementation(libs.compose.shimmer)
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.kaml)
            implementation(compose.materialIconsExtended)
            implementation(libs.kmpauth.uihelper)
            implementation(libs.koin.core)
            // KotlinPoet moved to JVM-only dependencies
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.jsonpath)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.ktor.core)
            implementation(libs.material.kolor)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.precompose.koin)
            implementation(libs.richeditor.compose)
            implementation(libs.xmlutil.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.google.cloud.storage)
        }
        jvmMain.dependencies {
            implementation(libs.xmlutil.core.jdk)
            implementation(libs.xmlutil.serialization.jvm)
            // KotlinPoet is only available on JVM
            implementation(libs.kotlinpoet)
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}
