package io.composeflow.ui.inspector.codeviewer

import androidx.compose.ui.text.AnnotatedString
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
import io.composeflow.formatter.PlatformCodeTheme

internal actual fun createSyntaxHighlighter(codeTheme: PlatformCodeTheme): SyntaxHighlighter = JvmSyntaxHighlighter(codeTheme)

private class JvmSyntaxHighlighter(
    private val codeTheme: PlatformCodeTheme,
) : SyntaxHighlighter {
    private val parser = PrettifyParser()
    private val codeMap: MutableMap<String, AnnotatedString> = mutableMapOf()

    override fun highlight(code: String): AnnotatedString =
        codeMap.getOrPut(code) {
            parseCodeAsAnnotatedString(
                parser = parser,
                theme = codeTheme.theme,
                lang = CodeLang.Kotlin,
                code = code,
            )
        }
}
