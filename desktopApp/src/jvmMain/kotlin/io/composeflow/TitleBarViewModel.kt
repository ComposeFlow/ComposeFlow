package io.composeflow

import io.composeflow.model.settings.ComposeBuilderSettings
import io.composeflow.model.settings.SettingsRepository
import io.composeflow.ui.utils.TitleBarContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class TitleBarViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance(),
) : ViewModel() {
    /**
     * The content of the title bar.
     * This is to set the content of the title bar from the Composable which is guarded until
     * the user is authenticated and a project is selected.
     * Because some of the icons in the title bar need to be hidden until a project is selected,
     * (e.g. download the code, play the app buttons). Thus hoisting the lambada to set the content
     * of the title bar and pass it down to the Composable where the content is produced.
     */
    private var _titleBarRightContent = MutableStateFlow<TitleBarContent>({})
    val titleBarRightContent: StateFlow<TitleBarContent> = _titleBarRightContent

    private var _titleBarLeftContent = MutableStateFlow<TitleBarContent>({})
    val titleBarLeftContent: StateFlow<TitleBarContent> = _titleBarLeftContent

    private val settings =
        settingsRepository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ComposeBuilderSettings(),
        )

    val versionAskedToUpdate =
        settings
            .map {
                VersionAskedToUpdate.Ready(it.versionAskedToUpdate)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = VersionAskedToUpdate.NotReady,
            )

    fun onTitleBarRightContentSet(content: TitleBarContent) {
        _titleBarRightContent.value = content
    }

    fun onTitleBarLeftContentSet(content: TitleBarContent) {
        _titleBarLeftContent.value = content
    }

    fun onSaveVersionAskedToUpdate(versionAskedToUpdate: String) {
        settingsRepository.saveVersionAskedToUpdate(versionAskedToUpdate)
    }
}
