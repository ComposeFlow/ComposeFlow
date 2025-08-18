package io.composeflow.auth

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class AuthRepository {
    actual val firebaseIdToken: Flow<FirebaseIdToken?> = flowOf(null)

    actual fun startGoogleSignInFlow() {
        Logger.i("AuthRepository.startGoogleSignInFlow called on WASM - not supported")
    }

    actual fun startFirebaseManagementGrantInFlow() {
        Logger.i("AuthRepository.startFirebaseManagementGrantInFlow called on WASM - not supported")
    }

    actual suspend fun logOut() {
        Logger.i("AuthRepository.logOut called on WASM - not supported")
    }
}
