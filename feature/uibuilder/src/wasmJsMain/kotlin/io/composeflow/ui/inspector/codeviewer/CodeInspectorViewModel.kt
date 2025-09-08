package io.composeflow.ui.inspector.codeviewer

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import io.composeflow.formatter.PlatformCodeTheme

internal actual fun createSyntaxHighlighter(codeTheme: PlatformCodeTheme): SyntaxHighlighter = WasmSyntaxHighlighter()

private class WasmSyntaxHighlighter : SyntaxHighlighter {
    override fun highlight(code: String): AnnotatedString {
        // For WASM, return plain text without syntax highlighting
        return buildAnnotatedString { append(code) }
    }
}
