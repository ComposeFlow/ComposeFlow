package io.composeflow.font

import co.touchlab.kermit.Logger

actual fun FontFamilyWrapper.generateFontFamilyFunSpec(): Any {
    Logger.i("FontFamilyWrapper.generateFontFamilyFunSpec called on WASM - code generation not supported")
    return Unit // Return Unit as a placeholder since code generation isn't supported on WASM
}
