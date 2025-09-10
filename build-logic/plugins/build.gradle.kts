plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.bundles.gradle.plugins)
}

gradlePlugin {
    plugins {
        registerPlugin(
            id = "io.compose.flow.kmp.library",
            implementationClass = "MultiplatformLibraryPlugin",
        )
        registerPlugin(
            id = "io.compose.flow.compose.multiplatform",
            implementationClass = "ComposeMultiplatformPlugin",
        )
    }
}

fun NamedDomainObjectContainer<PluginDeclaration>.registerPlugin(
    id: String,
    implementationClass: String,
) {
    // For simplicity, we use id as the name of the plugin declaration
    register(id) {
        this.id = id
        this.implementationClass = implementationClass
    }
}
