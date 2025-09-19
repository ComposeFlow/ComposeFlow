package io.composeflow.model.settings

import io.composeflow.datastore.PlatformDataStore
import io.composeflow.di.ServiceLocator
import io.composeflow.di.ServiceLocator.KEY_DEFAULT_DISPATCHER
import io.composeflow.json.jsonSerializer
import io.composeflow.platform.getEnvVar
import io.composeflow.platform.getOrCreateDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsRepository(
    private val dataStore: PlatformDataStore = ServiceLocator.getOrPut { getOrCreateDataStore() },
    private val javaHomePathFromEnvVar: PathSetting.FromEnvVar? =
        getEnvVar("JAVA_HOME")?.let {
            PathSetting.FromEnvVar("JAVA_HOME", it)
        },
    dispatchers: CoroutineDispatcher = ServiceLocator.getOrPutWithKey(KEY_DEFAULT_DISPATCHER) { Dispatchers.Default },
) {
    private val scope: CoroutineScope = CoroutineScope(dispatchers)

    // Preference keys as strings
    private val darkThemeKey = "dark_theme"
    private val appDarkThemeKey = "app_dark_theme"
    private val javaHomeKey = "java_home"
    private val showBordersKey = "show_borders"
    private val versionAskedToUpdateKey = "version_asked_to_update"
    private val onboardingCompletedKey = "onboarding_completed"

    // Internal state flows for reactive behavior
    private val settingsFlow = MutableStateFlow<ComposeBuilderSettings?>(null)
    private val hasShownOnboardingFlow = MutableStateFlow<Boolean?>(null)

    val settings: Flow<ComposeBuilderSettings> = settingsFlow.filterNotNull()

    private suspend fun loadSettings(): ComposeBuilderSettings {
        val composeBuilderTheme = dataStore.getInt(darkThemeKey) ?: DarkThemeSetting.Dark.ordinal
        val appTheme = dataStore.getInt(appDarkThemeKey) ?: DarkThemeSetting.System.ordinal
        val showBorders = dataStore.getBoolean(showBordersKey) ?: false
        val javaHomeJson = dataStore.getString(javaHomeKey)
        val versionAskedToUpdate = dataStore.getString(versionAskedToUpdateKey)

        val javaHome =
            javaHomeJson?.let {
                jsonSerializer.decodeFromString<PathSetting>(it)
            } ?: javaHomePathFromEnvVar

        return ComposeBuilderSettings(
            composeBuilderDarkThemeSetting = DarkThemeSetting.fromOrdinal(composeBuilderTheme),
            appDarkThemeSetting = DarkThemeSetting.fromOrdinal(appTheme),
            showBordersInCanvas = showBorders,
            javaHome = javaHome,
            versionAskedToUpdate = versionAskedToUpdate,
        ).also { settingsFlow.value = it }
    }

    init {
        // Load settings on initialization
        scope.launch {
            loadSettings()
            loadOnboardingStatus()
        }
    }

    fun saveComposeBuilderDarkTheme(darkThemeSetting: DarkThemeSetting) {
        scope.launch {
            dataStore.putInt(darkThemeKey, darkThemeSetting.ordinal)
            // Refresh cached settings
            loadSettings()
        }
    }

    val appDarkThemeSetting: Flow<Boolean> =
        settings.map {
            when (it.appDarkThemeSetting) {
                DarkThemeSetting.System -> false
                DarkThemeSetting.Light -> false
                DarkThemeSetting.Dark -> true
            }
        }

    fun saveAppDarkTheme(darkThemeSetting: DarkThemeSetting) {
        scope.launch {
            dataStore.putInt(appDarkThemeKey, darkThemeSetting.ordinal)
            // Refresh cached settings
            loadSettings()
        }
    }

    fun saveShowBorders(showBorders: Boolean) {
        scope.launch {
            dataStore.putBoolean(showBordersKey, showBorders)
            // Refresh cached settings
            loadSettings()
        }
    }

    fun saveJavaHomePath(javaHomePath: PathSetting) {
        scope.launch {
            dataStore.putString(javaHomeKey, jsonSerializer.encodeToString(javaHomePath))
            // Refresh cached settings
            loadSettings()
        }
    }

    fun saveVersionAskedToUpdate(versionAskedToUpdate: String) {
        scope.launch {
            dataStore.putString(versionAskedToUpdateKey, versionAskedToUpdate)
            // Refresh cached settings
            loadSettings()
        }
    }

    val hasShownOnboarding: Flow<Boolean> = hasShownOnboardingFlow.filterNotNull()

    private suspend fun loadOnboardingStatus(): Boolean {
        val status = dataStore.getBoolean(onboardingCompletedKey) ?: false
        hasShownOnboardingFlow.value = status
        return status
    }

    fun saveOnboardingCompleted(completed: Boolean) {
        scope.launch {
            dataStore.putBoolean(onboardingCompletedKey, completed)
            // Refresh cached onboarding status
            loadOnboardingStatus()
        }
    }

    companion object {
        fun getInstance(): SettingsRepository = ServiceLocator.getOrPut { SettingsRepository() }
    }
}
