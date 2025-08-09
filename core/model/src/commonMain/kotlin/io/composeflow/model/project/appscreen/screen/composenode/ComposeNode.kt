package io.composeflow.model.project.appscreen.screen.composenode

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.window.core.layout.WindowWidthSizeClass
import com.charleskorn.kaml.YamlNode
import com.squareup.kotlinpoet.CodeBlock
import io.composeflow.eachEquals
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.IdMap
import io.composeflow.model.createNewIdIfNotPresent
import io.composeflow.model.enumwrapper.NodeVisibility
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.modifier.toModifierChain
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.CardTrait
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.ComponentTrait
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.EmptyTrait
import io.composeflow.model.parameter.FabTrait
import io.composeflow.model.parameter.HorizontalPagerTrait
import io.composeflow.model.parameter.LazyColumnTrait
import io.composeflow.model.parameter.LazyHorizontalGridTrait
import io.composeflow.model.parameter.LazyListTrait
import io.composeflow.model.parameter.LazyRowTrait
import io.composeflow.model.parameter.LazyVerticalGridTrait
import io.composeflow.model.parameter.NavigationDrawerTrait
import io.composeflow.model.parameter.RowTrait
import io.composeflow.model.parameter.ScreenTrait
import io.composeflow.model.parameter.TabContentTrait
import io.composeflow.model.parameter.TabRowTrait
import io.composeflow.model.parameter.TabTrait
import io.composeflow.model.parameter.TopAppBarTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.component.ComponentId
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.project.findComponentOrNull
import io.composeflow.model.project.findComposeNodeOrThrow
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.issue.TrackableIssue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.model.property.EnumProperty
import io.composeflow.model.state.ScreenState
import io.composeflow.model.state.StateId
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.serializer.FallbackActionHandlerSerializer
import io.composeflow.serializer.MutableStateListSerializer
import io.composeflow.serializer.MutableStateSerializer
import io.composeflow.serializer.decodeFromStringWithFallback
import io.composeflow.serializer.decodeFromYamlNodeWithFallback
import io.composeflow.serializer.encodeToString
import io.composeflow.serializer.parseToYamlNode
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.adaptive.computeWindowAdaptiveInfo
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import replaceIdInYamlNode
import kotlin.uuid.Uuid

