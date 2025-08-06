package io.composeflow.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import io.composeflow.auth.google.TokenResponse
import io.composeflow.datastore.ANONYMOUSE_USER_ID
import io.composeflow.isAiConfigured
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val firebaseIdTokenModule =
    SerializersModule {
        polymorphic(FirebaseIdToken::class) {
            subclass(FirebaseIdToken.SignedInToken::class, FirebaseIdToken.SignedInToken.serializer())
            subclass(FirebaseIdToken.Anonymouse::class, FirebaseIdToken.Anonymouse.serializer())
            defaultDeserializer { FirebaseIdToken.SignedInToken.serializer() }
        }
    }

@Serializable
sealed interface FirebaseIdToken {
    @Suppress("ktlint:standard:property-naming")
    val user_id: String
    val rawToken: String?

    @Serializable
    data class SignedInToken(
        val name: String,
        val picture: String,
        val iss: String,
        val aud: String,
        @Suppress("ktlint:standard:property-naming")
        val auth_time: Long,
        @Suppress("ktlint:standard:property-naming")
        override val user_id: String,
        val sub: String,
        val iat: Long,
        val exp: Long,
        val email: String,
        @Suppress("ktlint:standard:property-naming")
        val email_verified: Boolean,
        val firebase: JsonElement, // TODO: Define appropriate scheme for each ID provider
        val googleTokenResponse: TokenResponse? = null,
        override val rawToken: String? = null,
    ) : FirebaseIdToken

    @Serializable
    data object Anonymouse : FirebaseIdToken {
        @Suppress("ktlint:standard:property-naming")
        override val user_id: String = ANONYMOUSE_USER_ID
        override val rawToken: String? = null
    }
}

val LocalFirebaseIdToken =
    staticCompositionLocalOf<FirebaseIdToken> {
        FirebaseIdToken.Anonymouse
    }

@Composable
fun isAiEnabled(): Boolean = LocalFirebaseIdToken.current != FirebaseIdToken.Anonymouse && isAiConfigured()

@Composable
fun ProvideFirebaseIdToken(
    firebaseIdToken: FirebaseIdToken,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalFirebaseIdToken provides firebaseIdToken,
    ) {
        content()
    }
}
