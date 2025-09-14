package io.composeflow.ui.uibuilder

import co.touchlab.kermit.Logger
import io.composeflow.Res
import io.composeflow.can_not_add_this_node
import io.composeflow.ksp.LlmParam
import io.composeflow.ksp.LlmTool
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.BottomAppBarTrait
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.getOperationTargetNode
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.ui.EventResult
import io.composeflow.ui.UiBuilderHelper
import io.composeflow.ui.UiBuilderHelper.addNodeToCanvasEditable
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString

/**
 * Handles operations related to UI builder, such as adding or removing nodes.
 * Operations in this class are exposed to the LLM to allow them call it as tools as well as used
 * from the GUI in ComposeFlow.
 */
@Suppress("ktlint:standard:max-line-length")
class UiBuilderOperator {
    /**
     * Pre operations before adding a node to a container node to validate if the node can be added.
     */
    suspend fun onPreAddComposeNodeToContainerNode(
        project: Project,
        containerNodeId: String,
        composeNode: ComposeNode,
    ): EventResult {
        val eventResult = EventResult()
        val containerNode = project.screenHolder.currentEditable().findNodeById(containerNodeId)
        if (containerNode == null) {
            eventResult.errorMessages.add(
                "Container node with ID $containerNodeId not found.",
            )
            return eventResult
        }
        val errorMessages = composeNode.checkConstraints(containerNode)
        if (errorMessages.isNotEmpty()) {
            eventResult.errorMessages.addAll(errorMessages)
            return eventResult
        }
        if (!containerNode.trait.value.isDroppable()) {
            eventResult.errorMessages.add(
                "You can't drop a node to ${containerNode.trait.value.iconText()}",
            )
            return eventResult
        }
        if (!composeNode.trait.value.canBeAddedAsChildren()) {
            eventResult.errorMessages.add(
                getString(Res.string.can_not_add_this_node),
            )
            return eventResult
        }
        if (TraitCategory.ScreenOnly in composeNode.trait.value.paletteCategories()) {
            val error =
                UiBuilderHelper.checkIfNodeCanBeAddedDueToScreenOnlyNode(
                    currentEditable = project.screenHolder.currentEditable(),
                    composeNode = composeNode,
                )
            error?.let {
                eventResult.errorMessages.add(error)
                return eventResult
            }
        }

        if (TraitCategory.ScreenOnly !in composeNode.trait.value.paletteCategories()) {
            composeNode.parentNode = containerNode
        }
        return eventResult
    }

    fun onAddComposeNodeToContainerNode(
        project: Project,
        containerNodeId: String,
        composeNode: ComposeNode,
        indexToDrop: Int,
    ) {
        val containerNode = project.screenHolder.currentEditable().findNodeById(containerNodeId)
        containerNode?.let {
            addNodeToCanvasEditable(
                project = project,
                containerNode = containerNode,
                composeNode = composeNode,
                canvasEditable = project.screenHolder.currentEditable(),
                indexToDrop = indexToDrop,
            )
        }
    }

