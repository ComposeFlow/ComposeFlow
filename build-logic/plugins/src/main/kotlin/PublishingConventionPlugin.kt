import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Convention plugin for publishing ComposeFlow modules to Maven repositories.
 *
 * This plugin:
 * - Applies the maven-publish plugin
 * - Configures a standard publication with ComposeFlow metadata
 * - Uses the module name to determine the artifact ID
 *
 * Apply this plugin to any module that needs to be published:
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
            // For KMP projects, wait for the maven-publish plugin to be applied by KMP
            // Then configure the publications
            pluginManager.withPlugin("maven-publish") {
                afterEvaluate {
                    configure<PublishingExtension> {
                        // Configure all existing Maven publications
                        publications.withType<MavenPublication>().configureEach {
                            // Add POM metadata to all publications
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

                        // Note: For JitPack, no repository configuration is needed here
                        // JitPack builds directly from your GitHub repository
                        // Users just need to add JitPack repository and use the dependency
                    }
                }
            }
        }
    }
}
