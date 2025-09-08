package io.composeflow.ui.uibuilder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.composeflow.MainViewUiState
import io.composeflow.Res
import io.composeflow.analytics.AnalyticsTracker
import io.composeflow.asClassName
import io.composeflow.auth.FirebaseIdToken
import io.composeflow.dynamic_items_is_used_only_once_warning
import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionType
import io.composeflow.model.apieditor.ApiDefinition
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.palette.PaletteDraggable
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.ColumnTrait
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.ParameterWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.VisibilityParams
import io.composeflow.model.project.appscreen.screen.composenode.getOperationTargetNode
import io.composeflow.model.project.appscreen.screen.composenode.restoreInstance
import io.composeflow.model.project.component.Component
import io.composeflow.model.project.copy
import io.composeflow.model.project.findCanvasEditableHavingNodeOrNull
import io.composeflow.model.project.findComponentOrThrow
import io.composeflow.model.project.findComposeNodeOrNull
import io.composeflow.model.project.issue.DestinationContext
import io.composeflow.model.project.replaceNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.settings.ComposeBuilderSettings
import io.composeflow.model.settings.DarkThemeSetting
import io.composeflow.model.settings.SettingsRepository
import io.composeflow.model.useroperation.OperationHistory
import io.composeflow.model.useroperation.UserOperation
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.repository.ProjectRepository
import io.composeflow.swap
import io.composeflow.template.ScreenTemplatePair
import io.composeflow.template.ScreenTemplates
import io.composeflow.ui.EventResult
import io.composeflow.ui.FormFactor
import io.composeflow.ui.UiBuilderHelper
import io.composeflow.ui.UiBuilderHelper.addNodeToCanvasEditable
import io.composeflow.ui.calculateScale
import io.composeflow.ui.common.buildUiState
import io.composeflow.ui.utils.isMinusPressed
import io.composeflow.ui.utils.isPlusPressed
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.util.generateUniqueName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.jetbrains.compose.resources.getString
import kotlin.uuid.Uuid