    @LlmTool(
        name = "add_compose_node_to_container",
        description = "Adds a Compose UI component to a container node in the UI builder. This allows placing UI elements inside containers like Column, Row, or Box.",
    )
    suspend fun onAddComposeNodeToContainerNode(
        project: Project,
        @LlmParam(
            description = "The ID of the container node where the component will be added. Must be a node that can contain other components.",
        )
        containerNodeId: String,
        @LlmParam(description = "The YAML representation of the ComposeNode node to be added to the container.")
        composeNodeYaml: String,
        @LlmParam(
            description = "The position index where the component should be inserted in the container.",
        )
        indexToDrop: Int,
    ): EventResult {
        val result = EventResult()
        try {
            val composeNode: ComposeNode = decodeFromStringWithFallback(composeNodeYaml)
            val eventResult =
                onPreAddComposeNodeToContainerNode(
                    project,
                    containerNodeId,
                    composeNode,
                )
            if (eventResult.errorMessages.isNotEmpty()) {
                eventResult.errorMessages.forEach {
                    Logger.e(it)
                }
            } else {
                onAddComposeNodeToContainerNode(
                    project = project,
                    containerNodeId = containerNodeId,
                    composeNode = composeNode,
                    indexToDrop = indexToDrop,
                )
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error adding compose node to container" }
            result.errorMessages.add("Error adding compose node to container: ${e.message}")
        }
        return result
    }

    fun onPreRemoveComposeNode(composeNode: ComposeNode?): EventResult {
        val result = EventResult()
        val errorMessage = composeNode?.checkIfNodeIsDeletable()
        if (errorMessage != null) {
            result.errorMessages.add(errorMessage)
            return result
        }
        return result
    }

    fun onPreRemoveComposeNodes(composeNodes: List<ComposeNode>): EventResult {
        val result = EventResult()
        composeNodes.forEach { composeNode ->
            val errorMessage = composeNode.checkIfNodeIsDeletable()
            if (errorMessage != null) {
                result.errorMessages.add(errorMessage)
            }
        }
        return result
    }

    @LlmTool(
        name = "remove_compose_node",
        description = "Removes a Compose UI component from the UI builder.",
    )
    fun onRemoveComposeNode(
        project: Project,
        @LlmParam(description = "The ID of the node to be removed.")
        composeNodeId: String,
    ): EventResult {
        val nodeToRemove = project.screenHolder.currentEditable().findNodeById(composeNodeId)

        val eventResult = onPreRemoveComposeNode(nodeToRemove)
        if (eventResult.errorMessages.isNotEmpty()) {
            eventResult.errorMessages.forEach {
                Logger.e(it)
            }
        } else {
            removeComposeNodeFromProject(project, nodeToRemove)
        }
        return eventResult
    }

    private fun removeComposeNodeFromProject(
        project: Project,
        nodeToRemove: ComposeNode?,
    ) {
        if (TraitCategory.ScreenOnly in (
                nodeToRemove?.trait?.value?.paletteCategories()
                    ?: emptyList()
            )
        ) {
            val canvasEditable = project.screenHolder.currentEditable()
            when (nodeToRemove?.trait?.value) {
                is FabTrait -> {
                    (canvasEditable as? Screen)?.fabNode?.value = null
                }

                is TopAppBarTrait -> {
                    (canvasEditable as? Screen)?.topAppBarNode?.value = null
                }

                is BottomAppBarTrait -> {
                    (canvasEditable as? Screen)?.bottomAppBarNode?.value = null
                }

                is NavigationDrawerTrait -> {
                    (canvasEditable as? Screen)?.navigationDrawerNode?.value = null
                }

                else -> {}
            }
        } else {
            val operationTarget = nodeToRemove?.getOperationTargetNode(project)
            operationTarget?.removeFromParent()
        }
    }

    @LlmTool(
        name = "remove_compose_nodes",
        description = "Removes Compose UI components from the UI builder.",
    )
    fun onRemoveComposeNodes(
        project: Project,
        @LlmParam(description = "The IDs of the nodes to be removed.")
        composeNodeIds: List<String>,
    ): EventResult {
        val nodesToRemove =
            composeNodeIds.mapNotNull {
                project.screenHolder.currentEditable().findNodeById(it)
            }

        val eventResult = onPreRemoveComposeNodes(nodesToRemove)
        if (eventResult.errorMessages.isNotEmpty()) {
            eventResult.errorMessages.forEach {
                Logger.e(it)
            }
        } else {
            nodesToRemove.forEach {
                removeComposeNodeFromProject(project, it)
            }
        }
        return eventResult
    }

    @LlmTool(
        name = "add_modifier",
        description = "Adds a new modifier to a Compose UI component. Modifiers are used to change the appearance or behavior of components, such as adding padding, setting size, or changing colors.",
    )
    fun onAddModifier(
        project: Project,
        @LlmParam(description = "The ID of the node to add the modifier to.")
        composeNodeId: String,
        @LlmParam(description = "The YAML representation of the modifier to add.")
        modifierYaml: String,
    ): EventResult {
        val result = EventResult()
        try {
            val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
            if (node == null) {
                Logger.e { "Node with ID $composeNodeId not found." }
                result.errorMessages.add("Node with ID $composeNodeId not found.")
                return result
            }

            val modifier: ModifierWrapper = decodeFromStringWithFallback(modifierYaml)
            onAddModifier(
                project,
                composeNodeId,
                modifier,
            )
        } catch (e: Exception) {
            Logger.e(e) { "Error adding modifier to node" }
            result.errorMessages.add("Error adding modifier to node: ${e.message}")
        }
        return result
    }

    fun onAddModifier(
        project: Project,
        composeNodeId: String,
        modifier: ModifierWrapper,
    ) {
        val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
        node?.modifierList?.add(modifier)
    }

    @LlmTool(
        name = "update_modifier",
        description = "Updates an existing modifier on a Compose UI component at a specific index.",
    )
    fun onUpdateModifier(
        project: Project,
        @LlmParam(description = "The ID of the node whose modifier will be updated.")
        composeNodeId: String,
        @LlmParam(description = "The index of the modifier to update.")
        index: Int,
        @LlmParam(description = "The YAML representation of the new modifier.")
        modifierYaml: String,
    ): EventResult {
        val result = EventResult()
        try {
            val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
            if (node == null) {
                Logger.e { "Node with ID $composeNodeId not found." }
                result.errorMessages.add("Node with ID $composeNodeId not found.")
                return result
            }

            if (index < 0 || index >= node.modifierList.size) {
                Logger.e { "Invalid modifier index: $index. Node has ${node.modifierList.size} modifiers." }
                result.errorMessages.add("Invalid modifier index: $index. Node has ${node.modifierList.size} modifiers.")
                return result
            }

            val modifier: ModifierWrapper = decodeFromStringWithFallback(modifierYaml)
            onUpdateModifier(project, composeNodeId, index, modifier)
        } catch (e: Exception) {
            Logger.e(e) { "Error updating modifier at index $index" }
            result.errorMessages.add("Error updating modifier at index $index: ${e.message}")
        }
        return result
    }

    fun onUpdateModifier(
        project: Project,
        composeNodeId: String,
        index: Int,
        modifier: ModifierWrapper,
    ) {
        val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
        node?.modifierList?.set(index, modifier)
    }

    @LlmTool(
        name = "remove_modifier",
        description = "Removes a modifier from a Compose UI component at a specific index.",
    )
    fun onRemoveModifier(
        project: Project,
        @LlmParam(description = "The ID of the node whose modifier will be removed.")
        composeNodeId: String,
        @LlmParam(description = "The index of the modifier to remove.")
        index: Int,
    ): EventResult {
        val result = EventResult()
        try {
            val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
            if (node == null) {
                Logger.e { "Node with ID $composeNodeId not found." }
                result.errorMessages.add("Node with ID $composeNodeId not found.")
                return result
            }

            if (index < 0 || index >= node.modifierList.size) {
                Logger.e { "Invalid modifier index: $index. Node has ${node.modifierList.size} modifiers." }
                result.errorMessages.add("Invalid modifier index: $index. Node has ${node.modifierList.size} modifiers.")
                return result
            }

            node.modifierList.removeAt(index)
        } catch (e: Exception) {
            Logger.e(e) { "Error removing modifier at index $index" }
            result.errorMessages.add("Error removing modifier at index $index: ${e.message}")
        }
        return result
    }

    @LlmTool(
        name = "update_trait",
        description = "Updates the trait of a Compose UI component.",
    )
    fun onUpdateTrait(
        project: Project,
        @LlmParam(description = "The ID of the node whose trait will be updated.")
        composeNodeId: String,
        @LlmParam(description = "The YAML representation of the new trait.")
        traitYaml: String,
    ): EventResult {
        val result = EventResult()
        try {
            val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
            if (node == null) {
                Logger.e { "Node with ID $composeNodeId not found." }
                result.errorMessages.add("Node with ID $composeNodeId not found.")
                return result
            }

            val trait: ComposeTrait = decodeFromStringWithFallback(traitYaml)
            onUpdateTrait(project, composeNodeId, trait)
        } catch (e: Exception) {
            Logger.e(e) { "Error updating trait for node $composeNodeId" }
            // TODO Use string resource for error messages
            //      https://github.com/ComposeFlow/ComposeFlow/issues/175
            result.errorMessages.add("Error updating trait: ${e.message}")
        }
        return result
    }

    fun onUpdateTrait(
        project: Project,
        composeNodeId: String,
        trait: ComposeTrait,
    ) {
        val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
        node?.let {
            val oldTrait = it.trait.value
            it.trait.value = trait
            if (it.label.value == oldTrait.iconText()) {
                it.label.value = trait.iconText()
            }
        }
    }

    @LlmTool(
        name = "swap_modifiers",
        description = "Swaps the positions of two modifiers on a Compose UI component.",
    )
    fun onSwapModifiers(
        project: Project,
        @LlmParam(description = "The ID of the node whose modifiers will be swapped.")
        composeNodeId: String,
        @LlmParam(description = "The index of the first modifier to swap.")
        fromIndex: Int,
        @LlmParam(description = "The index of the second modifier to swap.")
        toIndex: Int,
    ): EventResult {
        val result = EventResult()
        try {
            val node = project.screenHolder.currentEditable().findNodeById(composeNodeId)
            if (node == null) {
                Logger.e { "Node with ID $composeNodeId not found." }
                result.errorMessages.add("Node with ID $composeNodeId not found.")
                return result
            }

            if (fromIndex < 0 ||
                fromIndex >= node.modifierList.size ||
                toIndex < 0 ||
                toIndex >= node.modifierList.size
            ) {
                Logger.e { "Invalid modifier indices: from=$fromIndex, to=$toIndex. Node has ${node.modifierList.size} modifiers." }
                result.errorMessages.add(
                    "Invalid modifier indices: from=$fromIndex, to=$toIndex. Node has ${node.modifierList.size} modifiers.",
                )
                return result
            }

            // Use move operation instead of swap for reorderable compatibility
            val item = node.modifierList.removeAt(fromIndex)
            node.modifierList.add(toIndex, item)
        } catch (e: Exception) {
            Logger.e(e) { "Error swapping modifiers at indices $fromIndex and $toIndex" }
            result.errorMessages.add("Error swappigng modifiers at indices $fromIndex and $toIndex: ${e.message}")
        }
        return result
    }

    @LlmTool(
        name = "move_compose_node_to_container",
        description = "Moves a UI component to a specific position within a container component. This allows reordering components within their parent container or moving them to a different container.",
    )
    fun onMoveComposeNodeToContainer(
        project: Project,
        @LlmParam(description = "The ID of the node to be moved.")
        composeNodeId: String,
        @LlmParam(description = "The ID of the container where the component should be moved to.")
        containerNodeId: String,
        @LlmParam(description = "The index where the component should be inserted in the container (0-based).")
        index: Int,
    ): EventResult {
        val eventResult = onPreMoveComposeNodeToPosition(project, composeNodeId, containerNodeId)
        if (eventResult.errorMessages.isNotEmpty()) {
            eventResult.errorMessages.forEach {
                Logger.w { it }
            }
            return eventResult
        }
        try {
            val composeNode =
                project.screenHolder.currentEditable().findNodeById(composeNodeId)
                    ?: return EventResult().also { it.errorMessages.add("Node '$composeNodeId' not found") }
            val containerNode =
                project.screenHolder.currentEditable().findNodeById(containerNodeId)
                    ?: return EventResult().also { it.errorMessages.add("Container '$containerNodeId' not found") }
            containerNode.insertChildAt(index = index, child = composeNode)
            if (containerNode == composeNode.parentNode) {
                // This means two identical nodes exist at the same time before the old node is
                // removed in the same parent. In that case, we want to make sure the original
                // node that originated the drag is removed.
                composeNode.removeFromParent(excludeIndex = index)
            } else {
                composeNode.removeFromParent()
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error moving component to position" }
            eventResult.errorMessages.add("Error moving component to position: ${e.message}")
        }
        return eventResult
    }

    fun onPreMoveComposeNodeToPosition(
        project: Project,
        composeNodeId: String,
        containerNodeId: String,
    ): EventResult {
        val result = EventResult()
        val composeNode =
            project.screenHolder.currentEditable().findNodeById(composeNodeId)
                ?: return result.also { it.errorMessages.add("Node '$composeNodeId' not found") }
        val containerNode =
            project.screenHolder.currentEditable().findNodeById(containerNodeId)
                ?: return result.also { it.errorMessages.add("Node '$containerNodeId' not found") }
        val errorMessages = composeNode.checkConstraints(containerNode)
        if (errorMessages.isNotEmpty()) {
            result.errorMessages.addAll(errorMessages)
        }
        return result
    }

    @LlmTool(
        name = "get_project_issues",
        description = "Retrieves all current issues in the project including invalid references, type mismatches, configuration problems, and other validation errors. This helps identify and resolve problems in the UI design.",
    )
    fun onGetProjectIssues(project: Project): EventResult {
        val result = EventResult()
        try {
            val issues = project.generateTrackableIssues()
            result.issues.addAll(issues)

            Logger.i { "Found ${issues.size} project issues" }
            issues.forEach { trackableIssue ->
                val contextInfo =
                    when (val context = trackableIssue.destinationContext) {
                        is io.composeflow.model.project.issue.DestinationContext.UiBuilderScreen ->
                            "Screen: ${context.canvasEditableId}, Node: ${context.composeNodeId}"

                        is io.composeflow.model.project.issue.DestinationContext.ApiEditorScreen ->
                            "API: ${context.apiId}"
                    }
                Logger.i { "Issue: $contextInfo - ${trackableIssue.issue::class.simpleName}" }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error retrieving project issues" }
            result.errorMessages.add("Error retrieving project issues: ${e.message}")
        }
        return result
    }

    @LlmTool(
        name = "list_screens",
        description = "Lists all screens in the project with their IDs and names. This helps understand the available screens in the project for navigation and modification purposes.",
    )
    fun onListScreens(project: Project): EventResult {
        val result = EventResult()
        try {
            val screens = project.screenHolder.screens
            val screenList = screens.map { it.toScreenSummary() }

            Logger.i { "Listed ${screens.size} screens in project" }
            screens.forEach { screen ->
                Logger.i {
                    "Screen: ${screen.id} - ${screen.name} (default: ${screen.isDefault.value}, selected: ${screen.isSelected.value})"
                }
            }

            // Success - no action needed, data will be stored in ToolArgs.result by ToolDispatcher
        } catch (e: Exception) {
            Logger.e(e) { "Error listing screens" }
            result.errorMessages.add("Error listing screens: ${e.message}")
        }
        return result
    }

    @LlmTool(
        name = "get_screen_details",
        description = "Retrieves detailed information about a specific screen including its complete structure as YAML. This provides full access to screen configuration, components, and state for analysis or modification.",
    )
    fun onGetScreenDetails(
        project: Project,
        @LlmParam(description = "The ID of the screen to retrieve details for.")
        screenId: String,
    ): EventResult {
        val result = EventResult()
        try {
            val screen = project.screenHolder.findScreen(screenId)
            if (screen == null) {
                Logger.e { "Screen with ID '$screenId' not found" }
                result.errorMessages.add("Screen with ID '$screenId' not found")
            } else {
                Logger.i { "Retrieved details for screen: ${screen.name} (${screen.id})" }
                // Success - screen details will be stored in ToolArgs.result by ToolDispatcher
            }
        } catch (e: Exception) {
            Logger.e(e) { "Error retrieving screen details for ID: $screenId" }
            result.errorMessages.add("Error retrieving screen details: ${e.message}")
        }
        return result
    }
}

/**
 * Data class representing a screen summary for AI tools.
 */
@Serializable
data class ScreenSummary(
    val id: String,
    val name: String,
    val title: String,
    val label: String,
    val isDefault: Boolean,
    val isSelected: Boolean,
    val showOnNavigation: Boolean,
)

fun Screen.toScreenSummary() =
    ScreenSummary(
        id = id,
        name = name,
        title = title.value,
        label = label.value,
        isDefault = isDefault.value,
        isSelected = isSelected.value,
        showOnNavigation = showOnNavigation.value,
    )
