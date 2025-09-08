package io.composeflow.formatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.wakaztahir.codeeditor.theme.CodeTheme
import com.wakaztahir.codeeditor.theme.SyntaxColors

actual class PlatformCodeTheme(
    val theme: CodeTheme,
)

class LightCodeTheme :
    CodeTheme(
        colors =
            SyntaxColors(
                type = light_type,
                keyword = light_keyword,
                literal = light_literal,
                comment = light_comment,
                string = light_string,
                punctuation = light_punctuation,
                plain = light_plain,
                tag = light_tag,
                declaration = light_declaration,
                source = light_source,
                attrName = light_attrName,
                attrValue = light_attrValue,
                nocode = light_noCode,
            ),
    )

class DarkCodeTheme :
    CodeTheme(
        colors =
            SyntaxColors(
                type = dark_type,
                keyword = dark_keyword,
                literal = dark_literal,
                comment = dark_comment,
                string = dark_string,
                punctuation = dark_punctuation,
                plain = dark_plain,
                tag = dark_tag,
                declaration = dark_declaration,
                source = dark_source,
                attrName = dark_attrName,
                attrValue = dark_attrValue,
                nocode = dark_noCode,
            ),
    )

@Composable
actual fun ProvideCodeTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val codeTheme =
        if (useDarkTheme) {
            DarkCodeTheme()
        } else {
            LightCodeTheme()
        }
    CompositionLocalProvider(LocalCodeTheme provides PlatformCodeTheme(codeTheme)) {
        content()
    }
}
