plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version libs.versions.ksp
    application
}

// Configure JVM toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
}

// Configure KSP options
ksp {
    arg("llmToolsOutputDir", "${project.layout.buildDirectory.get()}/generated/llm-tools")
}

// Configure Kotlin compiler options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Configure application
application {
    mainClass.set("io.composeflow.ksp.example.LlmToolsExample")
}

// Task to run the LlmToolsExample
tasks.register("runExample") {
    group = "examples"
    description = "Run the LlmToolsExample"

    // Create a directory for the example output
    doFirst {
        mkdir("${project.layout.buildDirectory.get()}/generated/llm-tools")
    }

    // Depend on the run task
    dependsOn("run")
}
