import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class MultiplatformLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("kotlin-multiplatform")
            }

            val kotlinMultiplatformExtension = extensions.getByType<KotlinMultiplatformExtension>()
            with(kotlinMultiplatformExtension) {
                // Add compiler flag to suppress expect/actual classes warning
                targets.all {
                    compilations.all {
                        compileTaskProvider.configure {
                            compilerOptions {
                                freeCompilerArgs.add("-Xexpect-actual-classes")
                            }
                        }
                    }
                }
                
                with(sourceSets) {
                    getByName("commonMain").apply {
                        dependencies {
                            implementation(catalogLibs.findLibrary("slf4j.api").get())
                            implementation(catalogLibs.findLibrary("logback.classic").get())
                            implementation(catalogLibs.findLibrary("logback.core").get())
                            implementation(catalogLibs.findLibrary("kermit").get())
                        }
                    }
                }
            }
        }
    }
}

fun DependencyHandlerScope.implementation(artifact: Dependency) {
    add("implementation", artifact)
}

fun DependencyHandlerScope.implementation(artifact: MinimalExternalModuleDependency) {
    add("implementation", artifact)
}

val Project.catalogLibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun KotlinSourceSet.optInKotlinExperimentalApis() {
    languageSettings {
        optIn("kotlin.uuid.ExperimentalUuidApi")
        optIn("kotlinx.coroutines.InternalCoroutinesApi")
    }
}
