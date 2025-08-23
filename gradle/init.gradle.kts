val ktlintVersion = "1.6.0"
val spotlessExcludedProjects =
    setOf(
        ":core:kxs-ts-gen-core",
    )

initscript {
    val spotlessVersion = "7.0.4"

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:$spotlessVersion")
    }
}

rootProject {
    allprojects {
        if (project.path !in spotlessExcludedProjects) {
            apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
            extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
                kotlin {
                    target(
                        "src/**/*.kt",
                        "src/**/*.kts",
                    )
                    targetExclude(
                        "src/**/*main*.kt",
                        "src/commonMain/kotlin/io/composeflow/custom/**/*.kt",
                    )
                    ktlint(ktlintVersion)
                }
                kotlinGradle {
                    ktlint(ktlintVersion)
                }
            }
        }
    }
}
