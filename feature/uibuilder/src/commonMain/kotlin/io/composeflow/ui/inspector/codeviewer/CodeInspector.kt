package io.composeflow.ui.inspector.codeviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.composeflow.formatter.LocalCodeTheme
import io.composeflow.model.project.Project
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.Res
import io.composeflow.copied_code
import io.composeflow.copy_code
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CodeInspector(
    project: Project,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val composeNode = project.screenHolder.findFocusedNodeOrNull() ?: return
    val codeTheme = LocalCodeTheme.current
    val viewModel =
        viewModel(modelClass = CodeInspectorViewModel::class) {
            CodeInspectorViewModel(
                project = project,
                codeTheme = codeTheme,
            )
        }
    viewModel.setComposeNode(composeNode)
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        CodeInspectorUiState.Loading -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.fillMaxSize(),
            ) {
                CircularProgressIndicator()
            }
        }

        is CodeInspectorUiState.Success -> {
            CodeViewer(
                parsedCode = state.parsedCode,
                onShowSnackbar = onShowSnackbar,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CodeViewer(
    parsedCode: AnnotatedString,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val copiedCodeStr = stringResource(Res.string.copied_code)
                ComposeFlowIconButton(
                    onClick = {
                        clipboardManager.setText(parsedCode)
                        coroutineScope.launch {
                            onShowSnackbar(copiedCodeStr, null)
                        }
                    },
                    modifier = Modifier.hoverOverlay(),
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(Res.string.copy_code),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        item {
            SelectionContainer {
                Text(
                    text = parsedCode,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
