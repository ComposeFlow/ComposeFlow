package io.composeflow.ui.inspector.codeviewer

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import io.composeflow.formatter.FormatterWrapper
import io.composeflow.formatter.PlatformCodeTheme
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

actual class CodeInspectorViewModel(
    project: Project,
    codeTheme: PlatformCodeTheme,
) : ViewModel() {
    private val composeNodeFlow: MutableStateFlow<ComposeNode?> = MutableStateFlow(null)
    actual val uiState: StateFlow<CodeInspectorUiState> =
        composeNodeFlow
            .map { node ->
                if (node == null) {
                    CodeInspectorUiState.Loading
                } else {
                    val codeBlock =
                        node.generateCode(
                            project = project,
                            context = GenerationContext(),
                            dryRun = true,
                        )
                    val code =
                        FormatterWrapper.formatCodeBlock(
                            codeBlock = codeBlock,
                            withImports = false,
                            isScript = true,
                        )
                    // For WASM, return plain text without syntax highlighting
                    CodeInspectorUiState.Success(
                        buildAnnotatedString { append(code) },
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CodeInspectorUiState.Loading,
            )

    actual fun setComposeNode(composeNode: ComposeNode) {
        composeNodeFlow.value = composeNode
    }
}