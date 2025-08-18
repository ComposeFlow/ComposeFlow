package io.composeflow.auth

import kotlinx.coroutines.flow.Flow

expect class AuthRepository() {
    val firebaseIdToken: Flow<FirebaseIdToken?>

    fun startGoogleSignInFlow()

    fun startFirebaseManagementGrantInFlow()

    suspend fun logOut()
}
