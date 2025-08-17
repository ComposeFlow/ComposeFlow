import java.io.FileInputStream
import java.util.Properties

plugins {
    id("io.compose.flow.kmp.library")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}

val localProps =
    Properties().apply {
        val propertiesFileName =
            if (project.hasProperty("release")) {
                "local.prod.properties"
            } else {
                "local.properties"
            }

        val localPropsFile = File(rootProject.rootDir, propertiesFileName)
        if (localPropsFile.exists()) {
            load(FileInputStream(localPropsFile))
        } else {
            println("Properties file $propertiesFileName not found!")
        }
    }

buildConfig {
    packageName("io.composeflow")

    useKotlinOutput()
    val authEndpoint =
        localProps.getProperty(
            "auth.endpoint",
            "", // Configure in local.properties for your own auth service
        )
    val llmEndpoint =
        localProps.getProperty(
            "llm.endpoint",
            "", // Configure in local.properties for your own LLM service
        )
    val firebaseApiKey =
        localProps.getProperty(
            "firebase.api.key",
            "", // Configure in local.properties with your Firebase API key
        )
    val googleCloudStorageProjectId =
        localProps.getProperty(
            "google.clouod.storage.project.id",
            "", // Configure in local.properties with your GCS project ID
        )
    val googleCloudStorageBucketName =
        localProps.getProperty(
            "google.clouod.storage.bucket.name",
            "", // Configure in local.properties with your GCS bucket name
        )
    buildConfigField(
        "String",
        "AUTH_ENDPOINT",
        "\"${authEndpoint}\"",
    )
    buildConfigField(
        "String",
        "LLM_ENDPOINT",
        "\"${llmEndpoint}\"",
    )
    buildConfigField(
        "String",
        "FIREBASE_API_KEY",
        "\"${firebaseApiKey}\"",
    )
    buildConfigField(
        "String",
        "GOOGLE_CLOUD_STORAGE_PROJECT_ID",
        "\"${googleCloudStorageProjectId}\"",
    )
    buildConfigField(
        "String",
        "GOOGLE_CLOUD_STORAGE_BUCKET_NAME",
        "\"${googleCloudStorageBucketName}\"",
    )
    buildConfigField("boolean", "isRelease", "${project.hasProperty("release")}")
    val billingEndpoint =
        localProps.getProperty(
            "billing.endpoint",
            "", // Configure in local.properties for your own billing service
        )
    buildConfigField(
        "String",
        "BILLING_ENDPOINT",
        "\"${billingEndpoint}\"",
    )
    val sentryDsn =
        localProps.getProperty(
            "sentry.dsn",
            "",
        )
    buildConfigField(
        "String",
        "SENTRY_DSN",
        "\"${sentryDsn}\"",
    )
    val postHogApiKey =
        localProps.getProperty(
            "posthog.api.key",
            "",
        )
    buildConfigField(
        "String",
        "POSTHOG_API_KEY",
        "\"${postHogApiKey}\"",
    )
    useKotlinOutput { internalVisibility = false }
}