@Serializable
@SerialName("ComposeNode")
data class ComposeNode(
    val id: String = Uuid.random().toString(),
    @Serializable(with = MutableStateSerializer::class)
    val trait: MutableState<ComposeTrait> = mutableStateOf(EmptyTrait),
    @Serializable(MutableStateSerializer::class)
    val label: MutableState<String> = mutableStateOf(trait.value.iconText()),
    /**
     * The level of this node in the tree. The root node's level is 0.
     * This is var intentionally because when the default ComposeNode has a child initially,
     * (For example, Button has a Text inside) the level of its child needs to be updated when
     * the parentNode is dragged onto the canvas.
     */
    var level: Int = 0,
    /**
     * The list of modifier wrappers attached to the content when this node is rendered.
     * Explicitly store the list of modifier so that it can be accessible in a later phase
     * (e.g. inspect/modify a specific modifier) because Compose doesn't expose a way to access a
     * specific modifier due to its declarative nature.
     */
    @Serializable(with = MutableStateListSerializer::class)
    val modifierList: MutableList<ModifierWrapper> = mutableStateListEqualsOverrideOf(),
    /**
     * The direct children that belong to this node.
     */
    @Serializable(with = MutableStateListSerializer::class)
    val children: MutableList<ComposeNode> = mutableStateListEqualsOverrideOf(),
    /**
     * Parameters only used if when this node is a child of any LazyList (such as LazyColumn, LazyRow)
     */
    @Serializable(with = MutableStateSerializer::class)
    val lazyListChildParams: MutableState<LazyListChildParams> = mutableStateOf(LazyListChildParams.FixedNumber()),
    @Serializable(with = MutableStateSerializer::class)
    val dynamicItems: MutableState<AssignableProperty?> = mutableStateOf(null),
    @Serializable(with = FallbackActionHandlerSerializer::class)
    val actionHandler: ActionHandler = ActionHandlerImpl(),
    val inspectable: Boolean = true,
    @Serializable(with = MutableStateSerializer::class)
    val visibilityParams: MutableState<VisibilityParams> = mutableStateOf(VisibilityParams()),
    /**
     * Updated to true when this node is focused. For example when it's clicked.
     * Intentionally non-Transient meaning it's serialized in the yaml representation.
     * So that information which nodes are focused in visual editor to LLM to give it more context.
     */
    @Serializable(MutableStateSerializer::class)
    val isFocused: MutableState<Boolean> = mutableStateOf(false),
    /**
     * Updated to true when the mouse cursor is hovered on this node.
     */
    @Transient
    val isHovered: MutableState<Boolean> = mutableStateOf(false),
    /**
     * Updated to true when another ComposeNode is being dragged within the bounds of this node.
     */
    @Transient
    val isDraggedOnBounds: MutableState<Boolean> = mutableStateOf(false),
    /**
     * The index to which index the new node is dropped. Which is used  when a composable is being
     * dragged and indicates the position within a container.
     * This is only used for when this node is a container.
     */
    @Transient
    val indexToBeDropped: MutableState<Int> = mutableStateOf(0),
    /**
     * Bounds relative to window when placed inside the editor canvas.
     */
    @Transient
    val boundsInWindow: MutableState<Rect> = mutableStateOf(Rect(0f, 0f, 0f, 0f)),
    /**
     * Reference to the componentId if this node is part of a component.
     * When this has a non-null value, this and children of this node can't be edited and
     * should be rendered by referencing the definition of the original component.
     * Null if the ComposeNode isn't part of a component.
     */
    val componentId: ComponentId? = null,
    /**
     * Set to true if node is drawn as part of a component
     */
    @Transient
    var isPartOfComponent: Boolean = false,
    /**
     * ID of the [ComposeNode], which is the original node before this node is copied for rendering
     * in the canvas. When the ComposeNode is rendered from a component, it copies the latest
     * definition from the component, that sub tree isn't merged into the original node tree.
     * Thus any modification such as copy, move operation needs to be against the original node
     * instead of the copied node. Thus, originalNodeId needs to be stored explicitly.
     */
    @Transient
    var originalNodeId: String? = null,
    @Transient
    private val tabsNode: TabsNode = TabsNodeImpl(),
    @Transient
    private val lazyListChildNode: LazyListChildNode = LazyListChildNodeImpl(),
    @Transient
    private val topAppBarNode: TopAppBarNode = TopAppBarNodeImpl(),
    @Transient
    private val bottomAppBarNode: BottomAppBarNode = BottomAppBarNodeImpl(),
    @Transient
    private val resizeHandler: ResizeHandler = ResizeHandlerImpl(),
    @Transient
    private val componentHandler: ComponentHandler = ComponentHandlerImpl(),
    @Transient
    val pendingModifier: MutableState<ModifierWrapper?> = mutableStateOf(null),
    @Transient
    val pendingModifierCommittedIndex: MutableState<Int?> = mutableStateOf(null),
) : TabsNode by tabsNode,
    LazyListChildNode by lazyListChildNode,
    ResizeHandler by resizeHandler,
    ActionHandler by actionHandler,
    TopAppBarNode by topAppBarNode,
    BottomAppBarNode by bottomAppBarNode,
    ComponentHandler by componentHandler {
    companion object {
        fun createRootNode() =
            ComposeNode(
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.FillMaxWidth(),
                        ModifierWrapper.Weight(1f),
                    ),
                trait = mutableStateOf(ColumnTrait()),
                label = mutableStateOf("Root"),
                inspectable = false,
            )
    }

    init {
        lazyListChildNode.self = this
        tabsNode.self = this
        resizeHandler.self = this
        topAppBarNode.self = this
        bottomAppBarNode.self = this
        componentHandler.self = this
        trait.value.actionTypes().forEach { actionType ->
            if (actionsMap[actionType] == null) {
                actionsMap[actionType] = mutableStateListOf()
            }
        }

        // Verify the visibilityCondition. If LLM-generated parameters set some condition, but failed
        // to set the nodeVisibility
        if (visibilityParams.value.visibilityCondition !is BooleanProperty.Empty) {
            visibilityParams.value =
                visibilityParams.value.copy(nodeVisibility = EnumProperty(value = NodeVisibility.Conditional))
        }
    }

    @Transient
    override var self: ComposeNode = this

    /**
     * Fallback ID when the id value isn't unique within a screen.
     * In a LLM generated screen, sometimes duplicated IDs are assigned, that leads to a crash
     * in ComposeFlow. This is to mitigate potential crashes by providing a unique identifier.
     */
    @Transient
    val fallbackId = "${label.value}-$id"

    /**
     * StateId that coexists with this node.
     */
    @Transient
    val companionStateId: StateId = "$id-companionState"

    fun displayName(project: Project): String =
        if (trait.value is ComponentTrait) {
            componentId?.let {
                project.findComponentOrNull(it)?.name
            } ?: label.value
        } else {
            if (label.value != trait.value.iconText()) {
                label.value + " (${trait.value.iconText()})"
            } else {
                label.value
            }
        }

    internal fun modifiersIncludingPending(): MutableList<ModifierWrapper> =
        pendingModifier.value?.let {
            mutableListOf(it).apply {
                addAll(modifierList)
            }
        } ?: modifierList

    /**
     * Returns the chain of Modifier including the [pendingModifier] so that uncommitted Modifier
     * is visible in the canvas, such as Height modifier while the ComposeNode is being resized.
     */
    fun modifierChainForCanvas(): Modifier = modifiersIncludingPending().toModifierChain()

    /**
     * Copy the instance with the same values except for the [id]. [id]s are replaced while keeping
     * the relationships with the references. This is to make sure the uniqueness of the [id]s
     * within the project when copying a [ComposeNode] (e.g. pasting a copied node) while keeping
     * the value equality of the instances.
     *
     * @param idMap Map that holds the mapping information between the existing IDs and new IDs.
     */
    fun copyExceptId(idMap: IdMap = mutableMapOf()): ComposeNode {
        val newId = idMap.createNewIdIfNotPresent(id)
        val restored =
            this.copy(
                id = newId,
                children =
                    mutableStateListEqualsOverrideOf<ComposeNode>().apply {
                        addAll(
                            children.map {
                                it.copyExceptId(
                                    idMap,
                                )
                            },
                        )
                    },
                componentId = componentId,
            )
        restored.parentNode = parentNode
        return restored
    }

    /**
     * Reference to the parent node.
     * This is not a constructor argument intentionally to avoid infinite loop when toString() is
     * invoked.
     *
     * Also, to avoid the infinite loop when serializing the [ComposeNode] making this as transient.
     */
    @Transient
    var parentNode: ComposeNode? = null

    @Suppress("ktlint:standard:backing-property-naming")
    private val _allChildren: List<ComposeNode>
        get() = children + children.flatMap { it._allChildren }

    fun allChildren(includeSelf: Boolean = true): List<ComposeNode> =
        if (includeSelf) {
            _allChildren + this
        } else {
            _allChildren
        }

    fun findDeepestContainerAtOrNull(position: Offset): ComposeNode? =
        findDeepestChildAt(position) {
            it.isPositionWithinBounds(position) &&
                it.isContainer() &&
                it.isVisibleInCanvas()
        }

    fun findDeepestChildAtOrNull(position: Offset): ComposeNode? =
        findDeepestChildAt(position) {
            it.isPositionWithinBounds(position) && it.isVisibleInCanvas()
        }

    /**
     * Check if this node can be deletable from the parent.
     *
     * Returns error message if this isn' deletable. Returns null otherwise.
     */
    fun checkIfNodeIsDeletable(): String? {
        if (trait.value is TabRowTrait || trait.value is TabContentTrait) {
            return "Deleting this isn't supported. Remove Tabs or individual Tab"
        }
        if (trait.value is TabTrait) {
            if (parentNode?.children?.size == 1) {
                return "At least one tab is necessary"
            }
        }
        if (trait.value is ScreenTrait || isContentRoot()) {
            return "This can't be removed."
        }
        return null
    }

    /**
     * Remove this node from the [children] of the [parentNode].
     *
     * If [excludeIndex] is specified, it doesn't remove the element if its index is
     * the specified index.
     * This is to only remove the desired element. For example when in the middle of moving a
     * node to another index, it may be possible where the two same nodes exist at the same time
     * except for its index.
     */
    fun removeFromParent(excludeIndex: Int? = null) {
        if (excludeIndex != null) {
            val indexes =
                parentNode?.children?.mapIndexed { index, node ->
                    if (index != excludeIndex && node == this) {
                        index to node
                    } else {
                        null
                    }
                }
            indexes?.forEach { pair ->
                pair?.let {
                    parentNode?.children?.removeAt(it.first)
                }
            }
        } else {
            var removeIndex: Int? = null
            parentNode?.children?.forEachIndexed { i, child ->
                if (child.id == this.id) {
                    removeIndex = i
                }
            }
            removeIndex?.let {
                parentNode?.children?.removeAt(it)
            }
        }
        updateChildParentRelationships()
    }

    fun findFirstFocusedNodeOrNull(): ComposeNode? =
        allChildren().firstOrNull { it.isFocused.value }

    fun findFocusedNodes(): List<ComposeNode> = allChildren().filter { it.isFocused.value }

    fun clearIsDraggedOnBoundsRecursively() =
        doRecursively { node, _, _ ->
            node.isDraggedOnBounds.value = false
        }

    fun clearIndexToBeDroppedRecursively() =
        doRecursively { node, _, _ ->
            node.indexToBeDropped.value = 0
        }

    fun clearIsFocusedRecursively() = doRecursively { node, _, _ -> node.isFocused.value = false }

    fun clearIsHoveredRecursively() = doRecursively { node, _, _ -> node.isHovered.value = false }

    fun setIsPartOfComponentRecursively(value: Boolean) =
        doRecursively { node, _, _ -> node.isPartOfComponent = value }

    /**
     * Check if any constraints are not violated against the parent [ComposeNode].
     *
     * @return the list of error messages if any constraints are violated.
     */
    fun checkConstraints(parent: ComposeNode): List<String> =
        trait.value.defaultConstraints().mapNotNull {
            it.getErrorIfInvalid(parent)
        }

    fun insertChildAt(
        index: Int = children.size,
        child: ComposeNode,
    ) {
        val parent = this
        check(parent.isContainer())

        // Fallback to the index that doesn't throw exception to mitigate the chance of ignoring the
        // AI generated content with the index
        val fallbackIndex =
            if (index < 0) {
                0
            } else if (index >= parent.children.size) {
                parent.children.size
            } else {
                index
            }
        children.add(
            fallbackIndex,
            child.copy().apply {
                parentNode = parent
                level = parent.level + 1
            },
        )
        updateChildParentRelationships()

        // If this node is a LazyList, set the default number of items from the
        // parameter
        when (val lazyListParams = trait.value) {
            is LazyListTrait -> {
                if (child.lazyListChildParams.value is LazyListChildParams.FixedNumber) {
                    child.lazyListChildParams.value =
                        LazyListChildParams.FixedNumber(lazyListParams.defaultChildNumOfItems)
                }
            }

            else -> {}
        }
    }

    fun addChild(child: ComposeNode) = insertChildAt(children.size, child)

    /**
     * Finds the ComposeNode up until the root node including a node where paletteNode == Screen
     */
    fun findNodesUntilRoot(includeSelf: Boolean = false): List<ComposeNode> {
        var parent = parentNode
        val result = mutableListOf<ComposeNode>()
        if (includeSelf) {
            result.add(this)
        }
        while (parent != null) {
            result.add(parent)
            parent = parent.parentNode
        }
        return result
    }

    /**
     * Finds the nodes up until the content root (exclude the node where paletteNode == Screen)
     */
    private fun findNodesUntilContentRoot(includeSelf: Boolean = false): List<ComposeNode> {
        var parent = parentNode
        val result = mutableListOf<ComposeNode>()
        if (includeSelf) {
            result.add(this)
        }
        while (parent != null && parent.trait.value !is ScreenTrait) {
            result.add(parent)
            parent = parent.parentNode
        }
        return result
    }

    fun findNearestContainerOrNull(): ComposeNode? {
        var target: ComposeNode? = this
        while (target != null) {
            if (target.isContainer()) {
                return target
            }
            target = target.parentNode
        }
        return null
    }

    fun findRoot(): ComposeNode {
        var current = this
        while (!current.isRoot()) {
            current = current.parentNode!!
        }
        return current
    }

    fun isContainer(): Boolean =
        TraitCategory.Container in trait.value.paletteCategories() ||
            TraitCategory.TabContent in trait.value.paletteCategories()

    fun isRoot(): Boolean =
        parentNode == null &&
            trait.value !is FabTrait

    fun isContentRoot(): Boolean = parentNode?.trait?.value is ScreenTrait

    fun bringToFront() {
        val selfLevel = level
        val siblings = findRoot().allChildren().filter { it.level == selfLevel }
        var maxZIndex = 0f
        siblings.filter { it != this }.forEach { node ->
            node.modifierList.forEach { modifier ->
                if (modifier is ModifierWrapper.ZIndex) {
                    if (modifier.zIndex != null && maxZIndex < modifier.zIndex) {
                        maxZIndex = modifier.zIndex
                    }
                }
            }
        }
        modifierList.removeIf { it is ModifierWrapper.ZIndex }
        modifierList.add(ModifierWrapper.ZIndex(maxZIndex + 1))
    }

    fun sendToBack() {
        val selfLevel = level
        val siblings = findRoot().allChildren().filter { it.level == selfLevel }
        var minZIndex = 0f
        siblings.filter { it != this }.forEach { node ->
            node.modifierList.forEach { modifier ->
                if (modifier is ModifierWrapper.ZIndex) {
                    if (modifier.zIndex != null && minZIndex > modifier.zIndex) {
                        minZIndex = modifier.zIndex
                    }
                }
            }
        }
        modifierList.removeIf { it is ModifierWrapper.ZIndex }
        modifierList.add(ModifierWrapper.ZIndex(minZIndex - 1))
    }

    fun updateDropIndex(
        project: Project,
        draggedPosition: Offset,
    ) {
        if (!isContainer() || !isPositionWithinBounds(draggedPosition)) {
            return
        }

        run loop@{
            when (trait.value) {
                is ColumnTrait, is TabContentTrait, is NavigationDrawerTrait -> {
                    children.forEachIndexed { index, child ->
                        if (index == 0 && draggedPosition.y < child.boundsInWindow.value.center.y) {
                            indexToBeDropped.value = 0
                            return@loop
                        }
                        if (child.boundsInWindow.value.center.y <= draggedPosition.y) {
                            indexToBeDropped.value = index + 1
                        }
                    }
                }

                is RowTrait, is HorizontalPagerTrait -> {
                    children.forEachIndexed { index, child ->
                        if (index == 0 && draggedPosition.x < child.boundsInWindow.value.center.x) {
                            indexToBeDropped.value = 0
                            return@loop
                        }
                        if (child.boundsInWindow.value.center.x <= draggedPosition.x) {
                            indexToBeDropped.value = index + 1
                        }
                    }
                }

                is LazyColumnTrait, is LazyVerticalGridTrait -> {
                    children.forEachIndexed { index, child ->
                        val boundsIncludingChildren = child.boundsIncludingShadowChildren(project)
                        if (index == 0 && draggedPosition.y < boundsIncludingChildren.center.y) {
                            indexToBeDropped.value = 0
                            return@loop
                        }
                        if (boundsIncludingChildren.center.y <= draggedPosition.y) {
                            indexToBeDropped.value = index + 1
                        }
                    }
                }

                is LazyRowTrait, is LazyHorizontalGridTrait -> {
                    children.forEachIndexed { index, child ->
                        val boundsIncludingChildren = child.boundsIncludingShadowChildren(project)
                        if (index == 0 && draggedPosition.x < boundsIncludingChildren.center.x) {
                            indexToBeDropped.value = 0
                            return@loop
                        }
                        if (boundsIncludingChildren.center.x <= draggedPosition.x) {
                            indexToBeDropped.value = index + 1
                        }
                    }
                }

                else -> {
                    // no-op
                }
            }
        }
    }

    /**
     * Returns bounds including shadow children (a child which can't be edited in the canvas).
     * This is used for calculating the bounds including children for a LazyList (such as LazyColumn
     * and LazyRow)
     */
    private fun boundsIncludingShadowChildren(project: Project): Rect {
        val childParams = lazyListChildParams
        return parentNode?.let { lazyList ->
            childParams.value.boundsIncludingChildren(
                project = project,
                lazyList = lazyList,
                self = this,
            )
        } ?: Rect.Zero
    }

    /**
     * Returns true if the any children (including this node) is dependent on the data source
     * having [sourceId].
     */
    fun isDependent(sourceId: String): Boolean {
        val dynamicItemsDependency =
            allChildren().any { it.dynamicItems.value?.isDependent(sourceId) == true }
        val actionDependency =
            allChildren().any { child ->
                child.actionsMap.any {
                    it.value.any { actionNode ->
                        actionNode.isDependent(sourceId)
                    }
                }
            }
        val paramsDependency =
            allChildren().any {
                it.trait.value.getPropertyContainers().any { propertyContainer ->
                    propertyContainer.assignableProperty?.isDependent(sourceId) == true
                }
            }
        val visibilityDependency = visibilityParams.value.isDependent(sourceId)
        return dynamicItemsDependency || actionDependency || paramsDependency || visibilityDependency
    }

    fun generateCode(
        project: Project,
        context: GenerationContext,
        dryRun: Boolean,
    ): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        val visibilityParams = visibilityParams.value
        if (visibilityParams.alwaysVisible()) {
            codeBlockBuilder.add(
                trait.value.generateCode(
                    project = project,
                    node = this,
                    context = context,
                    dryRun = dryRun,
                ),
            )
        } else {
            codeBlockBuilder.add("if (")
            codeBlockBuilder.add(
                visibilityParams.generateVisibilityCondition(project, context, dryRun = dryRun),
            )
            codeBlockBuilder.addStatement(") {")
            codeBlockBuilder.add(
                trait.value.generateCode(
                    project = project,
                    node = this,
                    context = context,
                    dryRun = dryRun,
                ),
            )
            codeBlockBuilder.addStatement("}")
        }
        // Wrap with specific code if any actions inside the Composable have a non-null CodeBlock by
        // generateWrapWithComposableBlock
        val wrappedCode =
            allActions()
                .filter { it.generateWrapWithComposableBlock(codeBlockBuilder.build()) != null }
                .fold(initial = codeBlockBuilder.build()) { acc, element ->
                    element.generateWrapWithComposableBlock(acc) ?: acc
                }

        val wrappedCodeByProperties =
            (
                trait.value
                    .getPropertyContainers()
                    .map { it.assignableProperty } + dynamicItems.value
                ).filter { it?.generateWrapWithComposableBlock(project, wrappedCode) != null }
                .fold(initial = wrappedCode) { acc, element ->
                    element?.generateWrapWithComposableBlock(project, acc) ?: acc
                }
        return wrappedCodeByProperties
    }

    /**
     * Render this node in the canvas.
     */
    @Composable
    fun RenderedNodeInCanvas(
        project: Project,
        canvasNodeCallbacks: CanvasNodeCallbacks,
        paletteRenderParams: PaletteRenderParams,
        zoomableContainerStateHolder: ZoomableContainerStateHolder,
        modifier: Modifier = Modifier,
    ) {
        val visibilityParams = visibilityParams.value
        if (!visibilityParams.visibleInUiBuilder) return

        if (!visibilityParams.alwaysVisible()) {
            val windowAdaptiveInfo = computeWindowAdaptiveInfo()
            when (windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass) {
                WindowWidthSizeClass.COMPACT -> {
                    if (!visibilityParams.formFactorVisibility.visibleInCompact) {
                        return
                    }
                }

                WindowWidthSizeClass.MEDIUM -> {
                    if (!visibilityParams.formFactorVisibility.visibleInMedium) {
                        return
                    }
                }

                WindowWidthSizeClass.EXPANDED -> {
                    if (!visibilityParams.formFactorVisibility.visibleInExpanded) {
                        return
                    }
                }
            }
        }

        if (trait.value is ComponentTrait && componentId != null) {
            val componentRootNode = getComponentRootNode(project, componentId)
            // Create a copy of ths Component wrapper to not actually add children to the
            // original node tree
            if (componentRootNode != null) {
                val copiedComponentWrapper = this.copyExceptId()
                copiedComponentWrapper.originalNodeId = this.id
                copiedComponentWrapper.addChild(componentRootNode)
                copiedComponentWrapper.trait.value.RenderedNode(
                    project = project,
                    node = copiedComponentWrapper,
                    canvasNodeCallbacks = canvasNodeCallbacks,
                    paletteRenderParams = paletteRenderParams,
                    zoomableContainerStateHolder = zoomableContainerStateHolder,
                    modifier = copiedComponentWrapper.modifierChainForCanvas(),
                )
            } else {
                Row {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                    )
                    Text(
                        text = "No component found for $componentId",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else {
            trait.value.RenderedNode(
                project = project,
                node = this,
                canvasNodeCallbacks = canvasNodeCallbacks,
                paletteRenderParams = paletteRenderParams,
                zoomableContainerStateHolder = zoomableContainerStateHolder,
                modifier = modifier,
            )
        }
    }

    /**
     * Update the focus state of this node.
     * if [toggleValue] is true,
     */
    fun setFocus(toggleValue: Boolean = false) {
        setTabSelectedIndex()
        if (toggleValue) {
            isFocused.value = isFocused.value.not()
        } else {
            isFocused.value = true
        }
    }

    fun updateChildParentRelationships(rootToUpdate: ComposeNode = findRoot()) {
        // Make sure levels and child/parent relationships are set across all nodes
        rootToUpdate.doRecursively(
            level = rootToUpdate.level,
            parentNode = rootToUpdate.parentNode,
        ) { node, parent, level ->
            node.level = level
            node.parentNode = parent
            node.parentNode?.level = level - 1
        }
    }

    /**
     * Update the ComoseNode reference to all the AssignableProperties
     */
    fun updateComposeNodeReferencesForTrait() {
        trait.value.updateCompanionStateProperties(this)
    }

    /**
     * Returns true if the Composable label in the canvas should be drawn inside the bounds of the
     * Composable.
     */
    fun showInvertedLabel(labelHeight: Float): Boolean {
        val trait = trait.value
        return if (trait is TopAppBarTrait) {
            true
        } else if (trait is FabTrait) {
            false
        } else if (parentNode?.trait?.value is ScreenTrait) {
            true
        } else {
            val nodesUntilRootReversed = findNodesUntilContentRoot()
            val nearestCutBounds =
                nodesUntilRootReversed
                    .firstOrNull {
                        it.trait.value.isLazyList() ||
                            it.trait.value is CardTrait ||
                            it.isContentRoot()
                    }?.boundsInWindow
            nearestCutBounds?.value?.let {
                if (boundsInWindow.value.top - it.top < labelHeight) {
                    true
                } else {
                    false
                }
            } ?: true
        }
    }

    fun getCompanionStateOrNull(project: Project): ScreenState<*>? =
        trait.value.companionState(this)

    fun getCompanionStates(project: Project): List<ScreenState<*>> {
        val statesFromTrait =
            trait.value.companionState(this)?.let {
                listOf(it)
            } ?: emptyList()
        val statesFromActions =
            allActions().mapNotNull {
                it.companionState(project)
            }

        return statesFromTrait + statesFromActions
    }

    fun isPositionWithinBounds(position: Offset) =
        position.x in boundsInWindow.value.left..boundsInWindow.value.right &&
            position.y in boundsInWindow.value.top..boundsInWindow.value.bottom

    private fun findDeepestChildAt(
        position: Offset = Offset.Zero,
        candidates: MutableSet<ComposeNode> = mutableSetOf(),
        predicate: (ComposeNode) -> Boolean,
    ): ComposeNode? {
        if (predicate(this)) {
            candidates.add(this)
        } else {
            return null
        }
        children.forEach { it.findDeepestChildAt(position, candidates, predicate) }
        val maxLevel = candidates.maxOf { it.level }
        return candidates
            .filter { it.level == maxLevel }
            .minBy {
                it.boundsInWindow.value.width * it.boundsInWindow.value.height
            }
    }

    private fun doRecursively(
        level: Int = 0,
        parentNode: ComposeNode? = null,
        operation: (self: ComposeNode, parent: ComposeNode?, level: Int) -> Unit,
    ) {
        operation(this, parentNode, level)
        val self = this
        children.forEach {
            it.doRecursively(level + 1, self, operation)
        }
    }

    @Suppress("unused")
    fun printRecursively(project: Project? = null) {
        doRecursively { self, _, level ->
            println(
                "  ".repeat(level) +
                    "name: ${
                        project?.let {
                            self.displayName(
                                it,
                            )
                        } ?: self.label.value
                    }, id: ${self.id}, isFocused: ${self.isFocused.value}, isHovered: ${self.isHovered.value}",
            )
        }
    }

    /**
     * Detect any issues in this node. When the user clicks on an issue, the ComposeFlow's UI
     * should navigate to the screen and focus on the node.
     */
    fun generateTrackableIssues(project: Project): List<TrackableIssue> {
        val paramIssues =
            trait.value.getPropertyContainers().flatMap {
                it.generateTrackableIssues(project = project, composeNode = this)
            }
        val canvasEditable = project.findCanvasEditableHavingNodeOrNull(this) ?: return emptyList()
        val issuesFromParams =
            canvasEditable.let {
                trait.value.generateIssues(project).map {
                    TrackableIssue(
                        destinationContext =
                            DestinationContext.UiBuilderScreen(
                                canvasEditableId = canvasEditable.id,
                                composeNodeId = this.id,
                            ),
                        issue = it,
                    )
                }
            }

        val actionIssues =
            allActions().flatMap { it.generateIssues(project) }.map { issue ->
                TrackableIssue(
                    destinationContext =
                        DestinationContext.UiBuilderScreen(
                            canvasEditableId = canvasEditable.id,
                            composeNodeId = this.id,
                        ),
                    issue = issue,
                )
            }
        val modifierIssues =
            if (isContentRoot()) {
                emptyList()
            } else {
                modifierList
                    .flatMap { it.generateIssues(this.parentNode?.trait?.value) }
                    .map { issue ->
                        TrackableIssue(
                            destinationContext =
                                DestinationContext.UiBuilderScreen(
                                    canvasEditableId = canvasEditable.id,
                                    composeNodeId = this.id,
                                ),
                            issue = issue,
                        )
                    }
            }
        val visibilityParamsIssues =
            visibilityParams.value.generateTrackableIssues(project, canvasEditable, this)
        return paramIssues + issuesFromParams + actionIssues + modifierIssues + visibilityParamsIssues
    }

    /**
     * Checks if the properties of this instance equals to the other.
     * The properties as [MutableState] are compared with its values.
     *
     * This is to check the equality of the instance restored by deserializing the encoded instance.
     */
    fun contentEquals(
        other: ComposeNode,
        excludeId: Boolean = true,
    ): Boolean {
        val idEquality =
            if (excludeId) {
                true
            } else {
                id == other.id
            }
        val companionStateIdEquality =
            if (excludeId) {
                true
            } else {
                companionStateId == other.companionStateId
            }
        val otherEquality =
            trait.value == other.trait.value &&
                label.value == other.label.value &&
                modifierList.eachEquals(
                    other = other.modifierList,
                    predicate = { t1, t2 ->
                        t1 == t2
                    },
                ) &&
                trait.value == other.trait.value &&
                children.eachEquals(
                    other = other.children,
                    predicate = { t1, t2 ->
                        t1.contentEquals(t2)
                    },
                )
        return idEquality &&
            companionStateIdEquality &&
            otherEquality
    }
}

/**
 * Restore the same [ComposeNode] instance that has same values.
 *
 * @param sameId When set to true, the same [ComposeNode.id] values are used for the new instance.
 */
fun ComposeNode.restoreInstance(sameId: Boolean = false): ComposeNode {
    val encoded = encodeToString(this)
    return if (sameId) {
        decodeFromStringWithFallback<ComposeNode>(encoded)
    } else {
        val rootNode: YamlNode = parseToYamlNode(encoded)
        val modifiedNode: YamlNode = replaceIdInYamlNode(rootNode)
        val modifiedComposeNode =
            decodeFromYamlNodeWithFallback(ComposeNode.serializer(), modifiedNode)
        modifiedComposeNode
    }
}

/**
 * Get the ComposeNode which will be the target against the mutational operations such as
 * copy, delete, move a ComposeNode.
 * This is needed because The actual sub tree under a Component (reusable set of Composables) is
 * only created when the node tree is rendered instead of being merged with the actual ComposeNode
 * tree.
 */
fun ComposeNode.getOperationTargetNode(project: Project): ComposeNode =
    originalNodeId?.let {
        project.findComposeNodeOrThrow(it)
    } ?: this
