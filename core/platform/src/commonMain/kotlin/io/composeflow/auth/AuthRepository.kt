package io.composeflow.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import io.composeflow.auth.google.GoogleOAuth2Client
import io.composeflow.auth.google.TokenResponse
import io.composeflow.di.ServiceLocator
import io.composeflow.logger.logger
import io.composeflow.platform.getOrCreateDataStore
import io.composeflow.ui.openInBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.io.IOException
import java.net.ServerSocket
import java.net.URI
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AuthRepository {
    @OptIn(ExperimentalTime::class)
    val firebaseIdToken: Flow<FirebaseIdToken?> =
        dataStore.data.map { preferences ->
            preferences[serializedFirebaseUserInfoKey]?.let {
                val firebaseIdToken = jsonSerializer.decodeFromString<FirebaseIdToken>(it)

                if (firebaseIdToken is FirebaseIdToken.SignedInToken) {
                    val expirationTime = Instant.fromEpochSeconds(firebaseIdToken.exp)
                    val currentTime = Clock.System.now()

                    if (expirationTime > currentTime) {
                        Logger.i("New firebaseIdToken detected. ${firebaseIdToken.user_id}")
                        firebaseIdToken
                    } else {
                        Logger.i(
                            "FirebaseIdToken expired. ${firebaseIdToken.user_id}, expirationTime: $expirationTime, currentTime: ${Clock.System.now()}",
                        )
                        if (firebaseIdToken.googleTokenResponse != null && firebaseIdToken.googleTokenResponse.refresh_token !== "") {
                            googleOAuth2
                                .refreshToken(firebaseIdToken.googleTokenResponse)
                                .mapBoth({ refreshedToken ->

                                    // Save refreshed token into DataStore
                                    dataStore.edit { prefs ->
                                        prefs[serializedFirebaseUserInfoKey] =
                                            jsonSerializer.encodeToString(refreshedToken)
                                    }
                                    return@let refreshedToken
                                }, { error ->
                                    Logger.e("Failed to refresh token. ", error)
                                    null
                                })
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }
        }

    fun startGoogleSignInFlow() {
        // Open the default web browser to the OAuth authorization URL
        openInBrowser(URI(googleOAuth2.buildAuthUrl()))
    }

    fun startFirebaseManagementGrantInFlow() {
        openInBrowser(URI(googleOAuth2.buildFirebaseManagementGrantUrl()))
    }

    suspend fun logOut() {
        dataStore.edit { preferences ->
            preferences.remove(serializedFirebaseUserInfoKey)
        }
    }

    companion object {
        private val dataStore: DataStore<Preferences> =
            ServiceLocator.getOrPut { getOrCreateDataStore() }

        private val jsonSerializer: Json =
            Json {
                ignoreUnknownKeys = true
                serializersModule = firebaseIdTokenModule
            }
        private val serializedFirebaseUserInfoKey = stringPreferencesKey("firebase_user_info")
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private val callbackPort = findAvailablePort()
        private val googleOAuth2 =
            GoogleOAuth2Client(
                callbackPort = callbackPort,
                OkHttpClient(),
            )

        private fun isPortAvailable(port: Int): Boolean {
            ServerSocket().use { serverSocket ->
                return try {
                    serverSocket.reuseAddress = true
                    serverSocket.bind(java.net.InetSocketAddress(port))
                    true
                } catch (_: IOException) {
                    false
                }
            }
        }

        private fun findAvailablePort(
            startPort: Int = 8090,
            endPort: Int = 8110,
        ): Int {
            var port = startPort
            val end =
                if (startPort > endPort) {
                    startPort
                } else {
                    endPort
                }

            while (port <= end) {
                if (isPortAvailable(port)) {
                    logger.info("port: $port is available for OAuth callback")
                    return port
                }
                logger.info("port: $port isn't available for OAuth callback. Trying different port")
                port += 1
            }
            error("Couldn't find available ports. Shutdown other applications that use :8080")
        }

        // The local server definition that receives the callback as part of OAuth2 flow.
        // Defining this as a singleton to avoid duplicate ports are used
        @Suppress("unused")
        private val callbackServer =
            routes(
                "/callback" bind Method.GET to { request: Request ->
                    val res = request.query("res")

                    if (res != null) {
                        val token = jsonSerializer.decodeFromString<TokenResponse>(res)
                        googleOAuth2
                            .signInWithGoogleIdToken(token)
                            .mapBoth(
                                success = { firebaseIdToken ->
                                    scope.launch {
                                        // Store the serialized FirebaseIdToken to DataStore, which is
                                        // the source of truth of the authorized user
                                        dataStore.edit { preferences ->
                                            preferences[serializedFirebaseUserInfoKey] =
                                                jsonSerializer.encodeToString(firebaseIdToken)
                                        }
                                    }
                                    Response(Status.OK).body("Authorization successful, you can close this window.")
                                },
                                failure = {
                                    Response(
                                        Status.INTERNAL_SERVER_ERROR,
                                    ).body("Internal server error. ${it.message} ${it.stackTraceToString()}")
                                },
                            )
                    } else {
                        Response(Status.BAD_REQUEST).body("Missing 'res' parameter.")
                    }
                },
            ).asServer(Netty(callbackPort)).start()
    }
}
