package io.composeflow

import io.composeflow.analytics.Analytics
import io.composeflow.analytics.AnalyticsTracker
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.datastore.LocalFirstProjectSaver
import io.composeflow.datastore.ProjectSaver
import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class ComposeFlowAppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val projectSaver: ProjectSaver = LocalFirstProjectSaver(),
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val loginResultUiState: StateFlow<LoginResultUiState> =
        authRepository.firebaseIdToken
            .onEach { token ->
                // Handle analytics user identification on authentication state changes
                try {
                    val analytics = ServiceLocator.get<Analytics>()
                    if (token != null) {
                        // User logged in - identify user for analytics
                        // Only send non-PII data to comply with privacy practices
                        analytics.identify(
                            userId = token.user_id.hashCode().toString(),
                            properties =
                                mapOf(
                                    "email_verified" to token.email_verified,
                                    "login_method" to "google",
                                ),
                        )
                        AnalyticsTracker.trackUserLogin("google")
                    } else {
                        // User logged out - reset analytics
                        analytics.reset()
                    }
                } catch (e: Exception) {
                    // Analytics is optional, don't fail if not available
                }
            }.mapLatest {
                when (it) {
                    null -> {
                        LoginResultUiState.NotStarted
                    }

                    else -> {
                        LoginResultUiState.Success(it)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LoginResultUiState.Loading,
            )

    fun onGoogleSignClicked() {
        authRepository.startGoogleSignInFlow()
    }

    suspend fun onLogOut() {
        // Track logout before clearing user data
        try {
            AnalyticsTracker.trackUserLogout()
        } catch (e: Exception) {
            // Analytics is optional, don't fail if not available
        }

        projectSaver.deleteCacheProjects()
        authRepository.logOut()
    }
}

sealed interface LoginResultUiState {
    data object NotStarted : LoginResultUiState

    data object Loading : LoginResultUiState

    data class Success(
        val firebaseIdToken: FirebaseIdToken,
    ) : LoginResultUiState
}
