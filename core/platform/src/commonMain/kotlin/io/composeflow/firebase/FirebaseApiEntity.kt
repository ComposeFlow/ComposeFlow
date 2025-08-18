package io.composeflow.firebase

import io.composeflow.firebase.management.FIREBASE_CONSOLE_URL
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidAppRequest(
    val packageName: String,
    val displayName: String,
)

@Serializable
data class AndroidAppMetadata(
    val name: String,
    val appId: String,
    val projectId: String,
    val packageName: String,
    val displayName: String? = null,
) {
    fun buildUrl(): String = "${FIREBASE_CONSOLE_URL}project/$projectId/settings/general/android:$packageName"
}

@Serializable
data class ListAndroidAppsResponse(
    val apps: List<AndroidAppMetadata>? = null,
    val nextPageToken: String? = null,
)

@Serializable
data class AppConfig(
    val configFilename: String,
    val configFileContents: String,
) {
    fun decodeFileContents(): String = configFileContents.decodeBase64Bytes().decodeToString()
}

@Serializable
data class IosAppRequest(
    val bundleId: String,
    val displayName: String,
)

@Serializable
data class IosAppMetadata(
    val name: String,
    val appId: String,
    val projectId: String,
    val bundleId: String,
    val displayName: String? = null,
) {
    fun buildUrl(): String = "${FIREBASE_CONSOLE_URL}project/$projectId/settings/general/ios:$bundleId"
}

@Serializable
data class ListIosAppsResponse(
    val apps: List<IosAppMetadata>? = null,
    val nextPageToken: String? = null,
)

@Serializable
data class WebAppRequest(
    val displayName: String,
)

@Serializable
data class OperationResponse(
    val name: String? = null,
    val done: Boolean? = false,
    val response: String? = null,
    val error: OperationError? = null,
)

@Serializable
data class WebAppWrapper(
    val metadata: WebAppMetadata,
    val config: String,
)

@Serializable
data class WebAppMetadata(
    val name: String,
    val appId: String,
    val projectId: String,
    val displayName: String,
    val appUrls: List<String>? = null,
    val apiKeyId: String? = null,
) {
    fun buildUrl(): String = "${FIREBASE_CONSOLE_URL}project/$projectId/settings/general/web:$appId"
}

@Serializable
data class ListWebAppsResponse(
    val apps: List<WebAppMetadata>? = null,
    val nextPageToken: String? = null,
)

@Serializable
data class OperationError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
    val details: List<ErrorDetail>? = null,
)

@Serializable
data class ErrorDetail(
    @SerialName("@type") val type: String,
    val reason: String? = null,
    val domain: String? = null,
    val metadata: Metadata? = null,
)

@Serializable
data class Metadata(
    val service: String? = null,
    val method: String? = null,
)
