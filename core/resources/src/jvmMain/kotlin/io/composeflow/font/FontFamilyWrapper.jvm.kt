package io.composeflow.font

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName

actual fun FontFamilyWrapper.generateFontFamilyFunSpec(): Any {
    val funSpecBuilder =
        FunSpec
            .builder(fontFamilyName())
            .addAnnotation(Composable::class)
            .returns(FontFamily::class)
            .addCode(
                CodeBlock.of(
                    "return %M(",
                    MemberName("androidx.compose.ui.text.font", "FontFamily"),
                ),
            )

    fontFileWrappers.forEach { fontFile ->
        val weightWrapper = FontWeightWrapper.fromFontWeight(fontFile.fontWeight)
        val styleAsString =
            when (fontFile.fontStyle) {
                FontStyle.Normal -> "Normal"
                FontStyle.Italic -> "Italic"
                else -> "Normal"
            }
        funSpecBuilder.addCode(
            CodeBlock.of(
                "%M(%M.font.%M, weight = %M.${weightWrapper.name}, style = %M.$styleAsString),",
                MemberName("org.jetbrains.compose.resources", "Font"),
                MemberName("io.composeflow", "Res"),
                MemberName("io.composeflow", fontFile.fontResourceName),
                MemberName("androidx.compose.ui.text.font", "FontWeight"),
                MemberName("androidx.compose.ui.text.font", "FontStyle"),
            ),
        )
    }
    funSpecBuilder.addCode(")")
    return funSpecBuilder.build()
}
