package io.composeflow.firebase

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.BuildConfig
import io.composeflow.auth.google.TokenResponse
import io.composeflow.di.ServiceLocator
import io.composeflow.http.KtorClientFactory
import io.composeflow.json.jsonSerializer
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Serializable
object EmptyRequest

/**
 * Wrapper class to call the Firebase/Google related APIs.
 * This class is a workaround because some of the APIs are not supported by Java SdK doesn't
 * (e.g. managing web apps in a Firebase project).
 */
class FirebaseApiCaller(
    private val httpClient: io.ktor.client.HttpClient = KtorClientFactory.create(),
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Unconfined
        },
) {
    suspend fun getWebAppConfig(
        firebaseProjectId: String,
        appId: String,
        tokenResponse: TokenResponse,
    ): Result<String?, Throwable> =
        runCatching {
            withContext(ioDispatcher) {
                val refreshedTokenResponse =
                    obtainAccessTokenWithRefreshToken(tokenResponse.refresh_token!!)

                val url =
                    "https://firebase.googleapis.com/v1beta1/projects/$firebaseProjectId/webApps/$appId/config"

                val response = httpClient.get(url) {
                    headers {
                        append(
                            HttpHeaders.Authorization,
                            "Bearer ${refreshedTokenResponse?.access_token}"
                        )
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                }

                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    Logger.e("Get web Apps failed. Code: ${response.status.value}, Body: $errorBody")
                    null
                } else {
                    response.bodyAsText()
                }
            }
        }

    suspend fun listAndroidApps(identifier: FirebaseAppIdentifier): Result<ListAndroidAppsResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: listAndroidApps")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/androidApps",
        )
    }

    suspend fun getAndroidAppConfig(
        identifier: FirebaseAppIdentifier,
        appId: String,
    ): Result<AppConfig?, Throwable> {
        Logger.i("FirebaseAPICaller: getAndroidAppConfig. appId: $appId")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/androidApps/$appId/config",
        )
    }

    suspend fun createAndroidApp(
        identifier: FirebaseAppIdentifier,
        packageName: String,
        displayName: String,
    ): Result<OperationResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: createAndroidApp. DisplayName: $displayName, PackageName: $packageName")
        return callApi(
            identifier = identifier,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/androidApps",
            requestBody = AndroidAppRequest(packageName = packageName, displayName = displayName),
        )
    }

    suspend fun listIosApps(identifier: FirebaseAppIdentifier): Result<ListIosAppsResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: listIosApps")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/iosApps",
        )
    }

    suspend fun getIosAppConfig(
        identifier: FirebaseAppIdentifier,
        appId: String,
    ): Result<AppConfig?, Throwable> {
        Logger.i("FirebaseAPICaller: getIosAppConfig. appId: $appId")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/iosApps/$appId/config",
        )
    }

    suspend fun createIosApp(
        identifier: FirebaseAppIdentifier,
        bundleId: String,
        displayName: String,
    ): Result<OperationResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: createIosApp. DisplayName: $displayName, BundleId: $bundleId")
        return callApi(
            identifier = identifier,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/iosApps",
            requestBody = IosAppRequest(bundleId = bundleId, displayName = displayName),
        )
    }

    suspend fun listWebApps(identifier: FirebaseAppIdentifier): Result<ListWebAppsResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: listWebApps")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/webApps",
        )
    }

    suspend fun getWebAppConfig(
        identifier: FirebaseAppIdentifier,
        appId: String,
    ): Result<String?, Throwable> {
        Logger.i("FirebaseAPICaller: getWebAppConfig. appId: $appId")
        return callApi(
            identifier = identifier,
            requestBody = EmptyRequest,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/webApps/$appId/config",
        )
    }

    suspend fun createWebApp(
        identifier: FirebaseAppIdentifier,
        displayName: String,
    ): Result<OperationResponse?, Throwable> {
        Logger.i("FirebaseAPICaller: createWebApps. DisplayName: $displayName")
        return callApi(
            identifier = identifier,
            endPoint = "https://firebase.googleapis.com/v1beta1/projects/${identifier.firebaseProjectId}/webApps",
            requestBody = WebAppRequest(displayName = displayName),
        )
    }

    suspend fun checkIdentityPlatformEnabled(identifier: FirebaseAppIdentifier): Result<Boolean?, Throwable> =
        runCatching {
            Logger.i("FirebaseAPICaller: checkIdentityPlatformEnabled")
            val refreshedTokenResponse =
                obtainAccessTokenWithRefreshToken(identifier.googleTokenResponse.refresh_token!!)

            val endPoint =
                "https://identitytoolkit.googleapis.com/admin/v2/projects/${identifier.firebaseProjectId}/config"
            val response = httpClient.get(endPoint) {
                headers {
                    append(
                        HttpHeaders.Authorization,
                        "Bearer ${refreshedTokenResponse?.access_token}"
                    )
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }

            withContext(ioDispatcher) {
                if (response.status.isSuccess()) {
                    true
                } else {
                    val errorBody = response.bodyAsText()
                    Logger.e("Check identity Platform Enabled failed. Code: ${response.status.value}, Body: $errorBody")
                    false
                }
            }
        }

    private suspend inline fun <reified Request, reified Response> callApi(
        identifier: FirebaseAppIdentifier,
        requestBody: Request,
        endPoint: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    ): Result<Response?, Throwable> =
        runCatching {
            val refreshedTokenResponse =
                obtainAccessTokenWithRefreshToken(identifier.googleTokenResponse.refresh_token!!)

            val response = withContext(dispatcher) {
                if (requestBody !is EmptyRequest) {
                    val requestBodyString =
                        jsonSerializer.encodeToString(serializer<Request>(), requestBody)
                    httpClient.post(endPoint) {
                        headers {
                            append(
                                HttpHeaders.Authorization,
                                "Bearer ${refreshedTokenResponse?.access_token}"
                            )
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                        setBody(requestBodyString)
                    }
                } else {
                    httpClient.get(endPoint) {
                        headers {
                            append(
                                HttpHeaders.Authorization,
                                "Bearer ${refreshedTokenResponse?.access_token}"
                            )
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                    }
                }
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Logger.e("API call failed. Code: ${response.status.value}, Body: $errorBody. Endpoint: $endPoint, Request: $requestBody")
                val operationResponse =
                    jsonSerializer.decodeFromString<OperationResponse>(errorBody)
                operationResponse.error?.let { error ->
                    throw Exception("Project ID: \"${identifier.firebaseProjectId}\": ${error.message}")
                }
            } else {
                val responseBody = response.bodyAsText()
                if (isPrimitiveType(Response::class)) {
                    responseBody as Response?
                } else {
                    jsonSerializer.decodeFromString(
                        serializer<Response>(),
                        responseBody,
                    )
                }
            }
        }

    suspend fun obtainAccessTokenWithRefreshToken(refreshToken: String): TokenResponse? {
        val url = "${BuildConfig.AUTH_ENDPOINT}/google/token"

        return withContext(Dispatchers.Unconfined) {
            val response = httpClient.submitForm(
                url = url,
                formParameters = parameters {
                    append("refresh_token", refreshToken)
                }
            )

            val responseBody = response.bodyAsText()
            if (!response.status.isSuccess()) {
                Logger.e("Failed to obtain access token. HTTP code: ${response.status.value}")
                Logger.e("Response body: $responseBody")
                return@withContext null
            }

            val tokenResponse = jsonSerializer.decodeFromString<TokenResponse>(responseBody)
            tokenResponse
        }
    }

    private fun isPrimitiveType(kClass: KClass<*>): Boolean =
        when (kClass) {
            String::class,
            Int::class,
            Long::class,
            Short::class,
            Byte::class,
            Double::class,
            Float::class,
            Boolean::class,
            Char::class,
                -> true

            else -> false
        }
}
