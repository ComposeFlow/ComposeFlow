package io.composeflow.ui.inspector.codeviewer

import androidx.compose.ui.text.AnnotatedString
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

class CodeInspectorViewModel(
    private val project: Project,
    private val codeTheme: PlatformCodeTheme,
) : ViewModel() {
    private val composeNodeFlow: MutableStateFlow<ComposeNode?> = MutableStateFlow(null)
    private val syntaxHighlighter = createSyntaxHighlighter(codeTheme)

    val uiState: StateFlow<CodeInspectorUiState> =
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
                    CodeInspectorUiState.Success(
                        syntaxHighlighter.highlight(code),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CodeInspectorUiState.Loading,
            )

    fun setComposeNode(composeNode: ComposeNode) {
        composeNodeFlow.value = composeNode
    }
}

// Platform-specific syntax highlighting
internal interface SyntaxHighlighter {
    fun highlight(code: String): AnnotatedString
}

internal expect fun createSyntaxHighlighter(codeTheme: PlatformCodeTheme): SyntaxHighlighter
