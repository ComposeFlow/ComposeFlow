package io.composeflow.model.validator

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.project.Project

interface ComposeStateValidator {
    fun asCodeBlock(
        project: Project,
        context: GenerationContext,
    ): CodeBlockWrapper
}
