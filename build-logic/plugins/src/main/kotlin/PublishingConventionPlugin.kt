import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

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
            // Apply maven-publish plugin
            pluginManager.apply("maven-publish")
            
            // Configure publishing after the project is evaluated
            afterEvaluate {
                configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("maven") {
                            // Determine artifact ID from project path
                            // e.g., ":core:model" becomes "core-model"
                            val projectPath = project.path.substring(1) // Remove leading ":"
                            val artifactId = projectPath.replace(":", "-")
                            
                            groupId = "io.composeflow"
                            this.artifactId = artifactId
                            version = project.findProperty("version")?.toString() ?: "local-SNAPSHOT"
                            
                            // Use the Kotlin Multiplatform component
                            from(components["kotlin"])
                            
                            pom {
                                // Format module name for display (e.g., "core-model" -> "Core Model")
                                val displayName = artifactId.split("-")
                                    .joinToString(" ") { it.capitalize() }
                                
                                name.set("ComposeFlow $displayName")
                                description.set("$displayName module for ComposeFlow")
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
            }
        }
    }
}