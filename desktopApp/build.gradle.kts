import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Locale
import java.util.Properties

plugins {
    id("io.compose.flow.kmp.library")
    id("io.compose.flow.compose.multiplatform")
    alias(libs.plugins.roborazzi)
    id("io.sentry.jvm.gradle") version libs.versions.sentry.jvm.gradle
    id("dev.hydraulic.conveyor") version libs.versions.conveyor
}

group = "io.composeflow"
version = "0.9.2"

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kermit)
            implementation(compose.desktop.currentOs)
            implementation(project(":core:analytics"))
            implementation(project(":core:config"))
            implementation(project(":core:di"))
            implementation(project(":core:logger"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:ui"))
            implementation(project(":feature:top"))
            implementation(project(":feature:uibuilder"))
            implementation(libs.datastore.preferences.core)
            implementation(libs.jewel.int.ui.standalone)
            implementation(libs.jewel.int.ui.decorated.window)
            implementation(libs.ktor.kotlinx.json)
            implementation(libs.sentry)
            implementation(libs.sentry.compose)
            implementation(libs.sentry.compose.desktop)
            implementation(libs.conveyor.control)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":core:di"))
            implementation(project(":core:icons"))
            implementation(project(":core:model"))
            implementation(project(":core:platform"))
            implementation(project(":core:serializer"))
            implementation(project(":core:testing"))
            implementation(project(":feature:app-builder"))
            implementation(project(":feature:uibuilder"))
            implementation(project(":feature:settings"))
            implementation(libs.roborazzi.desktop)
            implementation(libs.kermit.test)
            implementation(libs.kotlin.datetime)
            implementation(libs.testparameterinjector)
            implementation(libs.datastore.preferences.core)
        }
        all {
            optInKotlinExperimentalApis()
        }
    }
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

// To build the distributable with JBR (Jetbrains Runtime), you need to set JAVA_HOME to the JBR
// directory.
// Check it by calling ./gradlew --version and see the JVM is set something like
// ./gradlew --version
//   ...
//   JVM:          17.0.9 (JetBrains s.r.o. 17.0.x.x.xx)
//   ...
compose.desktop {
    application {
        mainClass = "io.composeflow.MainKt"

        buildTypes.release {
            proguard {
                optimize.set(false)
                // The app after applying proguard still sees some runtime issue:
                //  - The image is not displayed properly in the desktop app
                //
                // Also the benefit of using proguard at the moment is not that big since it only
                // reduces the app size by 20MB out of 300MB
                configurationFiles.from(project.file("compose-desktop.pro"))
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ComposeFlow"
            packageVersion = "1.0.0"
            modules(
                "java.instrument",
                "java.logging",
                "java.management",
                "java.naming",
                "java.sql",
                "jdk.compiler",
                "jdk.crypto.ec",
                "jdk.unsupported",
                "jdk.zipfs",
            )

            windows {
                menu = true
                menuGroup = "ComposeFlow"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "AF792DA6-2EA3-495A-95E5-C3C6CBCB9948"
                iconFile.set(project.file("src/jvmMain/resources/ic_composeflow_launcher.ico"))
            }

            macOS {
                // Use -Pcompose.desktop.mac.sign=true to sign and notarize.
                packageName = "ComposeFlow"
                dockName = "ComposeFlow"
                bundleID = "in.composeflow"
                iconFile.set(project.file("src/jvmMain/resources/ic_composeflow_launcher.icns"))

                infoPlist {
                    "CFBundleName" to "ComposeFlow"
                }
            }
            linux {
                menuGroup = "ComposeFlow"
                iconFile.set(project.file("src/jvmMain/resources/ic_composeflow_launcher.png"))
                installationPath = "/opt"
            }
        }
    }
}

val localProps =
    Properties().apply {
        val localPropsFile = File(rootProject.rootDir, "local.properties")
        if (localPropsFile.exists()) {
            load(FileInputStream(localPropsFile))
        }
    }

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "io.composeflow"
    projectName = "ComposeFlow"
    authToken = localProps.getProperty("sentry.authtoken") ?: "authToken not found"
}

dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

tasks.withType(AbstractJLinkTask::class.java).configureEach {
    // Change the property to pass --strip-native-commands flag as `false` to jlink
    // so that jdk commands such as java, javac are included in the packaged distributable
    // to run gradle wrapper with the runtime packaged with the app
    // Using reflection because the field is internal in the class
    val taskClass = AbstractJLinkTask::class.java
    val field = taskClass.getDeclaredField("stripNativeCommands")
    field.isAccessible = true

    @Suppress("UNCHECKED_CAST")
    val property = field.get(this) as Property<Boolean>
    property.set(false)
}

configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}
