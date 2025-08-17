plugins {
    id("io.compose.flow.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    id("io.compose.flow.compose.multiplatform")
}

kotlin {
    jvm("desktop")
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
            implementation(libs.datastore.core.okio)
            implementation(libs.datastore.preferences.core)
            implementation(libs.filekit.compose)
            implementation(libs.kermit)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.kotlinx.atomicfu)
            implementation(project.dependencies.platform("org.http4k:http4k-bom:5.9.0.0"))
            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.http4k.core)
            implementation(libs.http4k.server.netty)
        }

        commonTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        named("desktopMain") {
            kotlin.srcDirs("src/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.content.negotiation)
            }
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
