package io.composeflow.model.project.theme

import io.composeflow.font.FontFamilyWrapper
import io.composeflow.font.generateFontFamilyFunSpec
import io.composeflow.kotlinpoet.wrapper.FileSpecBuilderWrapper
import io.composeflow.kotlinpoet.wrapper.toWrapper
import com.squareup.kotlinpoet.FunSpec

internal actual fun addFontFamilyFunSpec(fileBuilder: FileSpecBuilderWrapper, fontFamily: FontFamilyWrapper) {
    val fontFunSpec = fontFamily.generateFontFamilyFunSpec()
    if (fontFunSpec is FunSpec) {
        fileBuilder.addFunction(fontFunSpec.toWrapper())
    }
}