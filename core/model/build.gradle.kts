plugins {
    id("io.compose.flow.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    id("io.compose.flow.compose.multiplatform")
    `maven-publish`
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ai"))
            implementation(project(":core:di"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:ui"))
            implementation(project(":core:kxs-ts-gen-core"))
            implementation(libs.compose.color.picker)
            implementation(libs.compose.navigation)
            implementation(libs.compose.shimmer)
            implementation(libs.kaml)
            implementation(compose.materialIconsExtended)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.jsonpath)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.result)
            implementation(libs.ktor.core)
            implementation(libs.ktor.utils)
            implementation(libs.material.kolor)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.precompose.koin)
            implementation(libs.reorderable)
            implementation(libs.richeditor.compose)
            implementation(libs.xmlutil.core)
        }
        jvmMain.dependencies {
            implementation(libs.xmlutil.core.jdk)
            implementation(libs.xmlutil.serialization.jvm)
            // KotlinPoet is only available on JVM
            implementation(libs.kotlinpoet)
            // Code formatting dependencies
            implementation(libs.compose.code.editor)
            implementation(libs.ktlint.core)
            implementation(libs.ktlint.ruleset.standard)
            // Reflection for ModifierHelper
            implementation(kotlin("reflect"))
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.google.cloud.storage)
        }
        all {
            optInComposeExperimentalApis()
            optInKotlinExperimentalApis()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.composeflow"
            artifactId = "core-model"
            version = project.findProperty("version")?.toString() ?: "local-SNAPSHOT"

            from(components["kotlin"])

            pom {
                name.set("ComposeFlow Core Model")
                description.set("Core domain models for ComposeFlow")
                url.set("https://github.com/ComposeFlow/ComposeFlow")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("composeflow")
                        name.set("ComposeFlow Team")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/ComposeFlow/ComposeFlow.git")
                    developerConnection.set("scm:git:ssh://github.com/ComposeFlow/ComposeFlow.git")
                    url.set("https://github.com/ComposeFlow/ComposeFlow")
                }
            }
        }
    }

    // Note: For JitPack, no repository configuration is needed here
    // JitPack builds directly from your GitHub repository
    // Users just need to add JitPack repository and use the dependency
}
