package io.composeflow.ui.inspector.codeviewer

import io.composeflow.formatter.PlatformCodeTheme
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import kotlinx.coroutines.flow.StateFlow
import moe.tlaster.precompose.viewmodel.ViewModel

expect class CodeInspectorViewModel(
    project: Project,
    codeTheme: PlatformCodeTheme,
) : ViewModel {
    val uiState: StateFlow<CodeInspectorUiState>
    fun setComposeNode(composeNode: ComposeNode)
}
