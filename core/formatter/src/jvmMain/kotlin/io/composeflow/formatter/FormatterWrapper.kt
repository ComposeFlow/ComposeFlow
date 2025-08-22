package io.composeflow.formatter

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec

actual object FormatterWrapper {
    private val ruleSets = setOf(
        StandardRuleSetProvider().get(),
    )
    
    private val userData = mapOf(
        "android" to true.toString(),
    )
    
    actual fun format(fileName: String?, text: String, isScript: Boolean): String =
        KtLint.format(
            KtLint.Params(
                fileName = fileName,
                text = text,
                ruleSets = ruleSets,
                userData = userData,
                cb = { _, _ -> run {} },
                script = isScript,
                editorConfigPath = null,
                debug = false,
            ),
        )
    
    actual fun formatCodeBlock(text: String, withImports: Boolean, isScript: Boolean): String {
        val fileSpec = if (isScript) {
            FileSpec.scriptBuilder("")
                .addCode(CodeBlock.of(text))
                .build()
        } else {
            FileSpec.builder("", "")
                .addCode(CodeBlock.of(text))
                .build()
        }
        
        val formatted = KtLint.format(
            KtLint.Params(
                fileName = null,
                text = fileSpec.toString(),
                ruleSets = ruleSets,
                userData = userData,
                cb = { _, _ -> run {} },
                script = isScript,
                editorConfigPath = null,
                debug = false,
            ),
        )
        
        return if (withImports) {
            formatted
        } else {
            formatted
                .lines()
                .filter { it.isNotEmpty() && !it.startsWith("import") }
                .joinToString("\n")
        }
    }
}