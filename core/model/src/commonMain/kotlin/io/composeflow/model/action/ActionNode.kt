package io.composeflow.model.action

import androidx.compose.runtime.mutableStateListOf
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.kotlinpoet.wrapper.CodeBlockWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.serializer.FallbackMutableStateListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

typealias ActionNodeId = String

sealed interface FocusableActionNode {
    val id: ActionNodeId

    fun getFocusedAction(): Action?
}

@Serializable
sealed interface ActionNode {
    val id: ActionNodeId

    /**
     * Returns is the node is a valid state. For example, when the action is null for a [Simple]
     * node, it's considered as invalid.
     */
    fun isValid(): Boolean

    /**
     * Replace the action for the matching [Simple] node.
     *
     * @returns the action node itself after the matching node is replaced.
     *
     */
    fun replaceAction(
        id: ActionNodeId,
        action: Action,
    ): ActionNode

    /**
     * Remove the action with the node that matches the given [id].
     */
    fun removeAction(id: ActionNodeId)

    /**
     * Find a [Simple] action node that matches the given [id]. returns null if matching node isn't
     * found.
     */
    fun findFocusableActionOrNull(id: ActionNodeId): FocusableActionNode?

    fun hasActionNode(id: ActionNodeId): Boolean

    /**
     * Returns list of actions contained in the ActionNode.
     */
    fun allActions(): List<Action>

    fun isDependent(sourceId: String): Boolean

