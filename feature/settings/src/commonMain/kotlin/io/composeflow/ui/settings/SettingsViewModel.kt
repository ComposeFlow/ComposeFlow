package io.composeflow.ui.settings

import androidx.compose.runtime.mutableStateOf
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.composeflow.Res
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.creating_app
import io.composeflow.firebase.FirebaseApiCaller
import io.composeflow.firebase.WebAppWrapper
import io.composeflow.firebase.management.AndroidAppWrapper
import io.composeflow.firebase.management.IosAppWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.project.firebase.prepareFirebaseApiCall
import io.composeflow.model.settings.ComposeBuilderSettings
import io.composeflow.model.settings.DarkThemeSetting
import io.composeflow.model.settings.DarkThemeSettingSetterUiState
import io.composeflow.model.settings.SettingsRepository
import io.composeflow.repository.ProjectRepository
import io.composeflow.ui.common.buildUiState
import io.composeflow.ui.settings.firebase.FirebaseAndroidAppApiResultState
import io.composeflow.ui.settings.firebase.FirebaseApiResultState
import io.composeflow.ui.settings.firebase.FirebaseIosAppApiResultState
import io.composeflow.ui.settings.firebase.FirebaseWebAppApiResultState
import io.composeflow.ui.settings.preference.SettingsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.seconds

