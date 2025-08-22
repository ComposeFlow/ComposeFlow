package io.composeflow.formatter

/**
 * Multiplatform wrapper for code formatting functionality.
 * On JVM, delegates to ktlint. On WASM, provides alternative formatting.
 */
expect object FormatterWrapper {
    /**
     * Format a Kotlin file represented as text.
     * @param fileName The name of the file (used for ktlint context)
     * @param text The Kotlin code to format
     * @param isScript Whether this is a Kotlin script (.kts) file
     * @return The formatted Kotlin code
     */
    fun format(fileName: String?, text: String, isScript: Boolean = false): String
    
    /**
     * Format code block text, optionally including imports.
     * @param text The Kotlin code to format
     * @param withImports Whether to include import statements in the result
     * @param isScript Whether this is a Kotlin script (.kts) file
     * @return The formatted Kotlin code
     */
    fun formatCodeBlock(text: String, withImports: Boolean = false, isScript: Boolean = true): String
}