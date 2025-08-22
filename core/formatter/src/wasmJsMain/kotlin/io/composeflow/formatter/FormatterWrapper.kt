package io.composeflow.formatter

actual object FormatterWrapper {
    actual fun format(fileName: String?, text: String, isScript: Boolean): String {
        // No-op implementation for wasmJs - return text as-is
        return text
    }
    
    actual fun formatCodeBlock(text: String, withImports: Boolean, isScript: Boolean): String {
        // No-op implementation for wasmJs - return text as-is
        return text
    }
}