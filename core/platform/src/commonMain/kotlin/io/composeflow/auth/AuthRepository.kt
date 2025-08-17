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
import io.composeflow.http.KtorClientFactory
import io.composeflow.platform.Uri
import io.composeflow.platform.getOrCreateDataStore
import io.composeflow.platform.toJavaUri
import io.composeflow.ui.openInBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import io.composeflow.auth.OAuthServer as OAuthServerImpl

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
        openInBrowser(Uri(googleOAuth2.buildAuthUrl()).toJavaUri())
    }

    fun startFirebaseManagementGrantInFlow() {
        openInBrowser(Uri(googleOAuth2.buildFirebaseManagementGrantUrl()).toJavaUri())
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
        private lateinit var oauthServer: OAuthServerImpl
        private lateinit var googleOAuth2: GoogleOAuth2Client
        private var callbackPort: Int = 0

        // Initialize the OAuth callback server
        init {
            callbackPort = OAuthServerImpl.findAvailablePort(startPort = 8090, endPort = 8110)
            oauthServer = OAuthServerImpl.create()
            googleOAuth2 = GoogleOAuth2Client(
                callbackPort = callbackPort,
                KtorClientFactory.create(),
            )

            oauthServer.start(callbackPort) { token: TokenResponse ->
                // Use runBlocking to wait for the authentication result
                val authResult = runBlocking {
                    googleOAuth2.signInWithGoogleIdToken(token)
                }

                authResult.mapBoth(
                    success = { firebaseIdToken: FirebaseIdToken ->
                        // Store the serialized FirebaseIdToken to DataStore
                        scope.launch {
                            dataStore.edit { preferences ->
                                preferences[serializedFirebaseUserInfoKey] =
                                    jsonSerializer.encodeToString(firebaseIdToken)
                            }
                        }
                        "Authorization successful, you can close this window."
                    },
                    failure = { error ->
                        Logger.e("Sign in failed: ${error.message}", error)
                        "Internal server error. ${error.message}"
                    },
                )
            }
        }
    }
}
