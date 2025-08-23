package io.composeflow.model

import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode

interface CodeGenerator {
    /**
     * Generate the Compose code that represents this node
     */
    fun generateCode(
        project: Project,
        node: ComposeNode = ComposeNode(),
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper = CodeBlockWrapper.of("")
}
