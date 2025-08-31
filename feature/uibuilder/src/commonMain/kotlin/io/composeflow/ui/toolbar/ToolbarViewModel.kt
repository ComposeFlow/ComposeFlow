package io.composeflow.ui.toolbar

import com.github.michaelbull.result.onSuccess
import io.composeflow.Res
import io.composeflow.appbuilder.AppRunner
import io.composeflow.auth.AuthRepository
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.cloud.storage.AssetSynchronizer
import io.composeflow.cloud.storage.GoogleCloudStorageWrapper
import io.composeflow.datastore.LocalAssetSaver
import io.composeflow.device.Device
import io.composeflow.device.EmulatorStatus
import io.composeflow.device.nextPortNumber
import io.composeflow.download_jdk_needed_message
import io.composeflow.downloading_jdk
import io.composeflow.failed_to_download_jdk
import io.composeflow.finished_downloading_jdk
import io.composeflow.firebase.FirebaseApiCaller
import io.composeflow.model.project.LoadedProjectUiState
import io.composeflow.model.project.Project
import io.composeflow.model.project.asLoadedProjectUiState
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.project.firebase.prepareFirebaseApiCall
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.NavigatableDestination
import io.composeflow.model.settings.ComposeBuilderSettings
import io.composeflow.model.settings.PathSetting
import io.composeflow.model.settings.SettingsRepository
import io.composeflow.platform.getEnvVar
import io.composeflow.repository.ProjectRepository
import io.composeflow.testing.isTest
import io.composeflow.ui.common.buildUiState
import io.composeflow.ui.statusbar.StatusBarUiState
import io.composeflow.wrapper.JdkChecker
import io.composeflow.wrapper.JdkDownloader
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
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.compose.resources.getString

