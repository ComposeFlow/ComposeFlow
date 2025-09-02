import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Convention plugin for configuring KMP publications for JitPack.
 * 
 * This plugin:
 * - Waits for KMP to create its publications
 * - Configures the group ID for JitPack compatibility
 * - Adds POM metadata
 * 
 * Apply this plugin to any module that needs custom publishing configuration:
 * ```
 * plugins {
 *     id("io.compose.flow.kmp.library")
 *     id("io.compose.flow.publishing")
 * }
 * ```
 */
class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Wait for maven-publish to be applied by KMP
            pluginManager.withPlugin("maven-publish") {
                afterEvaluate {
                    configure<PublishingExtension> {
                        // Configure ALL existing publications created by KMP
                        publications.withType<MavenPublication>().configureEach {
                            // Set consistent group ID for JitPack
                            groupId = "com.github.ComposeFlow.ComposeFlow"
                            
                            // Set artifact ID based on publication name
                            // KMP creates: kotlinMultiplatform, jvm, js, etc.
                            artifactId = when (name) {
                                "kotlinMultiplatform" -> "core-${project.name}"
                                else -> "core-${project.name}-$name"
                            }
                            
                            // Add POM metadata
                            pom {
                                name.set("ComposeFlow ${project.name}")
                                description.set("${project.name} module for ComposeFlow")
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
                }
            }
        }
    }
}