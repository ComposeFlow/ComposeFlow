package io.composeflow.formatter

import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper

actual object FormatterWrapper {
    actual fun format(
        fileName: String?,
        text: String,
        isScript: Boolean,
    ): String {
        // No-op implementation for wasmJs - return text as-is
        return text
    }

    actual fun formatCodeBlock(
        text: String,
        withImports: Boolean,
        isScript: Boolean,
    ): String {
        // No-op implementation for wasmJs - return text as-is
        return text
    }

    actual fun formatCodeBlock(
        codeBlock: CodeBlockWrapper,
        withImports: Boolean,
        isScript: Boolean,
    ): String {
        // No-op implementation for wasmJs - return CodeBlock's string representation as-is
        return codeBlock.toString()
    }
}
