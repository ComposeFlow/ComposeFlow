package io.composeflow.ui.settings

import io.composeflow.model.settings.ComposeBuilderSettings
import io.composeflow.model.settings.DarkThemeSetting
import io.composeflow.model.settings.DarkThemeSettingSetterUiState
import io.composeflow.model.settings.SettingsRepository
import io.composeflow.ui.common.buildUiState
import io.composeflow.ui.settings.preference.SettingsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class AccountSettingsViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance(),
) : ViewModel() {
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

    val javaHomePath =
        settings
            .map { it.javaHome }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = settings.value.javaHome,
            )
}