class UiBuilderViewModel(
    firebaseIdToken: FirebaseIdToken,
    private val project: Project,
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val projectRepository: ProjectRepository = ProjectRepository(firebaseIdToken),
    private val uiBuilderOperator: UiBuilderOperator = UiBuilderOperator(),
    private val onUpdateProject: (Project) -> Unit,
) : ViewModel() {
    // Expose settings repository for onboarding
    val settings: SettingsRepository get() = settingsRepository

    // Project that is updated when the the project that is being edited.
    // This may conflicts with the [project] field in this ViewModel, but to detect the real time
    // updates when the Project.ScreenHolder.pending* fields, having this field.
    // This may need to be commonized with the [project] field
    val editingProject =
        projectRepository.editingProject.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Project(),
        )

    var draggedNode by mutableStateOf<ComposeNode?>(null)
        private set

    var copiedNodes by mutableStateOf<List<ComposeNode>>(emptyList())
        private set

    private val appDarkThemeSetting =
        settingsRepository.appDarkThemeSetting.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    private val composeBuilderSettings =
        settingsRepository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ComposeBuilderSettings(),
        )

    private val canvasTopToolbarUiState: StateFlow<CanvasTopToolbarUiState> =
        viewModelScope.buildUiState(
            appDarkThemeSetting,
            composeBuilderSettings,
        ) { appDarkThemeSetting, composeBuilderSettings ->
            CanvasTopToolbarUiState(
                isDarkMode = appDarkThemeSetting,
                onDarkModeChanged = { isDarkMode ->
                    settingsRepository.saveAppDarkTheme(
                        darkThemeSetting =
                            if (isDarkMode) {
                                DarkThemeSetting.Dark
                            } else {
                                DarkThemeSetting.Light
                            },
                    )
                },
                showBorders = composeBuilderSettings.showBordersInCanvas,
                onShowBordersChanged = {
                    settingsRepository.saveShowBorders(it)
                },
            )
        }

    val canvasAreaUiState: StateFlow<CanvasAreaUiState> =
        viewModelScope.buildUiState(
            canvasTopToolbarUiState,
            appDarkThemeSetting,
        ) { canvasTopToolbarUiState, _ ->
            CanvasAreaUiState(
                topToolbarUiState = canvasTopToolbarUiState,
            )
        }

    val mainViewUiState: StateFlow<MainViewUiState> =
        viewModelScope.buildUiState(
            appDarkThemeSetting,
        ) { appDarkTheme ->
            MainViewUiState(
                appDarkTheme = appDarkTheme,
            )
        }

    var zoomableContainerStateHolder by mutableStateOf(ZoomableContainerStateHolder())

    var formFactor by mutableStateOf<FormFactor>(FormFactor.Phone())
        private set

    private var uiBuilderCanvasSizeDp by mutableStateOf(IntSize(0, 0))

    var leftPaneDividerPosition by mutableStateOf(380.dp)
        private set

    var inspectorTabWidth by mutableStateOf(420.dp)
        private set

    fun onFormFactorChanged(newFormFactor: FormFactor) {
        formFactor = newFormFactor
        val maximumAvailableWidth = uiBuilderCanvasSizeDp.width
        val maximumAvailableHeight = uiBuilderCanvasSizeDp.height

        val scale =
            calculateScale(
                formFactor = formFactor,
                Size(
                    maximumAvailableWidth.toFloat(),
                    maximumAvailableHeight.toFloat(),
                ),
            )
        zoomableContainerStateHolder.onToolbarZoomScaleChanged(scale)
    }

    fun onScaleChanged(newScale: Float): EventResult {
        zoomableContainerStateHolder.onToolbarZoomScaleChanged(newScale)
        return EventResult(consumed = true)
    }

    fun onUiBuilderCanvasSizeChanged(newSize: IntSize) {
        uiBuilderCanvasSizeDp = newSize
    }

    fun onLeftPaneDividerPositionChanged(newPosition: Dp) {
        leftPaneDividerPosition = newPosition
    }

    fun onInspectorTabWidthChanged(newWidth: Dp) {
        inspectorTabWidth = newWidth
    }

    fun onDraggedPositionUpdated(
        draggedPosition: Offset,
        paletteDraggable: PaletteDraggable,
    ) {
        val root = project.screenHolder.findDeepestChildAtOrNull(draggedPosition)?.findRoot()
        project.screenHolder.currentEditable().clearIsDraggedOnBoundsRecursively()
        project.screenHolder.currentEditable().clearIndexToBeDroppedRecursively()

        val dropTarget = root?.findDeepestContainerAtOrNull(draggedPosition)
        dropTarget?.let {
            if (TraitCategory.ScreenOnly in paletteDraggable.paletteCategories()) {
                // If the paletteNode being dragged is a screen only node,
                // Update the status of root node draggedOnBounds status
                root.isDraggedOnBounds.value = true
            } else {
                it.isDraggedOnBounds.value = true
                it.updateDropIndex(project, draggedPosition)
            }
        }
    }

    fun onDragEnd() {
        project.screenHolder.getAllComposeNodes().forEach {
            it.clearIsDraggedOnBoundsRecursively()
            it.clearIndexToBeDroppedRecursively()
        }
    }

    fun onKeyPressed(keyEvent: KeyEvent): EventResult =
        if (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace) {
            onDeleteKey()
        } else if (keyEvent.key == Key.Z && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onUndo()
        } else if (keyEvent.key == Key.Y && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onRedo()
        } else if (keyEvent.key == Key.C && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onCopyFocusedNodes()
        } else if (keyEvent.key == Key.V && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onPaste()
        } else if (isPlusPressed(keyEvent) && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onScaleChanged(newScale = zoomableContainerStateHolder.scale + 0.1f)
        } else if (isMinusPressed(keyEvent) && (keyEvent.isMetaPressed || keyEvent.isCtrlPressed)) {
            onScaleChanged(newScale = zoomableContainerStateHolder.scale - 0.1f)
        } else {
            EventResult(consumed = false)
        }

    fun onDeleteKey(): EventResult {
        val focusedNodes = project.screenHolder.currentEditable().findFocusedNodes()
        val result = EventResult()
        val eventResult = uiBuilderOperator.onPreRemoveComposeNodes(focusedNodes)
        if (eventResult.errorMessages.isNotEmpty()) {
            return eventResult
        }

        // Track compose node deletion
        try {
            focusedNodes.forEach { node ->
                AnalyticsTracker.trackComposeNodeDeleted(
                    nodeType = node.label.value,
                    count = 1,
                )
            }
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        val nodesToDelete =
            focusedNodes.map {
                it.restoreInstance(sameId = true)
            }
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ComposablesDeleted(
                    nodes = nodesToDelete,
                ),
        )
        uiBuilderOperator.onRemoveComposeNodes(
            project,
            nodesToDelete.map { it.id },
        )

        saveProject(project)
        return result
    }

    fun onUndo(): EventResult {
        val undoableOperation = OperationHistory.undo(project)
        undoableOperation?.let {
            val restored = Project.deserializeFromString(undoableOperation.serializedProject)
            onUpdateProject(restored)
            saveProject(restored)
        }
        return EventResult()
    }

    fun onRedo(): EventResult {
        val undoableOperation = OperationHistory.redo(project)
        undoableOperation?.let {
            val restored =
                project.copy(
                    screenHolder =
                        Project.deserializeFromString(undoableOperation.serializedProject).screenHolder,
                )
            onUpdateProject(restored)
            saveProject(restored)
        }
        return EventResult()
    }

    fun onCopyFocusedNodes(): EventResult {
        val focused = project.screenHolder.currentEditable().findFocusedNodes()
        val result = EventResult()
        val operationTargets = focused.map { it.getOperationTargetNode(project) }

        // Track compose node copy
        try {
            if (operationTargets.isNotEmpty()) {
                AnalyticsTracker.trackComposeNodeCopied(
                    nodeType = operationTargets.joinToString(", ") { it.trait.value.toString() },
                    count = operationTargets.size,
                )
            }
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        if (operationTargets.size == 1) {
            copiedNodes = operationTargets
            result.errorMessages.add("Copied ${operationTargets[0].displayName(project)}")
        } else if (operationTargets.size > 1) {
            copiedNodes = operationTargets
            result.errorMessages.add("Copied multiple nodes")
        }
        return result
    }

    fun onPaste(): EventResult {
        val result = EventResult()
        if (copiedNodes.isEmpty()) {
            result.errorMessages.add("No Composable in clipboard")
            return result
        }

        // Track compose node paste
        try {
            if (copiedNodes.isNotEmpty()) {
                AnalyticsTracker.trackComposeNodePasted(
                    nodeType = copiedNodes.joinToString(", ") { it.trait.value.toString() },
                    count = copiedNodes.size,
                )
            }
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        copiedNodes.forEach { copied ->
            val container =
                project.screenHolder
                    .currentEditable()
                    .findFocusedNodes()
                    .firstOrNull()
                    ?.findNearestContainerOrNull()
                    ?: project.screenHolder.currentRootNode()

            container.let {
                val toBePasted = copied.restoreInstance(sameId = false)

                if (TraitCategory.ScreenOnly in copied.trait.value.paletteCategories()) {
                    val error =
                        UiBuilderHelper.checkIfNodeCanBeAddedDueToScreenOnlyNode(
                            currentEditable = project.screenHolder.currentEditable(),
                            composeNode = copied,
                        )
                    error?.let {
                        result.errorMessages.add(error)
                        return result
                    }
                }

                OperationHistory.record(
                    project = project,
                    userOperation = UserOperation.ComposablePasted(node = toBePasted),
                )

                addNodeToCanvasEditable(
                    project = project,
                    containerNode = it,
                    composeNode = toBePasted,
                    canvasEditable = project.screenHolder.currentEditable(),
                    indexToDrop = null,
                )
                saveProject(project)
            }
        }

        return result
    }

    suspend fun onComposableDroppedToTarget(
        dropPosition: Offset,
        composeNode: ComposeNode,
    ): EventResult {
        val rootNode = project.screenHolder.findDeepestChildAtOrNull(dropPosition)?.findRoot()
        val dropTarget = rootNode?.findDeepestContainerAtOrNull(dropPosition)
        var eventResult = EventResult()
        dropTarget?.let {
            eventResult =
                onAddComposeNodeToContainerNode(
                    containerNodeId = it.id,
                    composeNode = composeNode,
                    indexToDrop = it.indexToBeDropped.value,
                )
        }
        return eventResult
    }

    private suspend fun onAddComposeNodeToContainerNode(
        containerNodeId: String,
        composeNode: ComposeNode,
        indexToDrop: Int,
    ): EventResult {
        val preValidationResult =
            uiBuilderOperator.onPreAddComposeNodeToContainerNode(
                project,
                containerNodeId,
                composeNode,
            )
        if (preValidationResult.errorMessages.isNotEmpty()) {
            return preValidationResult
        }

        // Track compose node added
        try {
            val containerNode = project.findComposeNodeOrNull(containerNodeId)
            AnalyticsTracker.trackComposeNodeAdded(
                nodeType = composeNode.trait.value.toString(),
                containerType = containerNode?.trait?.value?.toString(),
                source = "palette",
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        recordOperation(
            project = project,
            userOperation =
                UserOperation.ComposableDropped(
                    node =
                        composeNode.restoreInstance(
                            sameId = true,
                        ),
                ),
        )

        uiBuilderOperator.onAddComposeNodeToContainerNode(
            project,
            containerNodeId,
            composeNode,
            indexToDrop,
        )
        saveProject(project)
        return EventResult()
    }

    fun onNodeDropToPosition(
        dropPosition: Offset,
        node: ComposeNode,
    ): EventResult {
        val deepestChild = project.screenHolder.findDeepestChildAtOrNull(dropPosition)
        val rootNode = deepestChild?.findRoot()
        val dropTarget = rootNode?.findDeepestContainerAtOrNull(dropPosition)
        val result = EventResult()
        if (node.isPositionWithinBounds(dropPosition)) {
            return result
        }
        dropTarget?.let {
            if (it == node) {
                return result
            }
            val errorMessages = node.checkConstraints(it)
            if (errorMessages.isNotEmpty()) {
                result.errorMessages.addAll(errorMessages)
                return result
            }

            val operationNode = node.getOperationTargetNode(project)
            recordOperation(
                project = project,
                userOperation =
                    UserOperation.ComposableMoved(
                        node =
                            operationNode.restoreInstance(
                                sameId = true,
                            ),
                    ),
            )

            uiBuilderOperator.onMoveComposeNodeToContainer(
                project = project,
                composeNodeId = operationNode.id,
                containerNodeId = dropTarget.id,
                index = dropTarget.indexToBeDropped.value,
            )
            saveProject(project)
        }
        return result
    }

    fun onBoundsInNodeUpdated(
        node: ComposeNode,
        boundsInWindow: Rect,
    ) {
        node.boundsInWindow.value = boundsInWindow
    }

    fun onDraggedNodeUpdated(node: ComposeNode?) {
        draggedNode = node
    }

    fun onMousePressedAt(
        eventPosition: Offset,
        isCtrlOrMetaPressed: Boolean = false,
    ) {
        project.screenHolder.updateFocusedNode(eventPosition, isCtrlOrMetaPressed)
    }

    fun onMouseHoveredAt(eventPosition: Offset) {
        project.screenHolder.updateHoveredNode(eventPosition)
    }

    fun onHoveredStatusUpdated(
        node: ComposeNode,
        isHovered: Boolean,
    ) {
        project.screenHolder.clearIsHovered()
        node.isHovered.value = isHovered
    }

    fun onFocusedStatusUpdated(node: ComposeNode) {
        project.screenHolder.clearIsFocused()
        node.setFocus()
    }

    fun onModifierUpdatedAt(
        node: ComposeNode,
        index: Int,
        wrapper: ModifierWrapper,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ModifierUpdated(
                    node = node.restoreInstance(sameId = true),
                    modifier = wrapper,
                ),
        )

        uiBuilderOperator.onUpdateModifier(
            project = project,
            composeNodeId = node.id,
            index = index,
            modifier = wrapper,
        )
        saveProject(project)
    }

    fun onModifierRemovedAt(
        node: ComposeNode,
        index: Int,
    ) {
        val toBeRemoved = node.modifierList[index]
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ModifierDeleted(
                    node = node.restoreInstance(sameId = true),
                    modifier = toBeRemoved,
                ),
        )

        // Use the UiBuilderOperator to remove the modifier
        uiBuilderOperator.onRemoveModifier(
            project = project,
            composeNodeId = node.id,
            index = index,
        )

        saveProject(project)
    }

    fun onModifierSwapped(
        node: ComposeNode,
        from: Int,
        to: Int,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ModifierSwapped(
                    node = node.restoreInstance(sameId = true),
                    from = from,
                    to = to,
                ),
        )

        // Use the UiBuilderOperator to swap the modifiers
        uiBuilderOperator.onSwapModifiers(
            project = project,
            composeNodeId = node.id,
            fromIndex = from,
            toIndex = to,
        )

        saveProject(project)
    }

    fun onModifierAdded(
        node: ComposeNode,
        wrapper: ModifierWrapper,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ModifierAdded(
                    node = node.restoreInstance(sameId = true),
                    modifier = wrapper,
                ),
        )

        uiBuilderOperator.onAddModifier(
            project = project,
            composeNodeId = node.id,
            modifier = wrapper,
        )

        saveProject(project)
    }

    fun onBringToFront() {
        val focused = project.screenHolder.findFocusedNodes()
        if (focused.isNotEmpty()) {
            recordOperation(
                project = project,
                userOperation =
                    UserOperation.BringNodesToFront(
                        nodes = focused.map { it.restoreInstance(sameId = true) },
                    ),
            )
            focused.forEach {
                it.bringToFront()
            }
            saveProject(project)
        }
    }

    fun onSendToBack() {
        val focused = project.screenHolder.findFocusedNodes()
        if (focused.isNotEmpty()) {
            recordOperation(
                project = project,
                userOperation =
                    UserOperation.SendNodesToBack(
                        nodes = focused.map { it.restoreInstance(sameId = true) },
                    ),
            )

            focused.forEach {
                it.sendToBack()
            }
            saveProject(project)
        }
    }

    fun onParamsUpdated(
        node: ComposeNode,
        trait: ComposeTrait,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ParameterUpdated(
                    node = node.restoreInstance(sameId = true),
                    params = trait,
                ),
        )

        node.trait.value = trait
        saveProject(project)
    }

    /**
     * @param lazyListSource is the data source that is used to update the params. For example,
     * data source of the ApiDefinition or the AppState to generate the dynamic children.
     */
    fun onParamsUpdatedWithLazyListSource(
        node: ComposeNode,
        trait: ComposeTrait,
        lazyListSource: LazyListChildParams?,
    ): EventResult {
        val result = EventResult()
        if (lazyListSource == null) {
            onParamsUpdated(node, trait)
            return result
        } else {
            val sourceId = lazyListSource.getSourceId()
            if (sourceId != null && node.isAnySiblingDependentSource(sourceId)) {
                viewModelScope.launch {
                    result.errorMessages.add(getString(Res.string.dynamic_items_is_used_only_once_warning))
                }
                return result
            } else {
                recordOperation(
                    project = project,
                    userOperation =
                        UserOperation.ParameterUpdated(
                            node = node.restoreInstance(sameId = true),
                            params = trait,
                        ),
                )
                node.setSourceForLazyListChild(lazyListSource)
                node.trait.value = trait
                saveProject(project)
            }
            return result
        }
    }

    fun onDynamicItemsUpdated(
        node: ComposeNode,
        assignableProperty: AssignableProperty?,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.DynamicItemsUpdated(
                    assignableProperty = assignableProperty,
                ),
        )

        node.dynamicItems.value = assignableProperty

        saveProject(project)
    }

    fun onLazyListChildParamsUpdated(
        node: ComposeNode,
        lazyListChildParams: LazyListChildParams,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.LazyListChildParamsUpdated(
                    lazyListChildParams = lazyListChildParams,
                ),
        )

        node.lazyListChildParams.value = lazyListChildParams

        saveProject(project)
    }

    fun onVisibilityParamsUpdated(
        node: ComposeNode,
        visibilityParams: VisibilityParams,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.VisibilityParamsUpdated(
                    visibilityParams = visibilityParams,
                ),
        )

        node.visibilityParams.value = visibilityParams

        saveProject(project)
    }

    fun onComposeNodeLabelUpdated(
        node: ComposeNode,
        label: String,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ComposableLabelUpdated(
                    node = node,
                    label = label,
                ),
        )
        val currentEditable = project.screenHolder.currentEditable()
        val newLabel = currentEditable.createUniqueLabel(project, node, label)
        node.label.value = newLabel

        saveProject(project)
    }

    fun onWrapWithContainerComposable(
        targetComposable: ComposeNode,
        wrapContainer: ComposeTrait,
    ) {
        val parent = targetComposable.parentNode ?: return
        recordOperation(
            project = project,
            userOperation =
                UserOperation.WrapComposable(
                    node = targetComposable,
                    wrapContainer = wrapContainer,
                ),
        )

        val wrapComposable =
            ComposeNode(
                trait = mutableStateOf(wrapContainer),
                modifierList =
                    mutableStateListEqualsOverrideOf(
                        ModifierWrapper.Padding(8.dp),
                    ),
            )
        wrapComposable.addChild(targetComposable)
        var insertIndex = parent.children.lastIndex
        parent.children.forEachIndexed { i, child ->
            if (child.id == targetComposable.id) {
                insertIndex = i
            }
        }
        parent.insertChildAt(insertIndex, wrapComposable)
        targetComposable.removeFromParent()

        saveProject(project)
    }

    fun onAddScreenFromTemplate(
        name: String,
        screenTemplatePair: ScreenTemplatePair,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ScreenAddedFromTemplate(
                    name = name,
                    screenTemplatePair = screenTemplatePair,
                ),
        )
        val newScreen = ScreenTemplates.createNewScreen(screenTemplatePair)
        val addedScreen = project.screenHolder.addScreen(name, newScreen)
        project.screenHolder.selectScreen(addedScreen)
        saveProject(project)
    }

    fun onAddScreen(screen: Screen) {
        // Track screen creation
        try {
            AnalyticsTracker.trackScreenCreated(
                screenType = screen::class.simpleName ?: "Unknown",
                creationMethod = "manual",
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        recordOperation(
            project = project,
            userOperation =
                UserOperation.ScreenAdded(
                    screen = screen,
                ),
        )
        val addedScreen =
            project.screenHolder.addScreen(
                screen.label.value,
                screen.copy(id = Uuid.random().toString()),
            )
        project.screenHolder.selectScreen(addedScreen)
        saveProject(project)
    }

    fun onScreenUpdated(screen: Screen) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ScreenUpdated(
                    screen = screen,
                ),
        )

        project.screenHolder.updateScreen(screen)

        saveProject(project)
    }

    fun onSelectScreen(screen: Screen) {
        // Track screen switching
        try {
            val fromScreen =
                project.screenHolder
                    .currentScreen()
                    .label.value
            AnalyticsTracker.trackScreenSwitched(
                fromScreen = fromScreen,
                toScreen = screen.label.value,
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        project.screenHolder.selectScreen(screen)
        saveProject(project)
    }

    fun onDeleteScreen(screen: Screen) {
        // Track screen deletion
        try {
            AnalyticsTracker.trackScreenDeleted(
                screenType = screen.label.value,
                hadComponents = screen.getAllComposeNodes().isNotEmpty(),
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        recordOperation(
            project = project,
            userOperation =
                UserOperation.ScreenDeleted(
                    screen = screen,
                ),
        )

        project.screenHolder.deleteScreen(screen)
        saveProject(project)
    }

    fun onCopyScreen(screen: Screen) {
        // Track screen copy
        try {
            AnalyticsTracker.trackScreenCreated(
                screenType = screen.label.value,
                creationMethod = "copy",
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        // Generate a unique name for the copied screen
        val copiedName =
            generateUniqueName(
                initial = screen.name,
                existing =
                    project.screenHolder.screens
                        .map { it.name }
                        .toSet(),
            )

        val copiedScreen =
            screen.copy(
                id = Uuid.random().toString(),
                name = copiedName,
            )
        copiedScreen.label.value = copiedName

        recordOperation(
            project = project,
            userOperation =
                UserOperation.ScreenAdded(
                    screen = copiedScreen,
                ),
        )

        val addedScreen =
            project.screenHolder.addScreen(
                copiedName,
                copiedScreen,
            )
        project.screenHolder.selectScreen(addedScreen)
        saveProject(project)
    }

    fun onScreensSwapped(
        from: Int,
        to: Int,
    ) {
        project.screenHolder.screens.swap(from, to)
        saveProject(project)
    }

    fun onPendingHeightModifierCommitted(node: ComposeNode) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.PendingModifierCommitted(
                    node = node,
                    pending = node.pendingModifier.value,
                ),
        )
        node.commitPendingHeightModifier()
        saveProject(project)
    }

    fun onPendingWidthModifierCommitted(node: ComposeNode) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.PendingModifierCommitted(
                    node = node,
                    pending = node.pendingModifier.value,
                ),
        )
        node.commitPendingWidthModifier()
        saveProject(project)
    }

    fun onActionsMapUpdated(
        node: ComposeNode,
        actionsMap: MutableMap<ActionType, MutableList<ActionNode>>,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ActionsMapUpdated(
                    node = node,
                    actionsMap = actionsMap,
                ),
        )
        node.actionHandler.actionsMap = actionsMap

        saveProject(project)
    }

    fun onCreateComponent(componentName: String) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.CreateComponent(
                    componentName = componentName,
                ),
        )
        val newComponentName =
            generateUniqueName(
                componentName,
                project.getAllCanvasEditable().map { it.name.asClassName() }.toSet(),
            )
        val component =
            Component(
                name = newComponentName,
                componentRoot =
                    mutableStateOf(
                        ComposeNode(
                            modifierList =
                                mutableStateListEqualsOverrideOf(
                                    ModifierWrapper.Width(400.dp),
                                    ModifierWrapper.Height(300.dp),
                                ),
                            trait = mutableStateOf(ColumnTrait()),
                        ),
                    ),
            )

        project.componentHolder.components.add(component)
        project.screenHolder.editedComponent.value = component

        saveProject(project)
    }

    fun onConvertToComponent(
        componentName: String,
        node: ComposeNode,
    ) {
        recordOperation(
            project = project,
            userOperation =
                UserOperation.ConvertToComponent(
                    componentName = componentName,
                    node = node,
                ),
        )
        val newComponentName =
            generateUniqueName(
                componentName,
                project.getAllCanvasEditable().map { it.name.asClassName() }.toSet(),
            )
        // Clear the focused state before converting
        val component =
            Component(
                name = newComponentName,
                componentRoot = mutableStateOf(node.restoreInstance(sameId = false)),
            )

        // Add new states to the Component
        component.getRootNode().allChildren().forEach {
            it.trait.value.onAttachStateToNode(
                project = project,
                stateHolder = component,
                node = it,
            )
        }

        project.replaceNode(node.id, node.createComponentWrapperNode(component.id))

        project.componentHolder.components.add(component)
        project.screenHolder.editedComponent.value = component

        saveProject(project)
    }

    fun onDoubleTap(composeNode: ComposeNode): EventResult {
        composeNode.componentId?.let {
            project.screenHolder.editedComponent.value = project.findComponentOrThrow(it)
        }
        return EventResult()
    }

    fun onEditComponent(component: Component) {
        component.clearIsFocusedRecursively()
        component.componentRoot.value.setFocus()
        project.screenHolder.editedComponent.value = component
        project.screenHolder.clearIsFocused()
        saveProject(project)
    }

    fun onRemoveComponent(component: Component): EventResult {
        val result = EventResult()
        if (project.getAllComposeNodes().any {
                it.componentId == component.id
            }
        ) {
            result.errorMessages.add("You can't delete a component used in the project")
            return result
        }
        recordOperation(
            project,
            userOperation = UserOperation.RemoveComponent(component),
        )
        if (project.screenHolder.editedComponent.value
                ?.id == component.id
        ) {
            project.screenHolder.editedComponent.value = null
        }
        project.componentHolder.components.remove(component)
        saveProject(project)
        return result
    }

    fun onAddParameterToCanvasEditable(
        canvasEditable: CanvasEditable,
        parameter: ParameterWrapper<*>,
    ) {
        recordOperation(
            project,
            userOperation =
                UserOperation.AddParameterToCanvasEditable(
                    canvasEditable = canvasEditable,
                    parameter = parameter,
                ),
        )
        val newName =
            generateUniqueName(
                initial = parameter.variableName,
                canvasEditable.parameters.map { it.variableName }.toSet(),
            )
        canvasEditable.parameters.add(parameter.copy(newName = newName))
        saveProject(project)
    }

    fun onUpdateParameterInCanvasEditable(
        canvasEditable: CanvasEditable,
        parameter: ParameterWrapper<*>,
    ) {
        recordOperation(
            project,
            userOperation =
                UserOperation.UpdateParameterInCanvasEditable(
                    canvasEditable = canvasEditable,
                    parameter = parameter,
                ),
        )
        val newName =
            generateUniqueName(
                initial = parameter.variableName,
                canvasEditable.parameters
                    .filterNot { it.id == parameter.id }
                    .map { it.variableName }
                    .toSet(),
            )
        val index = canvasEditable.parameters.indexOfFirst { it.id == parameter.id }
        canvasEditable.parameters[index] = parameter.copy(newName = newName)
        saveProject(project)
    }

    fun onRemoveParameterFromCanvasEditable(
        canvasEditable: CanvasEditable,
        parameter: ParameterWrapper<*>,
    ) {
        recordOperation(
            project,
            userOperation =
                UserOperation.RemoveParameterFromCanvasEditable(
                    canvasEditable = canvasEditable,
                    parameter = parameter,
                ),
        )
        canvasEditable.parameters.remove(parameter)
        saveProject(project)
    }

    fun onPopEditedComponent() {
        project.screenHolder.editedComponent.value = null
        saveProject(project)
    }

    /**
     * Update the API.
     *
     * Adding a parameter to API may be called from Call API action editor, which is used from
     * the UI builder. Thus, having this method in this ViewModel.
     */
    fun onApiUpdated(updatedApi: ApiDefinition) {
        val apiIndex = project.apiHolder.apiDefinitions.indexOfFirst { it.id == updatedApi.id }
        if (apiIndex != -1) {
            recordOperation(project, UserOperation.UpdateApi(updatedApi))
            project.apiHolder.apiDefinitions[apiIndex] = updatedApi

            saveProject(project)
        }
    }

    fun onSetPendingFocus(destinationContext: DestinationContext.UiBuilderScreen) {
        val screen =
            project.screenHolder.screens.firstOrNull { it.id == destinationContext.canvasEditableId }
        screen?.let {
            project.screenHolder.selectScreen(it)
        }
        val component =
            project.componentHolder.components.firstOrNull { it.id == destinationContext.canvasEditableId }
        component?.let {
            project.screenHolder.editedComponent.value = component
        }

        val composeNode =
            project.getAllComposeNodes().firstOrNull { it.id == destinationContext.composeNodeId }
        composeNode?.let {
            val containingEditable = project.findCanvasEditableHavingNodeOrNull(composeNode)
            containingEditable?.clearIsFocusedRecursively()
            composeNode.setFocus()
        }

        project.screenHolder.pendingDestinationContext = null
        saveProject(project)
    }

    fun onResetPendingInspectorTab() {
        project.screenHolder.pendingDestinationContext = null
        saveProject(project)
    }

    private fun saveProject(project: Project) {
        // Track project save
        try {
            AnalyticsTracker.trackProjectSaved(
                saveTrigger = "user_action",
                autoSave = false,
            )
        } catch (_: Exception) {
            // Analytics is optional, don't fail on errors
        }

        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }

    private fun recordOperation(
        project: Project,
        userOperation: UserOperation,
    ) {
        viewModelScope.launch {
            OperationHistory.record(
                project = project,
                userOperation = userOperation,
            )
        }
    }
}
