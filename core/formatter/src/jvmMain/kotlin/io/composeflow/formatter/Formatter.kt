package io.composeflow.formatter

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec

object Formatter {
    private val ruleSets =
        setOf(
            StandardRuleSetProvider().get(),
        )
    private val userData =
        mapOf(
            "android" to true.toString(),
        )

    fun format(fileSpec: FileSpec): String =
        KtLint.format(
            KtLint.Params(
                fileName = fileSpec.name,
                text = fileSpec.toString(),
                ruleSets = ruleSets,
                userData = userData,
                cb = { _, _ -> run {} },
                script = false,
                editorConfigPath = null,
                debug = false,
            ),
        )

    fun format(
        codeBlock: CodeBlock,
        withImports: Boolean = false,
    ): String {
        val fileSpec =
            FileSpec
                .scriptBuilder("")
                .addCode(codeBlock)
                .build()
        val formatted =
            KtLint.format(
                KtLint.Params(
                    fileName = null,
                    text = fileSpec.toString(),
                    ruleSets = ruleSets,
                    userData = userData,
                    cb = { _, _ -> run {} },
                    script = true,
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

fun FileSpec.Builder.suppressWarningTypes(vararg types: String) {
    if (types.isEmpty()) {
        return
    }

    val format = "%S,".repeat(types.count()).trimEnd(',')
    addAnnotation(
        AnnotationSpec
            .builder(ClassName("", "Suppress"))
            .addMember(format, *types)
            .build(),
    )
}

/**
 * This is to suppress having the public visibility modifier for the generated file.
 * By design, Kotlinpoet adds public visibility modifier https://github.com/square/kotlinpoet/issues/1205.
 * There is no way to avoid having the public modifier at the moment.
 * This method at least suppress the warning by having redundant public modifiers.
 */
fun FileSpec.Builder.suppressRedundantVisibilityModifier() {
    suppressWarningTypes("RedundantVisibilityModifier")
}
