package io.composeflow.formatter

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper

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
                .addCode(CodeBlock.builder().add(text).build())
                .build()
        } else {
            FileSpec.builder("", "")
                .addCode(CodeBlock.builder().add(text).build())
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
    
    actual fun formatCodeBlock(codeBlock: CodeBlockWrapper, withImports: Boolean, isScript: Boolean): String {
        val fileSpec = if (isScript) {
            FileSpec.scriptBuilder("")
                .addCode(codeBlock.toKotlinPoet())
                .build()
        } else {
            FileSpec.builder("", "")
                .addCode(codeBlock.toKotlinPoet())
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