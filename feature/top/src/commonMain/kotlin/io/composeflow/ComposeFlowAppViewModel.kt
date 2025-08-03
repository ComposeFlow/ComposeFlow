package io.composeflow

import io.composeflow.analytics.Analytics
import io.composeflow.analytics.AnalyticsTracker
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.datastore.LocalFirstProjectSaver
import io.composeflow.datastore.ProjectSaver
import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class ComposeFlowAppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val projectSaver: ProjectSaver = LocalFirstProjectSaver(),
) : ViewModel() {
    private val _isAnonymous = MutableStateFlow(false)
    @OptIn(ExperimentalCoroutinesApi::class)
    val loginResultUiState: StateFlow<LoginResultUiState> =
        combine(
            authRepository.firebaseIdToken,
            _isAnonymous
        ) { token, isAnonymous ->
            when {
                isAnonymous -> LoginResultUiState.Anonymous
                token != null -> LoginResultUiState.Success(token)
                else -> LoginResultUiState.NotStarted
            }
        }.onEach { state ->
            // Handle analytics user identification on authentication state changes
            try {
                val analytics = ServiceLocator.get<Analytics>()
                when (state) {
                    is LoginResultUiState.Success -> {
                        // User logged in - identify user for analytics
                        // Only send non-PII data to comply with privacy practices
                        analytics.identify(
                            userId = state.firebaseIdToken.user_id.hashCode().toString(),
                            properties =
                                mapOf(
                                    "email_verified" to state.firebaseIdToken.email_verified,
                                    "login_method" to "google",
                                ),
                        )
                        AnalyticsTracker.trackUserLogin("google")
                    }
                    is LoginResultUiState.Anonymous -> {
                        // Anonymous user - track without identification
                        AnalyticsTracker.trackUserLogin("anonymous")
                    }
                    else -> {
                        // User logged out - reset analytics
                        analytics.reset()
                    }
                }
            } catch (e: Exception) {
                // Analytics is optional, don't fail if not available
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginResultUiState.Loading,
        )

    fun onGoogleSignClicked() {
        authRepository.startGoogleSignInFlow()
    }
    
    fun onUseWithoutSignIn() {
        _isAnonymous.value = true
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
        _isAnonymous.value = false
    }
}

sealed interface LoginResultUiState {
    data object NotStarted : LoginResultUiState

    data object Loading : LoginResultUiState

    data class Success(
        val firebaseIdToken: FirebaseIdToken,
    ) : LoginResultUiState
    
    data object Anonymous : LoginResultUiState
}
