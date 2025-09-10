plugins {
    id("io.compose.flow.kmp.library")
    alias(libs.plugins.kotlin.serialization)
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
            implementation(project(":core:di"))
            implementation(project(":core:config"))
            implementation(project(":core:logger"))
            implementation(project(":core:serializer"))

            api(project.dependencies.platform(libs.google.cloud.bom))
            api(libs.google.cloud.storage)
            // Replace OkHttp with Ktor for multiplatform support
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.utils)

            implementation(libs.google.firebase.admin)
            implementation(libs.commons.compress)
            implementation(libs.filekit.compose)
            implementation(libs.kermit)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.kotlinx.atomicfu)
            implementation(libs.kotlinx.serialization.json)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        jvmMain {
            kotlin.srcDirs("src/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.datastore.preferences.core)
                implementation(libs.gradle.tooling.api)
            }
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        
        wasmJsTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