    fun generateCodeBlock(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlockWrapper

    fun generateInitializationCodeBlocks(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): List<CodeBlockWrapper?>

    @Serializable
    @SerialName("Simple")
    data class Simple(
        override val id: ActionNodeId = Uuid.random().toString(),
        val action: Action? = null,
    ) : ActionNode,
        FocusableActionNode {
        override fun getFocusedAction(): Action? = action

        override fun replaceAction(
            id: ActionNodeId,
            action: Action,
        ): ActionNode =
            if (this.id == id) {
                action.asActionNode(this.id)
            } else {
                this
            }

        // Delegates the removal of the Simple action in the parent container
        override fun removeAction(id: ActionNodeId) {}

        override fun isValid(): Boolean = action != null

        override fun findFocusableActionOrNull(id: ActionNodeId): FocusableActionNode? = if (this.id == id) this else null

        override fun hasActionNode(id: ActionNodeId): Boolean = this.id == id

        override fun allActions(): List<Action> = action?.let { listOf(it) } ?: emptyList()

        override fun isDependent(sourceId: String): Boolean = action is CallApi && action.apiId == sourceId

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper =
            action?.let {
                val builder = CodeBlockWrapper.builder()
                it
                    .generateActionTriggerCodeBlock(project, context, dryRun = dryRun)
                    ?.let { codeBlock: CodeBlockWrapper ->
                        builder.add(codeBlock)
                    }
                builder.addStatement("")
                builder.build()
            } ?: CodeBlockWrapper.of("")

        override fun generateInitializationCodeBlocks(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): List<CodeBlockWrapper?> = listOf(action?.generateInitializationCodeBlock(project, context, dryRun = dryRun))
    }

    @Serializable
    @SerialName("Conditional")
    data class Conditional(
        override val id: ActionNodeId = Uuid.random().toString(),
        val ifCondition: AssignableProperty = BooleanProperty.Empty,
        @Serializable(FallbackMutableStateListSerializer::class)
        val trueNodes: MutableList<ActionNode> = mutableStateListOf(),
        @Serializable(FallbackMutableStateListSerializer::class)
        val falseNodes: MutableList<ActionNode> = mutableStateListOf(),
    ) : ActionNode {
        override fun isValid(): Boolean {
            val trueNodesResult =
                trueNodes.all { trueNode ->
                    when (trueNode) {
                        is Conditional ->
                            trueNode.trueNodes.all { it.isValid() } &&
                                trueNode.falseNodes.all { it.isValid() }

                        is Simple -> trueNode.isValid()
                        is Forked -> trueNode.isValid()
                    }
                }
            val falseNodesResult =
                falseNodes.all { falseNode ->
                    when (falseNode) {
                        is Conditional ->
                            falseNode.trueNodes.all { it.isValid() } &&
                                falseNode.falseNodes.all { it.isValid() }

                        is Simple -> falseNode.isValid()
                        is Forked -> falseNode.isValid()
                    }
                }
            return ifCondition != BooleanProperty.Empty && trueNodesResult && falseNodesResult
        }

        override fun replaceAction(
            id: ActionNodeId,
            action: Action,
        ): ActionNode {
            val trueNodeIndex = trueNodes.indexOfFirst { it.id == id && it is FocusableActionNode }
            if (trueNodeIndex != -1) {
                val newActionNode = action.asActionNode(actionNodeId = id)
                trueNodes[trueNodeIndex] = newActionNode
                return this
            }
            val falseNodeIndex =
                falseNodes.indexOfFirst { it.id == id && it is FocusableActionNode }
            if (falseNodeIndex != -1) {
                val newActionNode = action.asActionNode(actionNodeId = id)
                falseNodes[falseNodeIndex] = newActionNode
                return this
            }
            return this
        }

        override fun removeAction(id: ActionNodeId) {
            fun removeFromMutableList(list: MutableList<ActionNode>) {
                val index = list.indexOfFirst { it.id == id }
                if (index != -1) {
                    list.removeAt(index)
                }
            }
            removeFromMutableList(trueNodes)
            removeFromMutableList(falseNodes)
        }

        override fun findFocusableActionOrNull(id: ActionNodeId): FocusableActionNode? {
            val result =
                trueNodes.firstOrNull { it.id == id && it is FocusableActionNode }
                    ?: falseNodes.firstOrNull { it.id == id && it is FocusableActionNode }
            return if (result != null) {
                result as FocusableActionNode
            } else {
                null
            }
        }

        override fun hasActionNode(id: ActionNodeId): Boolean = trueNodes.any { it.id == id } || falseNodes.any { it.id == id }

        override fun allActions(): List<Action> =
            trueNodes.flatMap {
                it.allActions()
            } +
                falseNodes.flatMap {
                    it.allActions()
                }

        override fun isDependent(sourceId: String): Boolean =
            ifCondition.isDependent(sourceId) ||
                trueNodes.any { it.isDependent(sourceId) } ||
                falseNodes.any { it.isDependent(sourceId) }

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper {
            val builder = CodeBlockWrapper.builder()
            if (trueNodes.isEmpty() && falseNodes.isEmpty()) {
                return builder.build()
            }
            ifCondition.addReadProperty(project, context, dryRun = dryRun)
            builder.add("if (")
            builder.add(
                ifCondition.transformedCodeBlock(
                    project,
                    context,
                    ComposeFlowType.BooleanType(),
                    dryRun = dryRun,
                ),
            )
            builder.addStatement(") {")
            trueNodes.forEach { actionNode ->
                builder.add(actionNode.generateCodeBlock(project, context, dryRun))
            }
            if (falseNodes.isNotEmpty()) {
                builder.addStatement("} else {")
                falseNodes.forEach { actionNode ->
                    builder.add(actionNode.generateCodeBlock(project, context, dryRun = dryRun))
                }
            }
            builder.addStatement("}")
            return builder.build()
        }

        override fun generateInitializationCodeBlocks(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): List<CodeBlockWrapper?> =
            trueNodes.flatMap {
                it.generateInitializationCodeBlocks(
                    project,
                    context,
                    dryRun,
                )
            } +
                falseNodes.flatMap {
                    it.generateInitializationCodeBlocks(
                        project,
                        context,
                        dryRun,
                    )
                }
    }

    /**
     * Action node that has both branches depending on the result of the forked action.
     */
    @Serializable
    @SerialName("Forked")
    data class Forked(
        override val id: ActionNodeId = Uuid.random().toString(),
        val forkedAction: Action,
        @Serializable(FallbackMutableStateListSerializer::class)
        val trueNodes: MutableList<ActionNode> = mutableStateListOf(),
        @Serializable(FallbackMutableStateListSerializer::class)
        val falseNodes: MutableList<ActionNode> = mutableStateListOf(),
    ) : ActionNode,
        FocusableActionNode {
        override fun getFocusedAction(): Action = forkedAction

        override fun isValid(): Boolean {
            val trueNodesResult =
                trueNodes.all { trueNode ->
                    when (trueNode) {
                        is Conditional ->
                            trueNode.trueNodes.all { it.isValid() } &&
                                trueNode.falseNodes.all { it.isValid() }

                        is Simple -> trueNode.isValid()
                        is Forked -> true
                    }
                }
            val falseNodesResult =
                falseNodes.all { falseNode ->
                    when (falseNode) {
                        is Conditional ->
                            falseNode.trueNodes.all { it.isValid() } &&
                                falseNode.falseNodes.all { it.isValid() }

                        is Simple -> falseNode.isValid()
                        is Forked -> true
                    }
                }
            return trueNodesResult && falseNodesResult
        }

        override fun replaceAction(
            id: ActionNodeId,
            action: Action,
        ): ActionNode {
            if (this.id == id) {
                val newActionNode = action.asActionNode(actionNodeId = id)
                if (newActionNode is Forked) {
                    newActionNode.trueNodes.addAll(trueNodes)
                    newActionNode.falseNodes.addAll(falseNodes)
                }
                return newActionNode
            }

            val trueNodeIndex = trueNodes.indexOfFirst { it.id == id && it is FocusableActionNode }
            if (trueNodeIndex != -1) {
                val newActionNode = action.asActionNode(actionNodeId = id)
                trueNodes[trueNodeIndex] = newActionNode
                return this
            }
            val falseNodeIndex =
                falseNodes.indexOfFirst { it.id == id && it is FocusableActionNode }
            if (falseNodeIndex != -1) {
                val newActionNode = action.asActionNode(actionNodeId = id)
                falseNodes[falseNodeIndex] = newActionNode
                return this
            }
            return this
        }

        override fun removeAction(id: ActionNodeId) {
            fun removeFromMutableList(list: MutableList<ActionNode>) {
                val index = list.indexOfFirst { it.id == id }
                if (index != -1) {
                    list.removeAt(index)
                }
            }
            removeFromMutableList(trueNodes)
            removeFromMutableList(falseNodes)
        }

        override fun findFocusableActionOrNull(id: ActionNodeId): FocusableActionNode? {
            var result: FocusableActionNode? = if (id == this.id) this else null
            if (result != null) return result
            result =
                trueNodes.firstOrNull { it.id == id && it is FocusableActionNode } as FocusableActionNode?
                    ?: falseNodes.firstOrNull { it.id == id && it is FocusableActionNode } as FocusableActionNode?
            return result
        }

        override fun hasActionNode(id: ActionNodeId): Boolean =
            id == this.id ||
                trueNodes.any { it.id == id } ||
                falseNodes.any { it.id == id }

        override fun allActions(): List<Action> =
            trueNodes.flatMap {
                it.allActions()
            } +
                falseNodes.flatMap {
                    it.allActions()
                } + forkedAction

        override fun isDependent(sourceId: String): Boolean =
            trueNodes.any { it.isDependent(sourceId) } ||
                falseNodes.any { it.isDependent(sourceId) } ||
                forkedAction.isDependent(sourceId)

        override fun generateInitializationCodeBlocks(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): List<CodeBlockWrapper?> {
            val trueBlockBuilder = CodeBlockWrapper.builder()
            if (trueNodes.isNotEmpty()) {
                trueNodes.forEach { actionNode ->
                    trueBlockBuilder.add(actionNode.generateCodeBlock(project, context, dryRun))
                }
            }
            val falseBlockBuilder = CodeBlockWrapper.builder()
            if (falseNodes.isNotEmpty()) {
                falseNodes.forEach { actionNode ->
                    falseBlockBuilder.add(actionNode.generateCodeBlock(project, context, dryRun))
                }
            }
            val initializationBlock =
                forkedAction
                    .generateInitializationCodeBlock(
                        project,
                        context,
                        dryRun = dryRun,
                        additionalCodeBlocks =
                            arrayOf(
                                trueBlockBuilder.build(),
                                falseBlockBuilder.build(),
                            ),
                    )
            return trueNodes.flatMap {
                it.generateInitializationCodeBlocks(
                    project,
                    context,
                    dryRun = dryRun,
                )
            } +
                falseNodes.flatMap {
                    it.generateInitializationCodeBlocks(
                        project,
                        context,
                        dryRun = dryRun,
                    )
                } +
                initializationBlock
        }

        override fun generateCodeBlock(
            project: Project,
            context: GenerationContext,
            dryRun: Boolean,
        ): CodeBlockWrapper =
            forkedAction.generateActionTriggerCodeBlock(project, context, dryRun)
                ?: CodeBlockWrapper.of("")
    }
}