class SettingsViewModel(
    private val project: Project,
    private val firebaseIdTokenArg: FirebaseIdToken,
    private val firebaseApiCaller: FirebaseApiCaller = FirebaseApiCaller(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdTokenArg),
) : ViewModel() {
    private val _firebaseApiResultState: MutableStateFlow<FirebaseApiResultState> =
        MutableStateFlow(FirebaseApiResultState.Initial)
    val firebaseApiResultState: StateFlow<FirebaseApiResultState> = _firebaseApiResultState

    private val _firebaseAndroidAppApiResultState: MutableStateFlow<FirebaseAndroidAppApiResultState> =
        MutableStateFlow(FirebaseAndroidAppApiResultState())
    val firebaseAndroidAppApiResultState: StateFlow<FirebaseAndroidAppApiResultState> =
        _firebaseAndroidAppApiResultState

    private val _firebaseIosAppApiResultState: MutableStateFlow<FirebaseIosAppApiResultState> =
        MutableStateFlow(FirebaseIosAppApiResultState())
    val firebaseIosAppApiResultState: StateFlow<FirebaseIosAppApiResultState> =
        _firebaseIosAppApiResultState

    private val _firebaseWebAppApiResultState: MutableStateFlow<FirebaseWebAppApiResultState> =
        MutableStateFlow(FirebaseWebAppApiResultState())
    val firebaseWebAppApiResultState: StateFlow<FirebaseWebAppApiResultState> =
        _firebaseWebAppApiResultState

    @OptIn(ExperimentalCoroutinesApi::class)
    private val firebaseIdToken =
        authRepository.firebaseIdToken
            .mapLatest {
                it
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = firebaseIdTokenArg,
            )

    private var authenticationStatusCheckJob: Job? = null

    init {
        viewModelScope.launch {

            project.firebaseAppInfoHolder.firebaseAppInfo.run {
                androidApp?.let {
                    _firebaseAndroidAppApiResultState.value =
                        FirebaseAndroidAppApiResultState(androidApp = it)
                }
                iOSApp?.let {
                    _firebaseIosAppApiResultState.value =
                        FirebaseIosAppApiResultState(iOSApp = it)
                }
                webApp?.let {
                    _firebaseWebAppApiResultState.value =
                        FirebaseWebAppApiResultState(webApp = it)
                }
            }
        }

        val firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo
        firebaseAppInfo.firebaseProjectId?.let {
            updateApiResult(firebaseAppInfo, it)
        }

        authenticationStatusCheckJob?.cancel()
        authenticationStatusCheckJob =
            viewModelScope.launch {
                while (isActive) {
                    try {
                        project.firebaseAppInfoHolder.firebaseAppInfo.firebaseProjectId?.let { firebaseProjectId ->
                            firebaseIdToken.prepareFirebaseApiCall(
                                firebaseProjectId = firebaseProjectId,
                                authRepository = authRepository,
                                ignoreInsufficientScope = true,
                            ) { identifier ->
                                firebaseApiCaller
                                    .checkIdentityPlatformEnabled(
                                        identifier,
                                    ).onSuccess {
                                        if (it == true && !firebaseAppInfo.authenticationEnabled.value) {
                                            project.firebaseAppInfoHolder.firebaseAppInfo =
                                                project.firebaseAppInfoHolder.firebaseAppInfo.copy(
                                                    authenticationEnabled = mutableStateOf(true),
                                                )
                                            saveProject()
                                        }
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.w("Failed in checking the authentication status", e)
                        authenticationStatusCheckJob?.cancel()
                    }
                    delay(30.seconds)
                }
            }
    }

    val settings =
        settingsRepository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ComposeBuilderSettings(),
        )

    private val composeFlowDarkThemeSetting =
        settings
            .map { it.composeBuilderDarkThemeSetting }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = settings.value.composeBuilderDarkThemeSetting,
            )

    private val darkThemeSettingSetterUiState =
        viewModelScope.buildUiState(
            composeFlowDarkThemeSetting,
        ) {
            DarkThemeSettingSetterUiState(
                darkThemeSetting = it,
                onThemeChanged = ::onThemeChanged,
            )
        }

    val darkThemeSettingsUiState =
        viewModelScope.buildUiState(
            darkThemeSettingSetterUiState,
        ) { darkThemeSettingSetterUiState ->
            SettingsUiState(
                darkThemeSettingSetterUiState = darkThemeSettingSetterUiState,
            )
        }

    private fun onThemeChanged(darkThemeSetting: DarkThemeSetting) {
        settingsRepository.saveComposeBuilderDarkTheme(darkThemeSetting)
    }

    fun onLoginScreenChanged(loginScreen: Screen) {
        project.screenHolder.loginScreenId.value = loginScreen.id
        saveProject()
    }

    fun onConnectFirebaseProjectId(firebaseProjectId: String) {
        viewModelScope.launch {
            _firebaseApiResultState.value = FirebaseApiResultState.Loading

            firebaseIdToken.prepareFirebaseApiCall(
                firebaseProjectId = firebaseProjectId,
                authRepository = authRepository,
            ) { identifier ->

                val creatingAppString = getString(Res.string.creating_app)
                val androidApp =
                    project.firebaseAppInfoHolder.firebaseAppInfo.androidApp
                firebaseApiCaller
                    .listAndroidApps(identifier)
                    .mapBoth(
                        success = { existingAndroidApps ->
                            val firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo
                            if (androidApp != null) {
                                existingAndroidApps
                                    ?.apps
                                    ?.firstOrNull {
                                        it.appId == androidApp.metadata.appId ||
                                            it.packageName == androidApp.metadata.packageName
                                    }?.let { metadata ->
                                        firebaseApiCaller
                                            .getAndroidAppConfig(
                                                identifier = identifier,
                                                appId = metadata.appId,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        AndroidAppWrapper(
                                                            metadata = metadata,
                                                            config = config.decodeFileContents(),
                                                        )
                                                    _firebaseAndroidAppApiResultState.value =
                                                        FirebaseAndroidAppApiResultState(androidApp = appWrapper)
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(androidApp = appWrapper)
                                                    saveProject()
                                                }
                                            }
                                    }
                            } else {
                                existingAndroidApps
                                    ?.apps
                                    ?.firstOrNull { it.packageName == project.bundleId }
                                    ?.let { metadata ->
                                        firebaseApiCaller
                                            .getAndroidAppConfig(
                                                identifier = identifier,
                                                appId = metadata.appId,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        AndroidAppWrapper(
                                                            metadata = metadata,
                                                            config = config.decodeFileContents(),
                                                        )
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(androidApp = appWrapper)
                                                    _firebaseAndroidAppApiResultState.value =
                                                        FirebaseAndroidAppApiResultState(androidApp = appWrapper)
                                                }
                                            }
                                    } ?: run {
                                    _firebaseAndroidAppApiResultState.value =
                                        FirebaseAndroidAppApiResultState(
                                            isLoading = true,
                                            loadingMessage = creatingAppString,
                                        )
                                    firebaseApiCaller
                                        .createAndroidApp(
                                            identifier,
                                            packageName = project.bundleId,
                                            displayName =
                                                FirebaseAppInfo.defaultAppDisplayName(
                                                    project.name,
                                                ),
                                        ).onSuccess {
                                            // Intentionally put some delay because right after creating the
                                            // web app, it doesn't show app in the list result
                                            delay(4000)
                                            // The response doesn't contain the created app metadata, so we need to fetch it again
                                            firebaseApiCaller
                                                .listAndroidApps(
                                                    identifier = identifier,
                                                ).onSuccess { androidApps ->
                                                    val newMetadata =
                                                        androidApps?.apps?.firstOrNull {
                                                            it.packageName == project.bundleId
                                                        }
                                                    newMetadata?.let {
                                                        firebaseApiCaller
                                                            .getAndroidAppConfig(
                                                                identifier = identifier,
                                                                appId = newMetadata.appId,
                                                            ).onSuccess { config ->
                                                                config?.let {
                                                                    val appWrapper =
                                                                        AndroidAppWrapper(
                                                                            metadata = newMetadata,
                                                                            config = config.decodeFileContents(),
                                                                        )
                                                                    _firebaseAndroidAppApiResultState.value =
                                                                        FirebaseAndroidAppApiResultState(
                                                                            androidApp = appWrapper,
                                                                        )
                                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                                        firebaseAppInfo.copy(
                                                                            androidApp = appWrapper,
                                                                        )
                                                                    saveProject()
                                                                }
                                                            }.onFailure {
                                                                _firebaseAndroidAppApiResultState.value =
                                                                    FirebaseAndroidAppApiResultState(
                                                                        failureMessage = it.message,
                                                                    )
                                                            }
                                                    } ?: run {
                                                        _firebaseAndroidAppApiResultState.value =
                                                            FirebaseAndroidAppApiResultState(
                                                                failureMessage =
                                                                    "Failed to create Android app",
                                                            )
                                                    }
                                                }
                                        }.onFailure {
                                            _firebaseAndroidAppApiResultState.value =
                                                FirebaseAndroidAppApiResultState(
                                                    failureMessage =
                                                        it.message ?: "Unknown error",
                                                )
                                        }
                                }
                            }
                        },
                        failure = {
                            _firebaseAndroidAppApiResultState.value =
                                FirebaseAndroidAppApiResultState(
                                    failureMessage = it.message ?: "Unknown error",
                                )
                        },
                    )

                val iOSApp = project.firebaseAppInfoHolder.firebaseAppInfo.iOSApp
                firebaseApiCaller
                    .listIosApps(identifier)
                    .mapBoth(
                        success = { existingIosApps ->
                            val firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo
                            if (iOSApp != null) {
                                existingIosApps
                                    ?.apps
                                    ?.firstOrNull {
                                        it.appId == iOSApp.metadata.appId ||
                                            it.bundleId == iOSApp.metadata.bundleId
                                    }?.let { metadata ->
                                        firebaseApiCaller
                                            .getIosAppConfig(
                                                identifier = identifier,
                                                appId = metadata.appId,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        IosAppWrapper(
                                                            metadata = metadata,
                                                            config = config.decodeFileContents(),
                                                        )
                                                    _firebaseIosAppApiResultState.value =
                                                        FirebaseIosAppApiResultState(iOSApp = appWrapper)
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(iOSApp = appWrapper)
                                                    saveProject()
                                                }
                                            }
                                    }
                            } else {
                                existingIosApps
                                    ?.apps
                                    ?.firstOrNull {
                                        it.bundleId == project.bundleId
                                    }?.let { metadata ->
                                        firebaseApiCaller
                                            .getIosAppConfig(
                                                identifier = identifier,
                                                appId = metadata.appId,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        IosAppWrapper(
                                                            metadata = metadata,
                                                            config = config.decodeFileContents(),
                                                        )
                                                    _firebaseIosAppApiResultState.value =
                                                        FirebaseIosAppApiResultState(iOSApp = appWrapper)
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(iOSApp = appWrapper)
                                                    saveProject()
                                                }
                                            }
                                    } ?: run {
                                    _firebaseIosAppApiResultState.value =
                                        FirebaseIosAppApiResultState(
                                            isLoading = true,
                                            loadingMessage = creatingAppString,
                                        )
                                    firebaseApiCaller
                                        .createIosApp(
                                            identifier,
                                            bundleId = project.bundleId,
                                            displayName =
                                                FirebaseAppInfo.defaultAppDisplayName(
                                                    project.name,
                                                ),
                                        ).onSuccess {
                                            // Intentionally put some delay because right after creating the
                                            // web app, it doesn't show app in the list result
                                            delay(4000)
                                            // The response doesn't contain the created app metadata, so we need to fetch it again
                                            firebaseApiCaller
                                                .listIosApps(
                                                    identifier = identifier,
                                                ).onSuccess { iosApps ->
                                                    val newMetadata =
                                                        iosApps?.apps?.firstOrNull {
                                                            it.bundleId == project.bundleId
                                                        }
                                                    newMetadata?.let {
                                                        firebaseApiCaller
                                                            .getIosAppConfig(
                                                                identifier = identifier,
                                                                appId = newMetadata.appId,
                                                            ).onSuccess { config ->
                                                                config?.let {
                                                                    val appWrapper =
                                                                        IosAppWrapper(
                                                                            metadata = newMetadata,
                                                                            config = config.decodeFileContents(),
                                                                        )
                                                                    _firebaseIosAppApiResultState.value =
                                                                        FirebaseIosAppApiResultState(
                                                                            iOSApp = appWrapper,
                                                                        )
                                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                                        firebaseAppInfo.copy(iOSApp = appWrapper)
                                                                    saveProject()
                                                                }
                                                            }.onFailure {
                                                                _firebaseIosAppApiResultState.value =
                                                                    FirebaseIosAppApiResultState(
                                                                        failureMessage = it.message,
                                                                    )
                                                            }
                                                    } ?: run {
                                                        _firebaseIosAppApiResultState.value =
                                                            FirebaseIosAppApiResultState(
                                                                failureMessage =
                                                                    "Failed to create iOS app",
                                                            )
                                                    }
                                                }
                                        }.onFailure {
                                            _firebaseAndroidAppApiResultState.value =
                                                FirebaseAndroidAppApiResultState(
                                                    failureMessage =
                                                        it.message ?: "Unknown error",
                                                )
                                        }
                                }
                            }
                        },
                        failure = {
                            _firebaseIosAppApiResultState.value =
                                FirebaseIosAppApiResultState(
                                    failureMessage = it.message ?: "Unknown error",
                                )
                        },
                    )

                val webApp = project.firebaseAppInfoHolder.firebaseAppInfo.webApp
                firebaseApiCaller
                    .listWebApps(
                        identifier = identifier,
                    ).mapBoth(
                        success = { existingWebApps ->
                            val firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo
                            if (webApp != null) {
                                existingWebApps
                                    ?.apps
                                    ?.firstOrNull {
                                        it.appId == webApp.metadata.appId ||
                                            it.displayName == webApp.metadata.displayName
                                    }?.let { metadata ->
                                        firebaseApiCaller
                                            .getWebAppConfig(
                                                firebaseProjectId = identifier.firebaseProjectId,
                                                appId = metadata.appId,
                                                tokenResponse = identifier.googleTokenResponse,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        WebAppWrapper(
                                                            metadata = metadata,
                                                            config = config,
                                                        )
                                                    _firebaseWebAppApiResultState.value =
                                                        FirebaseWebAppApiResultState(webApp = appWrapper)
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(webApp = appWrapper)
                                                    saveProject()
                                                }
                                            }.onFailure {
                                                _firebaseWebAppApiResultState.value =
                                                    FirebaseWebAppApiResultState(failureMessage = it.message)
                                            }
                                    }
                            } else {
                                existingWebApps
                                    ?.apps
                                    ?.firstOrNull {
                                        it.appId == project.bundleId
                                    }?.let { metadata ->
                                        firebaseApiCaller
                                            .getWebAppConfig(
                                                firebaseProjectId = identifier.firebaseProjectId,
                                                appId = metadata.appId,
                                                tokenResponse = identifier.googleTokenResponse,
                                            ).onSuccess { config ->
                                                config?.let {
                                                    val appWrapper =
                                                        WebAppWrapper(
                                                            metadata = metadata,
                                                            config = config,
                                                        )
                                                    _firebaseWebAppApiResultState.value =
                                                        FirebaseWebAppApiResultState(webApp = appWrapper)
                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                        firebaseAppInfo.copy(webApp = appWrapper)
                                                    saveProject()
                                                }
                                            }.onFailure {
                                                _firebaseWebAppApiResultState.value =
                                                    FirebaseWebAppApiResultState(failureMessage = it.message)
                                            }
                                    } ?: run {
                                    _firebaseWebAppApiResultState.value =
                                        FirebaseWebAppApiResultState(
                                            isLoading = true,
                                            loadingMessage = creatingAppString,
                                        )
                                    firebaseApiCaller
                                        .createWebApp(
                                            identifier = identifier,
                                            displayName =
                                                FirebaseAppInfo.defaultWebAppDisplayName(
                                                    project.name,
                                                ),
                                        ).onSuccess {
                                            // Intentionally put some delay because right after creating the
                                            // web app, it doesn't show app in the list result
                                            delay(4000)
                                            // The response doesn't contain the created web app metadata, so we need to fetch it again
                                            firebaseApiCaller
                                                .listWebApps(
                                                    identifier = identifier,
                                                ).onSuccess { webApps ->
                                                    val newMetadata =
                                                        webApps?.apps?.firstOrNull {
                                                            it.displayName ==
                                                                FirebaseAppInfo.defaultWebAppDisplayName(
                                                                    project.name,
                                                                )
                                                        }
                                                    newMetadata?.let {
                                                        firebaseApiCaller
                                                            .getWebAppConfig(
                                                                identifier = identifier,
                                                                appId = newMetadata.appId,
                                                            ).onSuccess { config ->
                                                                config?.let {
                                                                    val appWrapper =
                                                                        WebAppWrapper(
                                                                            metadata = newMetadata,
                                                                            config = config,
                                                                        )
                                                                    _firebaseWebAppApiResultState.value =
                                                                        FirebaseWebAppApiResultState(
                                                                            webApp = appWrapper,
                                                                        )
                                                                    project.firebaseAppInfoHolder.firebaseAppInfo =
                                                                        firebaseAppInfo.copy(webApp = appWrapper)
                                                                    saveProject()
                                                                }
                                                            }.onFailure {
                                                                _firebaseWebAppApiResultState.value =
                                                                    FirebaseWebAppApiResultState(
                                                                        failureMessage = it.message,
                                                                    )
                                                            }
                                                    } ?: run {
                                                        _firebaseWebAppApiResultState.value =
                                                            FirebaseWebAppApiResultState(
                                                                failureMessage =
                                                                    "Failed to create web app",
                                                            )
                                                    }
                                                }
                                        }.onFailure {
                                            _firebaseWebAppApiResultState.value =
                                                FirebaseWebAppApiResultState(
                                                    failureMessage =
                                                        it.message ?: "Unknown error",
                                                )
                                        }
                                }
                            }
                        },
                        failure = {
                            _firebaseWebAppApiResultState.value =
                                FirebaseWebAppApiResultState(
                                    failureMessage = it.message ?: "Unknown error",
                                )
                        },
                    )

                var firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo
                firebaseApiCaller
                    .checkIdentityPlatformEnabled(
                        identifier,
                    ).onSuccess {
                        if (it == true && !firebaseAppInfo.authenticationEnabled.value) {
                            firebaseAppInfo =
                                firebaseAppInfo.copy(authenticationEnabled = mutableStateOf(true))
                            project.firebaseAppInfoHolder.firebaseAppInfo = firebaseAppInfo
                        }
                    }
                updateApiResult(firebaseAppInfo, identifier.firebaseProjectId.trim())
            }
        }
    }

    private fun updateApiResult(
        firebaseAppInfo: FirebaseAppInfo,
        firebaseProjectId: String,
    ) {
        if (_firebaseAndroidAppApiResultState.value.isSuccess() &&
            _firebaseIosAppApiResultState.value.isSuccess() &&
            _firebaseWebAppApiResultState.value.isSuccess()
        ) {
            _firebaseApiResultState.value = FirebaseApiResultState.Success()
            project.firebaseAppInfoHolder.firebaseAppInfo =
                firebaseAppInfo.copy(firebaseProjectId = firebaseProjectId)
            saveProject()
        } else if (
            _firebaseAndroidAppApiResultState.value.isSuccess() ||
            _firebaseIosAppApiResultState.value.isSuccess() ||
            _firebaseWebAppApiResultState.value.isSuccess()
        ) {
            _firebaseApiResultState.value = FirebaseApiResultState.PartialSuccess()
            project.firebaseAppInfoHolder.firebaseAppInfo =
                firebaseAppInfo.copy(firebaseProjectId = firebaseProjectId)
            saveProject()
        } else {
            _firebaseApiResultState.value = FirebaseApiResultState.Failure()
        }
    }

    private fun saveProject() {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}
