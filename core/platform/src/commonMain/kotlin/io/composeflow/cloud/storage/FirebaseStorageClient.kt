package io.composeflow.cloud.storage

import co.touchlab.kermit.Logger
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.IO
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

    suspend fun isAvailable(): Boolean =
        try {
            val firebaseIdToken =
                authRepository.firebaseIdToken
                    .take(1)
                    .last()
            firebaseIdToken is FirebaseIdToken.SignedInToken
        } catch (e: Exception) {
            false
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
                    HttpUrl
                        .Builder()
                        .apply {
                            scheme("https")
                            host(host)
                            addPathSegment(version)
                            addPathSegments(location.bucketOnlyServerUrl())
                            if (location.isRoot()) {
                                addQueryParameter("prefix", "")
                            } else {
                                addQueryParameter("prefix", location.path)
                            }
                            addQueryParameter("delimiter", "/")
                            if (pageToken != null) {
                                addQueryParameter("pageToken", pageToken)
                            }
                        }.build()
                val request =
                    Request
                        .Builder()
                        .apply {
                            url(url)
                            get()
                            addHeader("Authorization", "Firebase $token")
                            addHeader("X-Firebase-Storage-Version", clientVersion)
                        }.build()
                okHttpClient.newCall(request).execute().use { response ->
                    response.body.string().let {
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
        }

    suspend fun getMetadata(location: Location): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                HttpUrl
                    .Builder()
                    .apply {
                        scheme("https")
                        host(host)
                        addPathSegment(version)
                        addPathSegment("b")
                        addPathSegment(location.bucket)
                        addPathSegment("o")
                        addPathSegment(location.path)
                    }.build()

            val request =
                Request
                    .Builder()
                    .apply {
                        url(url)
                        get()
                        addHeader("Authorization", "Firebase $token")
                        addHeader("X-Firebase-Storage-Version", clientVersion)
                    }.build()

            okHttpClient.newCall(request).execute().use { response ->
                response.body.string().let { body ->
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
                HttpUrl
                    .Builder()
                    .apply {
                        scheme("https")
                        host(host)
                        addPathSegment(version)
                        addPathSegment("b")
                        addPathSegment(location.bucket)
                        addPathSegment("o")
                        addPathSegment(location.path)
                        addQueryParameter("alt", "media")
                    }.build()

            val request =
                Request
                    .Builder()
                    .apply {
                        url(url)
                        get()
                        addHeader("Authorization", "Firebase $token")
                        addHeader("X-Firebase-Storage-Version", clientVersion)
                    }.build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.code == 200) {
                    response.body.bytes().let { bytes ->
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
                                contentBytes = bytes,
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
                HttpUrl
                    .Builder()
                    .apply {
                        scheme("https")
                        host(host)
                        addPathSegment(version)
                        addPathSegment("b")
                        addPathSegment(location.bucket)
                        addPathSegment("o")
                        addPathSegment(location.path)
                    }.build()

            val uploadMetadata =
                UploadMetadata(
                    name = blobInfo.fileName,
                    contentType = contentType ?: "application/octet-stream",
                    metadata = metadata,
                )

            val requestBody =
                MultipartBody
                    .Builder()
                    .apply {
                        setType(MultipartBody.FORM)
                        addFormDataPart(
                            "name",
                            "",
                            Json
                                .encodeToString(uploadMetadata)
                                .toByteArray()
                                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                        )
                        addFormDataPart(
                            "name",
                            "",
                            contentByes.toRequestBody("mime/type".toMediaTypeOrNull()),
                        )
                    }.build()

            val request =
                Request
                    .Builder()
                    .apply {
                        url(url)
                        post(requestBody)
                        addHeader("X-Goog-Upload-Protocol", "multipart")
                        addHeader("Authorization", "Firebase $token")
                        addHeader("X-Firebase-Storage-Version", clientVersion)
                    }.build()

            okHttpClient.newCall(request).execute().use { response ->
                response.body.string().let { body ->
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
        }

        return res ?: throw Exception("No res")
    }

    suspend fun delete(location: Location) {
        withContext(ioDispatcher) {
            val token = getAuthToken()
            val url =
                HttpUrl
                    .Builder()
                    .apply {
                        scheme("https")
                        host(host)
                        addPathSegment(version)
                        addPathSegment("b")
                        addPathSegment(location.bucket)
                        addPathSegment("o")
                        addPathSegment(location.path)
                    }.build()

            val request =
                Request
                    .Builder()
                    .apply {
                        url(url)
                        delete()
                        addHeader("Authorization", "Firebase $token")
                        addHeader("X-Firebase-Storage-Version", clientVersion)
                    }.build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.code != 204) {
                    response.body.string().let { body ->
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
}