class ToolbarViewModel(
    private val firebaseIdTokenArg: FirebaseIdToken?,
    private val projectRepository: ProjectRepository =
        if (firebaseIdTokenArg !=
            null
        ) {
            ProjectRepository(firebaseIdTokenArg)
        } else {
            ProjectRepository.createAnonymous()
        },
    private val authRepository: AuthRepository = AuthRepository(),
    private val firebaseApiCaller: FirebaseApiCaller = FirebaseApiCaller(),
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val googleCloudStorageWrapper: GoogleCloudStorageWrapper = GoogleCloudStorageWrapper(),
    private val localAssetSaver: LocalAssetSaver = LocalAssetSaver(),
    private val assertSynchronizer: AssetSynchronizer =
        AssetSynchronizer(
            googleCloudStorageWrapper,
            localAssetSaver,
        ),
) : ViewModel() {
    val editingProject =
        projectRepository.editingProject.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Project(),
        )

    private val _availableDevices: MutableStateFlow<List<Device>> =
        MutableStateFlow(listOf(Device.Web))
    val availableDevices: StateFlow<List<Device>> = _availableDevices

    private val selectedDeviceName = MutableStateFlow(availableDevices.value.first().deviceName)

    val selectedDevice =
        viewModelScope.buildUiState(
            availableDevices,
            selectedDeviceName,
        ) { devices, selected ->
            devices.firstOrNull { it.deviceName == selected }
                ?: devices.first()
        }

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

    private var runPreviewJob: Job? = null

    private val settings =
        settingsRepository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ComposeBuilderSettings(),
        )

    val javaHomePath =
        settings
            .map { it.javaHome }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = settings.value.javaHome,
            )

    private val _pendingPreviewAppParams = MutableStateFlow<PreviewAppParams?>(null)
    val pendingPreviewAppParams = _pendingPreviewAppParams

    init {
        viewModelScope.launch {
            while (isActive) {
                _availableDevices.value = AppRunner.getAvailableDevices()
                delay(4000)
            }
        }
    }

    fun onSelectedDeviceNameChanged(deviceName: String) {
        selectedDeviceName.value = deviceName
    }

    fun onCheckPreviewAvailability(previewAppParams: PreviewAppParams): PreviewAvailability {
        val isJavaAvailable =
            previewAppParams.javaHomePath?.let {
                JdkChecker.isValidJavaHome(it.path())
            } == true

        return if (isJavaAvailable) {
            PreviewAvailability.Available(previewAppParams)
        } else {
            PreviewAvailability.JdkNotInstalled(previewAppParams)
        }
    }

    fun onShowDownloadJdkConfirmationDialog(previewAppParams: PreviewAppParams) {
        _pendingPreviewAppParams.value = previewAppParams
    }

    fun onResetPendingPreviewRequest() {
        _pendingPreviewAppParams.value = null
    }

    fun onRunPreviewApp(
        previewAppParams: PreviewAppParams,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        downloadJdk: Boolean = false,
    ) {
        viewModelScope.launch {
            // Reset the StatusBar state
            onStatusBarUiStateChanged(StatusBarUiState.Normal)
            var localJavaHomePath = previewAppParams.javaHomePath
            if (downloadJdk) {
                try {
                    onStatusBarUiStateChanged(
                        StatusBarUiState.Loading(getString(Res.string.downloading_jdk)),
                    )
                    val downloadedJavaHome = JdkDownloader.downloadAndExtract()
                    onStatusBarUiStateChanged(
                        StatusBarUiState.Loading(getString(Res.string.finished_downloading_jdk)),
                    )
                    downloadedJavaHome?.let {
                        settingsRepository.saveJavaHomePath(PathSetting.FromLocal(it))
                        localJavaHomePath = PathSetting.FromLocal(it)
                    } ?: run {
                        onStatusBarUiStateChanged(
                            StatusBarUiState.Failure(getString(Res.string.failed_to_download_jdk)),
                        )
                        return@launch
                    }
                } catch (_: Exception) {
                    onStatusBarUiStateChanged(
                        StatusBarUiState.Failure(getString(Res.string.failed_to_download_jdk)),
                    )
                    return@launch
                }
            }

            val projectUiState =
                projectRepository
                    .loadProject(previewAppParams.projectFileName)
                    .asLoadedProjectUiState(previewAppParams.projectFileName)
            when (projectUiState) {
                is LoadedProjectUiState.Error -> {
                    onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to run the app"))
                }

                LoadedProjectUiState.Loading -> {}
                LoadedProjectUiState.NotFound -> {}
                is LoadedProjectUiState.Success -> {
                    localJavaHomePath?.let {
                        onStatusBarUiStateChanged(StatusBarUiState.Loading("Running the app"))
                        onRunPreviewApp(
                            project = projectUiState.project,
                            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                            device = previewAppParams.targetDevice,
                            availableDevices = previewAppParams.availableDevices,
                            localJavaHomePath = it,
                        )
                    } ?: run {
                        onStatusBarUiStateChanged(
                            StatusBarUiState.Failure(
                                getString(Res.string.download_jdk_needed_message),
                            ),
                        )
                    }
                }
            }
        }
    }

    @VisibleForTesting
    fun onRunPreviewApp(
        project: Project,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        device: Device = Device.Web,
        availableDevices: List<Device> = emptyList(),
        localJavaHomePath: PathSetting =
            PathSetting.FromEnvVar(
                envVarName = "JAVA_HOME",
                value = getEnvVar("JAVA_HOME") ?: "",
            ),
    ) {
        val localFirebaseIdToken = firebaseIdToken.value ?: return
        onStatusBarUiStateChanged(StatusBarUiState.Loading("Generating the code"))
        if (isTest()) {
            /**
             * The emulator test can be flaky,
             * so we build the app instead of running it in tests
             */
            AppRunner.buildApp(
                project.generateCode(),
                packageName = project.packageName,
                projectName = project.name,
                copyInstructions = project.generateCopyInstructions(),
                copyLocalFileInstructions =
                    project.generateCopyLocalFileInstructions(
                        userId = localFirebaseIdToken.user_id,
                    ),
                writeFileInstructions = project.generateWriteFileInstructions(),
                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
            )
        } else {
            runPreviewJob?.cancel()
            runPreviewJob =
                viewModelScope.launch {
                    project.firebaseAppInfoHolder.firebaseAppInfo =
                        obtainUpdatedFirebaseAppInfo(project.firebaseAppInfoHolder.firebaseAppInfo)
                    saveProject(project)
                    assertSynchronizer.syncFilesLocally(
                        userId = firebaseIdTokenArg?.user_id ?: "anonymous",
                        projectId = project.id,
                        assetFiles = project.getAssetFiles(),
                    )

                    val fileSpecs = project.generateCode()

                    when (device) {
                        is Device.AndroidEmulator -> {
                            val portNumber =
                                if (device.status == EmulatorStatus.Device) {
                                    device.status.portNumber
                                } else {
                                    availableDevices.nextPortNumber()
                                }
                            AppRunner.runAndroidApp(
                                device = device,
                                fileSpecs = fileSpecs,
                                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                                portNumber = portNumber,
                                packageName = project.packageName,
                                projectName = project.name,
                                copyInstructions = project.generateCopyInstructions(),
                                copyLocalFileInstructions =
                                    project.generateCopyLocalFileInstructions(
                                        userId = localFirebaseIdToken.user_id,
                                    ),
                                writeFileInstructions = project.generateWriteFileInstructions(),
                                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                                localJavaHomePath = localJavaHomePath,
                            )
                        }

                        is Device.IosSimulator -> {
                            AppRunner.runIosApp(
                                device = device,
                                fileSpecs = fileSpecs,
                                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                                packageName = project.packageName,
                                projectName = project.name,
                                copyInstructions = project.generateCopyInstructions(),
                                copyLocalFileInstructions =
                                    project.generateCopyLocalFileInstructions(
                                        userId = localFirebaseIdToken.user_id,
                                    ),
                                writeFileInstructions = project.generateWriteFileInstructions(),
                                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                            )
                        }

                        Device.Web -> {
                            AppRunner.runJsApp(
                                fileSpecs = fileSpecs,
                                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                                packageName = project.packageName,
                                projectName = project.name,
                                copyInstructions = project.generateCopyInstructions(),
                                copyLocalFileInstructions =
                                    project.generateCopyLocalFileInstructions(
                                        userId = localFirebaseIdToken.user_id,
                                    ),
                                writeFileInstructions = project.generateWriteFileInstructions(),
                                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
                                localJavaHomePath = localJavaHomePath,
                            )
                        }
                    }
                }
        }
    }

    fun onDownloadCode(
        projectFileName: String,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        viewModelScope.launch {
            // Reset the StatusBar state
            onStatusBarUiStateChanged(StatusBarUiState.Normal)
            val projectUiState =
                projectRepository
                    .loadProject(projectFileName)
                    .asLoadedProjectUiState(projectFileName)
            when (projectUiState) {
                is LoadedProjectUiState.Error -> {}
                LoadedProjectUiState.Loading -> {}
                LoadedProjectUiState.NotFound -> {}
                is LoadedProjectUiState.Success -> {
                    onDownloadCode(
                        project = projectUiState.project,
                        onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                    )
                }
            }
        }
    }

    @VisibleForTesting
    fun onDownloadCode(
        project: Project,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        val localFirebaseIdToken = firebaseIdToken.value ?: return
        onStatusBarUiStateChanged(StatusBarUiState.Loading("Generating the code"))
        viewModelScope.launch {
            project.firebaseAppInfoHolder.firebaseAppInfo =
                obtainUpdatedFirebaseAppInfo(project.firebaseAppInfoHolder.firebaseAppInfo)
            saveProject(project)
            assertSynchronizer.syncFilesLocally(
                userId = firebaseIdTokenArg?.user_id ?: "anonymous",
                projectId = project.id,
                assetFiles = project.getAssetFiles(),
            )
            val fileSpecs = project.generateCode()
            AppRunner.downloadCode(
                project = project,
                fileSpecs = fileSpecs,
                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                packageName = project.packageName,
                projectName = project.name,
                copyInstructions = project.generateCopyInstructions(),
                copyLocalFileInstructions =
                    project.generateCopyLocalFileInstructions(
                        userId = localFirebaseIdToken.user_id,
                    ),
                writeFileInstructions = project.generateWriteFileInstructions(),
                firebaseAppInfo = project.firebaseAppInfoHolder.firebaseAppInfo,
            )
        }
    }

    fun onSetPendingFocuses(
        navigatableDestination: NavigatableDestination,
        destinationContext: DestinationContext,
    ) {
        val project = editingProject.value
        project.screenHolder.pendingDestination = navigatableDestination
        project.screenHolder.pendingDestinationContext = destinationContext

        saveProject(project)
    }

    private suspend fun obtainUpdatedFirebaseAppInfo(firebaseAppInfo: FirebaseAppInfo): FirebaseAppInfo {
        var result = firebaseAppInfo
        firebaseAppInfo.firebaseProjectId?.let { firebaseProjectId ->
            firebaseIdToken.prepareFirebaseApiCall(
                firebaseProjectId = firebaseProjectId,
                authRepository = authRepository,
            ) { newIdentifier ->
                firebaseAppInfo.androidApp?.let { androidApp ->
                    firebaseApiCaller
                        .getAndroidAppConfig(
                            identifier = newIdentifier,
                            appId = androidApp.metadata.appId,
                        ).onSuccess { newConfig ->
                            newConfig?.let {
                                result =
                                    result.copy(androidApp = androidApp.copy(config = newConfig.decodeFileContents()))
                            }
                        }
                }
                firebaseAppInfo.iOSApp?.let { iOSApp ->
                    firebaseApiCaller
                        .getIosAppConfig(
                            identifier = newIdentifier,
                            appId = iOSApp.metadata.appId,
                        ).onSuccess { newConfig ->
                            newConfig?.let {
                                result =
                                    result.copy(iOSApp = iOSApp.copy(config = newConfig.decodeFileContents()))
                            }
                        }
                }
                firebaseAppInfo.webApp?.let { webApp ->
                    firebaseApiCaller
                        .getWebAppConfig(
                            identifier = newIdentifier,
                            appId = webApp.metadata.appId,
                        ).onSuccess { newConfig ->
                            newConfig?.let {
                                result = result.copy(webApp = webApp.copy(config = it))
                            }
                        }
                }
            }
        }
        return result
    }

    private fun saveProject(project: Project) {
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }

    override fun onCleared() {
        super.onCleared()
        runPreviewJob?.cancel()
    }
}
