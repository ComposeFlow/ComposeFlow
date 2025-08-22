package io.composeflow.model.project.theme

import io.composeflow.font.FontFamilyWrapper
import io.composeflow.kotlinpoet.wrapper.FileSpecBuilderWrapper

internal actual fun addFontFamilyFunSpec(fileBuilder: FileSpecBuilderWrapper, fontFamily: FontFamilyWrapper) {
    // No-op on WASM since code generation is not supported
}