package io.composeflow.ui.inspector.codeviewer

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeTheme
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
import io.composeflow.formatter.Formatter
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
    project: Project,
    codeTheme: CodeTheme,
) : ViewModel() {
    private val composeNodeFlow: MutableStateFlow<ComposeNode?> = MutableStateFlow(null)
    private val codeMap: MutableMap<String, AnnotatedString> = mutableMapOf()
    val uiState: StateFlow<CodeInspectorUiState> =
        composeNodeFlow
            .map { node ->
                if (node == null) {
                    CodeInspectorUiState.Loading
                } else {
                    val parser = PrettifyParser()
                    val code =
                        Formatter.format(
                            node.generateCode(
                                project = project,
                                context = GenerationContext(),
                                dryRun = true,
                            ),
                        )
                    codeMap.putIfAbsent(
                        code,
                        parseCodeAsAnnotatedString(
                            parser = parser,
                            theme = codeTheme,
                            lang = CodeLang.Kotlin,
                            code = code,
                        ),
                    )
                    CodeInspectorUiState.Success(
                        codeMap[code] ?: buildAnnotatedString { },
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
