package io.composeflow.cloud.storage

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.di.ServiceLocator
import io.composeflow.http.KtorClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant

class AnonymousUserException(
    message: String = "Firebase Storage operations are not available for anonymous users. Please sign in to access cloud storage.",
) : Exception(message)

@OptIn(kotlin.time.ExperimentalTime::class)
class FirebaseStorageClient(
    val authRepository: AuthRepository = AuthRepository(),
    val host: String = "firebasestorage.googleapis.com",
    val version: String = "v0",
    private val clientVersion: String = "webjs/11.3.0",
    private val httpClient: HttpClient = KtorClientFactory.create(),
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.Default
        },
) {
    private suspend fun getAuthToken(): String {
        val firebaseIdToken =
            authRepository.firebaseIdToken
                .take(1)
                .last()

        return when (firebaseIdToken) {
            is FirebaseIdToken.SignedInToken -> {
                firebaseIdToken.rawToken ?: throw Exception("Signed-in user has no raw token")
            }

            is FirebaseIdToken.Anonymouse -> {
                Logger.i("Anonymous user attempted Firebase Storage operation. Feature not available for anonymous users.")
                throw AnonymousUserException()
            }

            null -> {
                Logger.i("No authenticated user found for Firebase Storage operation.")
                throw AnonymousUserException()
            }
        }
    }

    suspend fun listAll(location: Location): ListResult {
        val accumulator =
            ListResult(
                prefixes = mutableListOf(),
                items = mutableListOf(),
            )

        listAllHelper(location, accumulator)

        return accumulator
    }

    private suspend fun listAllHelper(
        location: Location,
        accumulator: ListResult,
        pageToken: String? = null,
    ): Result<Unit, Throwable> =
        runCatching {
            withContext(ioDispatcher) {
                // TODO: Consider a case when the token expires
                val token = getAuthToken()
                val url =
                    URLBuilder()
                        .apply {
                            protocol = io.ktor.http.URLProtocol.HTTPS
                            host = this@FirebaseStorageClient.host
                            pathSegments =
                                buildList {
                                    add(version)
                                    add("b")
                                    add(location.bucket)
                                    add("o")
                                }
                        }.build()

                val authHeader = "Firebase $token"

                val response =
                    httpClient.get(url) {
                        parameter("prefix", if (location.isRoot()) "" else location.path)
                        parameter("delimiter", "/")
                        if (pageToken != null) {
                            parameter("pageToken", pageToken)
                        }
                        header(HttpHeaders.Authorization, authHeader)
                        header("X-Firebase-Storage-Version", clientVersion)
                    }

                val responseBody = response.bodyAsText()
                responseBody.let {
                    val jsonElement = Json.parseToJsonElement(it)

                    val error = jsonElement.jsonObject["error"]
                    if (error != null) {
                        val code = error.jsonObject["code"]
                        val message = error.jsonObject["message"]
                        throw Exception("code: $code, message: $message")
                    }

                    val prefixesArray = jsonElement.jsonObject["prefixes"]?.jsonArray
                    val itemsArray = jsonElement.jsonObject["items"]?.jsonArray
                    val nextPageToken =
                        jsonElement.jsonObject["nextPageToken"]?.jsonPrimitive?.content
                    if (prefixesArray != null) {
                        accumulator.prefixes.addAll(
                            prefixesArray.map { prefix ->
                                val loc = Location(location.bucket, prefix.toString())
                                Reference(
                                    loc,
                                )
                            },
                        )
                    }
                    if (itemsArray != null) {
                        accumulator.items.addAll(
                            itemsArray.map { item ->
                                val loc =
                                    Location(
                                        location.bucket,
                                        item.jsonObject["name"]?.jsonPrimitive?.content ?: "",
                                    )
                                Reference(
                                    loc,
                                )
                            },
                        )
                    }

                    if (nextPageToken != null) {
                        listAllHelper(location, accumulator, nextPageToken)
                    }
                }
            }
        }

    suspend fun getMetadata(location: Location): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                URLBuilder()
                    .apply {
                        protocol = io.ktor.http.URLProtocol.HTTPS
                        host = this@FirebaseStorageClient.host
                        pathSegments =
                            buildList {
                                add(version)
                                add("b")
                                add(location.bucket)
                                add("o")
                                add(location.path)
                            }
                    }.build()

            val response =
                httpClient.get(url) {
                    header(HttpHeaders.Authorization, "Firebase $token")
                    header("X-Firebase-Storage-Version", clientVersion)
                }

            val body = response.bodyAsText()
            val jsonElement = Json.parseToJsonElement(body)

            val error = jsonElement.jsonObject["error"]
            if (error != null) {
                val code = error.jsonObject["code"]
                val message = error.jsonObject["message"]
                throw Exception("code: $code, message: $message")
            }

            jsonElement.jsonObject.forEach { (key, value) ->
                metadata[key] = value.jsonPrimitive.content
            }
        }
        return metadata
    }

    suspend fun get(location: Location): BlobInfoWrapper {
        val metadata = getMetadata(location)

        var res: BlobInfoWrapper? = null
        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                URLBuilder()
                    .apply {
                        protocol = io.ktor.http.URLProtocol.HTTPS
                        host = this@FirebaseStorageClient.host
                        pathSegments =
                            buildList {
                                add(version)
                                add("b")
                                add(location.bucket)
                                add("o")
                                add(location.path)
                            }
                    }.build()

            val response =
                httpClient.get(url) {
                    parameter("alt", "media")
                    header(HttpHeaders.Authorization, "Firebase $token")
                    header("X-Firebase-Storage-Version", clientVersion)
                }

            if (response.status.value == 200) {
                val bytes = response.readRawBytes()
                bytes.let {
                    res =
                        BlobInfoWrapper(
                            blobId =
                                BlobIdWrapper(
                                    bucket = location.bucket,
                                    name = location.path,
                                    generation = metadata["generation"]?.toLong() ?: 0,
                                ),
                            fileName = location.filename(),
                            folderName = location.folderName(),
                            mediaLink = url.toString(),
                            size = metadata["size"]?.toLong() ?: 0,
                            contentBytes = it,
                            createTime =
                                metadata["timeCreated"]?.let {
                                    Instant.parse(it)
                                },
                            updateTime =
                                metadata["updated"]?.let {
                                    Instant.parse(it)
                                },
                        )
                }
            }
        }

        return res ?: throw Exception("No res")
    }

    suspend fun upload(
        location: Location,
        blobInfo: BlobInfoWrapper,
        metadata: Map<String, String>? = null,
        contentType: String? = null,
    ): BlobInfoWrapper {
        var res: BlobInfoWrapper? = null
        val contentByes = blobInfo.contentBytes ?: throw Exception("No content")

        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                URLBuilder()
                    .apply {
                        protocol = io.ktor.http.URLProtocol.HTTPS
                        host = this@FirebaseStorageClient.host
                        pathSegments =
                            buildList {
                                add(version)
                                add("b")
                                add(location.bucket)
                                add("o")
                                add(location.path)
                            }
                    }.build()

            val uploadMetadata =
                UploadMetadata(
                    name = blobInfo.fileName,
                    contentType = contentType ?: "application/octet-stream",
                    metadata = metadata,
                )

            val response =
                httpClient.submitFormWithBinaryData(
                    url = url.toString(),
                    formData =
                        formData {
                            append("metadata", Json.encodeToString(uploadMetadata))
                            append(
                                "file",
                                contentByes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, contentType ?: "application/octet-stream")
                                    append(HttpHeaders.ContentDisposition, "filename=\"${blobInfo.fileName}\"")
                                },
                            )
                        },
                ) {
                    header("X-Goog-Upload-Protocol", "multipart")
                    header(HttpHeaders.Authorization, "Firebase $token")
                    header("X-Firebase-Storage-Version", clientVersion)
                }

            val body = response.bodyAsText()
            body.let {
                val jsonElement = Json.parseToJsonElement(body)

                val error = jsonElement.jsonObject["error"]
                if (error != null) {
                    val code = error.jsonObject["code"]
                    val message = error.jsonObject["message"]
                    throw Exception("code: $code, message: $message")
                }

                res =
                    BlobInfoWrapper(
                        blobId =
                            BlobIdWrapper(
                                bucket = location.bucket,
                                name = location.path,
                                generation =
                                    jsonElement.jsonObject["generation"]
                                        ?.jsonPrimitive
                                        ?.content
                                        ?.toLong()
                                        ?: 0,
                            ),
                        fileName = location.filename(),
                        folderName = location.folderName(),
                        mediaLink = url.toString(),
                        size =
                            jsonElement.jsonObject["size"]
                                ?.jsonPrimitive
                                ?.content
                                ?.toLong()
                                ?: 0,
                        contentBytes = blobInfo.contentBytes,
                        createTime =
                            jsonElement.jsonObject["timeCreated"]?.jsonPrimitive?.content?.let {
                                Instant.parse(it)
                            },
                        updateTime =
                            jsonElement.jsonObject["updated"]?.jsonPrimitive?.content?.let {
                                Instant.parse(it)
                            },
                    )
            }
        }

        return res ?: throw Exception("No res")
    }

    suspend fun delete(location: Location) {
        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                URLBuilder()
                    .apply {
                        protocol = io.ktor.http.URLProtocol.HTTPS
                        host = this@FirebaseStorageClient.host
                        pathSegments =
                            buildList {
                                add(version)
                                add("b")
                                add(location.bucket)
                                add("o")
                                add(location.path)
                            }
                    }.build()

            val response =
                httpClient.delete(url) {
                    header(HttpHeaders.Authorization, "Firebase $token")
                    header("X-Firebase-Storage-Version", clientVersion)
                }

            if (response.status.value != 204) {
                val body = response.bodyAsText()
                body.let {
                    val jsonElement = Json.parseToJsonElement(body)

                    val error = jsonElement.jsonObject["error"]
                    if (error != null) {
                        val code = error.jsonObject["code"]
                        val message = error.jsonObject["message"]
                        throw Exception("code: $code, message: $message")
                    }
                }
            }
        }
    }
}
