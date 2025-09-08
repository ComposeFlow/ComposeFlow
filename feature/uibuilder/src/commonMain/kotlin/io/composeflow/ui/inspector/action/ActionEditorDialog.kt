package io.composeflow.ui.inspector.action

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_action
import io.composeflow.add_conditional_action
import io.composeflow.cancel
import io.composeflow.confirm
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Placeholder
import io.composeflow.define_action
import io.composeflow.edit
import io.composeflow.invalid_action_exists
import io.composeflow.model.action.Action
import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionNodeId
import io.composeflow.model.action.ActionType
import io.composeflow.model.action.FocusableActionNode
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BooleanProperty
import io.composeflow.not_defined
import io.composeflow.remove
import io.composeflow.ui.Tooltip
import io.composeflow.ui.dropdown.PlatformCursorDropdownMenu
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.propertyeditor.AssignableBooleanPropertyEditor
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

private val dialogWidth = 1540.dp
private val dialogHeight = 1000.dp
private val basicSpaceSize = 16.dp
private val dashPathEffect =
    PathEffect.dashPathEffect(
        floatArrayOf(
            10f,
            10f,
        ),
        0f,
    )

@Composable
fun ActionEditorDialog(
    project: Project,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionCustomizablePopup(
        onDismissRequest = onCloseDialog,
        modifier = modifier,
    ) {
        Surface {
            ActionEditorContent(
                project = project,
                composeNode = composeNode,
                composeNodeCallbacks = composeNodeCallbacks,
                onCloseDialog = onCloseDialog,
                modifier = Modifier.size(dialogWidth, dialogHeight),
            )
        }
    }
}

@Composable
private fun ActionEditorContent(
    project: Project,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        viewModel(
            modelClass = ActionEditorViewModel::class,
            keys = listOf(composeNode.id, composeNode.actionsMap),
        ) { ActionEditorViewModel() }

    LaunchedEffect(composeNode.actionsMap) {
        viewModel.initializeMap(
            actionTypes = composeNode.trait.value.actionTypes(),
            initialActionsMap = composeNode.actionsMap,
        )
    }

    val currentActionType = viewModel.currentActionType.value
    val actionsMap = viewModel.actionsMap
    val focusedActionNode =
        viewModel.findFocusableActionNodeOrNull(viewModel.focusedActionNodeId.value)
    val isConfirmEnabled =
        viewModel.actionsMap.entries.all { entry ->
            entry.value.all { it.isValid() }
        }
    Column(
        modifier = modifier,
    ) {
        Header(
            actionsMap = actionsMap,
            actionTypes =
                actionsMap.entries
                    .sortedBy { it.key.priority }
                    .map { it.key },
            composeNode = composeNode,
            currentActionType = currentActionType,
            isConfirmEnabled = isConfirmEnabled,
            onActionTypeSelected = viewModel::onActionTypeSelected,
            onDismissDialog = onCloseDialog,
            composeNodeCallbacks = composeNodeCallbacks,
        )
        Body(
            project = project,
            composeNode = composeNode,
            composeNodeCallbacks = composeNodeCallbacks,
            actionNodes =
                actionsMap.entries.firstOrNull { it.key == currentActionType }?.value
                    ?: emptyList(),
            currentActionType = currentActionType,
            focusedActionNode = focusedActionNode,
            onActionNodesUpdated = viewModel::onActionNodesUpdated,
            onFocusableActionNodeClicked = viewModel::onFocusableActionNodeClicked,
            onRemoveActionNodeWithActionId = viewModel::onRemoveActionWithActionNodeId,
            onUpdateActionWithActionNodeId = viewModel::onUpdateActionWithActionNodeId,
        )
    }
}

@Composable
private fun Header(
    actionsMap: MutableMap<ActionType, MutableList<ActionNode>>,
    actionTypes: List<ActionType>,
    currentActionType: ActionType,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
    isConfirmEnabled: Boolean,
    onActionTypeSelected: (ActionType) -> Unit,
    onDismissDialog: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            actionTypes.forEach { actionType ->
                FilterChip(
                    selected = actionType == currentActionType,
                    label = {
                        Text(actionType.name)
                    },
                    onClick = {
                        onActionTypeSelected(actionType)
                    },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier =
                Modifier
                    .padding(end = 16.dp)
                    .align(Alignment.CenterEnd),
        ) {
            TextButton(
                onClick = {
                    onDismissDialog()
                },
                modifier = Modifier.padding(end = 16.dp),
            ) {
                Text(stringResource(Res.string.cancel))
            }

            if (isConfirmEnabled) {
                OutlinedButton(
                    onClick = {
                        composeNodeCallbacks.onActionsMapUpdated(
                            composeNode,
                            actionsMap,
                        )
                        onDismissDialog()
                    },
                    enabled = true,
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            } else {
                Tooltip(stringResource(Res.string.invalid_action_exists)) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun Body(
    project: Project,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
    actionNodes: List<ActionNode>,
    currentActionType: ActionType,
    focusedActionNode: FocusableActionNode?,
    onActionNodesUpdated: (ActionType, List<ActionNode>) -> Unit,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
) {
    Row {
        LazyRow(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f),
        ) {
            item {
                LazyColumn {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            BasicSpace()
                            if (actionNodes.isEmpty()) {
                                EmptyActionContainer(
                                    currentActionType = currentActionType,
                                    onActionNodesUpdated = onActionNodesUpdated,
                                )
                            } else {
                                actionNodes.forEachIndexed { index, actionNode ->
                                    ActionNodeContainer(
                                        project = project,
                                        composeNode = composeNode,
                                        index = index,
                                        onActionNodesUpdated = onActionNodesUpdated,
                                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                                        currentActionType = currentActionType,
                                        actionNodes = actionNodes,
                                        actionNode = actionNode,
                                        focusedActionNode = focusedActionNode,
                                    )
                                }
                            }
                            BasicSpace()
                        }
                    }
                }
            }
        }
        ActionDetailsPane(
            project = project,
            composeNode = composeNode,
            composeNodeCallbacks = composeNodeCallbacks,
            focusedActionNode = focusedActionNode,
            onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
            onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
            modifier =
                Modifier
                    .width(340.dp)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer),
        )
    }
}

@Composable
private fun ActionNodeContainer(
    project: Project,
    composeNode: ComposeNode,
    index: Int,
    onActionNodesUpdated: (ActionType, MutableList<ActionNode>) -> Unit,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    currentActionType: ActionType,
    actionNodes: List<ActionNode>,
    actionNode: ActionNode,
    focusedActionNode: FocusableActionNode?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    with(density) {
        Box(modifier = modifier.fillMaxWidth()) {
            var actionNodeTopOffset by remember { mutableStateOf(Offset.Zero) }
            var actionNodeBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var secondAddButtonTopOffset by remember { mutableStateOf(Offset.Zero) }
            var secondAddButtonBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var bottomOfContainerOffset by remember { mutableStateOf(Offset.Zero) }

            val lineColor = MaterialTheme.colorScheme.outlineVariant
            Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                drawLine(
                    color = lineColor,
                    start = actionNodeTopOffset - Offset(x = 0f, y = basicSpaceSize.toPx()),
                    end = actionNodeTopOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )

                if (secondAddButtonTopOffset != Offset.Zero) {
                    drawLine(
                        color = lineColor,
                        start = actionNodeBottomOffset,
                        end = secondAddButtonTopOffset,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashPathEffect,
                    )
                }

                if (actionNodes.lastIndex != index && secondAddButtonBottomOffset != Offset.Zero) {
                    drawLine(
                        color = lineColor,
                        start = secondAddButtonBottomOffset,
                        end = bottomOfContainerOffset,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashPathEffect,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier.onGloballyPositioned {
                        bottomOfContainerOffset =
                            it.boundsInParent().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                    },
            ) {
                if (index == 0) {
                    AddNewActionNodeButton(
                        onAddActionNode = { newNode ->
                            onActionNodesUpdated(
                                currentActionType,
                                actionNodes.toMutableList().apply {
                                    add(0, newNode)
                                },
                            )
                        },
                    )
                }
                BasicSpace()
                SingleActionNode(
                    project = project,
                    composeNode = composeNode,
                    actionNode = actionNode,
                    currentActionType = currentActionType,
                    focusedActionNode = focusedActionNode,
                    onNodeUpdated = { newNode ->
                        val newActionNodes =
                            actionNodes.toMutableList().apply {
                                set(index, newNode)
                            }
                        onActionNodesUpdated(currentActionType, newActionNodes)
                    },
                    onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                    onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                    onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                    modifier =
                        Modifier.onGloballyPositioned {
                            actionNodeTopOffset =
                                it.boundsInParent().topCenter + Offset(x = 4.dp.toPx(), 0f)
                            actionNodeBottomOffset =
                                it.boundsInParent().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                        },
                )
                BasicSpace()
                AddNewActionNodeButton(
                    onAddActionNode = { newNode ->
                        onActionNodesUpdated(
                            currentActionType,
                            actionNodes.toMutableList().apply {
                                add(index + 1, newNode)
                            },
                        )
                    },
                    modifier =
                        Modifier.onGloballyPositioned {
                            secondAddButtonTopOffset =
                                it.boundsInParent().topCenter + Offset(x = 4.dp.toPx(), 0f)
                            secondAddButtonBottomOffset =
                                it.boundsInParent().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                        },
                )
            }
        }
    }
}

@Composable
private fun SingleActionNode(
    project: Project,
    composeNode: ComposeNode,
    actionNode: ActionNode,
    currentActionType: ActionType,
    focusedActionNode: FocusableActionNode?,
    onNodeUpdated: (ActionNode) -> Unit,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (actionNode) {
            is ActionNode.Simple -> {
                SimpleActionNode(
                    project = project,
                    actionNode = actionNode,
                    focusedActionNode = focusedActionNode,
                    onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                    onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                    onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                )
            }

            is ActionNode.Conditional -> {
                ConditionalActionNode(
                    project = project,
                    composeNode = composeNode,
                    conditionalNode = actionNode,
                    currentActionType = currentActionType,
                    focusedActionNode = focusedActionNode,
                    onIfConditionChanged = {
                        val newNode = actionNode.copy(ifCondition = it)
                        onNodeUpdated(newNode)
                    },
                    onTrueNodesUpdated = {
                        val newNode = actionNode.copy(trueNodes = it)
                        onNodeUpdated(newNode)
                    },
                    onFalseNodesUpdated = {
                        val newNode = actionNode.copy(falseNodes = it)
                        onNodeUpdated(newNode)
                    },
                    onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                    onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                    onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                )
            }

            is ActionNode.Forked -> {
                ForkedActionNode(
                    project = project,
                    composeNode = composeNode,
                    forkedActionNode = actionNode,
                    currentActionType = currentActionType,
                    focusedActionNode = focusedActionNode,
                    onTrueNodesUpdated = {
                        val newNode = actionNode.copy(trueNodes = it)
                        onNodeUpdated(newNode)
                    },
                    onFalseNodesUpdated = {
                        val newNode = actionNode.copy(falseNodes = it)
                        onNodeUpdated(newNode)
                    },
                    onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                    onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                    onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                )
            }
        }
    }
}

@Composable
private fun SimpleActionNode(
    project: Project,
    actionNode: FocusableActionNode,
    focusedActionNode: FocusableActionNode?,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFocused = focusedActionNode?.id == actionNode.id
    val borderColor =
        if (actionNode.getFocusedAction() == null) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            if (isFocused) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        }
    var actionNodeMenuOpened by remember { mutableStateOf(false) }
    var selectActionDialogOpen by remember { mutableStateOf(false) }
    Box(
        modifier
            .alpha(if (isFocused) 1f else 0.5f)
            .background(
                color =
                    if (actionNode.getFocusedAction() != null) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    },
                shape = RoundedCornerShape(8.dp),
            ).border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp),
            ).height(72.dp)
            .width(180.dp)
            .hoverIconClickable()
            .clickable {
                onFocusableActionNodeClicked(actionNode)
            },
    ) {
        ComposeFlowIconButton(
            onClick = {
                onFocusableActionNodeClicked(actionNode)
                actionNodeMenuOpened = true
            },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp),
        ) {
            ComposeFlowIcon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
            )
        }
        Column {
            if (actionNode.getFocusedAction() == null) {
                Column {
                    Text(
                        stringResource(Res.string.not_defined),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                    )
                    Text(
                        stringResource(Res.string.define_action),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else {
                Column(modifier = Modifier.padding(8.dp)) {
                    actionNode.getFocusedAction()?.SimplifiedContent(project)
                }
            }
        }
    }

    if (actionNodeMenuOpened) {
        PlatformCursorDropdownMenu(
            expanded = true,
            onDismissRequest = {
                actionNodeMenuOpened = false
            },
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = stringResource(Res.string.edit),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }, onClick = {
                        selectActionDialogOpen = true
                        actionNodeMenuOpened = false
                    })
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = stringResource(Res.string.remove),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }, onClick = {
                        onRemoveActionNodeWithActionId(actionNode.id)
                        actionNodeMenuOpened = false
                    })
                }
            }
        }
    }

    if (selectActionDialogOpen) {
        val closeDialog = {
            selectActionDialogOpen = false
        }
        PositionCustomizablePopup(
            expanded = true,
            onDismissRequest = {
                closeDialog()
            },
            onKeyEvent = {
                if (it.key == Key.Escape) {
                    closeDialog()
                    true
                } else {
                    false
                }
            },
        ) {
            AddActionDialogContent(
                project = project,
                actionInEdit = focusedActionNode?.getFocusedAction(),
                onActionSelected = {
                    focusedActionNode?.let { focusedNode ->
                        onUpdateActionWithActionNodeId(focusedNode.id, it)
                    }
                },
                onCloseClick = {
                    closeDialog()
                },
            )
        }
    }
}

@Composable
private fun ConditionalActionNode(
    project: Project,
    composeNode: ComposeNode,
    conditionalNode: ActionNode.Conditional,
    currentActionType: ActionType,
    focusedActionNode: FocusableActionNode?,
    onIfConditionChanged: (AssignableProperty) -> Unit,
    onTrueNodesUpdated: (MutableList<ActionNode>) -> Unit,
    onFalseNodesUpdated: (MutableList<ActionNode>) -> Unit,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    var overlayVisible by remember { mutableStateOf(false) }
    val overlayModifier =
        if (overlayVisible) {
            Modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
        } else {
            Modifier
        }
    var menuOpened by remember { mutableStateOf(false) }
    if (menuOpened) {
        PlatformCursorDropdownMenu(
            expanded = true,
            onDismissRequest = {
                menuOpened = false
            },
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = stringResource(Res.string.remove),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }, onClick = {
                        onRemoveActionNodeWithActionId(conditionalNode.id)
                        menuOpened = false
                    })
                }
            }
        }
    }
    Box(
        modifier =
            Modifier
                .onPointerEvent(PointerEventType.Enter) {
                    overlayVisible = true
                }.onPointerEvent(PointerEventType.Exit) {
                    overlayVisible = false
                }.then(overlayModifier),
    ) {
        if (overlayVisible) {
            ComposeFlowIconButton(
                onClick = {
                    menuOpened = true
                },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                )
            }
        }
        Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            val density = LocalDensity.current
            var windowOffset by remember { mutableStateOf(Offset.Zero) }
            var bottomOffset by remember { mutableStateOf(Offset.Zero) }
            var ifBlockBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueLabelTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueLabelBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseLabelTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseLabelBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueBranchStartNodeTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseBranchStartNodeTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueBranchEndNodeBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseBranchEndNodeBottomOffset by remember { mutableStateOf(Offset.Zero) }

            val trueLabelTopOffset by remember(
                windowOffset,
                globalTrueLabelTopOffset,
            ) { derivedStateOf { globalTrueLabelTopOffset - windowOffset } }
            val trueLabelBottomOffset by remember(
                windowOffset,
                globalTrueLabelBottomOffset,
            ) { derivedStateOf { globalTrueLabelBottomOffset - windowOffset } }
            val falseLabelTopOffset by remember(
                windowOffset,
                globalFalseLabelTopOffset,
            ) { derivedStateOf { globalFalseLabelTopOffset - windowOffset } }
            val falseLabelBottomOffset by remember(
                windowOffset,
                globalFalseLabelBottomOffset,
            ) { derivedStateOf { globalFalseLabelBottomOffset - windowOffset } }
            val trueBranchEndBottomOffset by remember(
                windowOffset,
                globalTrueBranchEndNodeBottomOffset,
            ) { derivedStateOf { globalTrueBranchEndNodeBottomOffset - windowOffset } }
            val falseBranchEndBottomOffset by remember(
                windowOffset,
                globalFalseBranchEndNodeBottomOffset,
            ) { derivedStateOf { globalFalseBranchEndNodeBottomOffset - windowOffset } }
            with(density) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        modifier.onGloballyPositioned {
                            windowOffset = it.boundsInWindow().topLeft
                            bottomOffset = it.boundsInParent().bottomCenter
                        },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .onGloballyPositioned {
                                    ifBlockBottomOffset =
                                        it.boundsInParent().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                }.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp),
                                ).padding(bottom = 4.dp),
                    ) {
                        Text(
                            text = "IF",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp),
                        )
                        AssignableBooleanPropertyEditor(
                            project = project,
                            node = composeNode,
                            initialProperty = conditionalNode.ifCondition,
                            onValidPropertyChanged = { property, _ ->
                                onIfConditionChanged(property)
                            },
                            onInitializeProperty = {
                                onIfConditionChanged(BooleanProperty.Empty)
                            },
                            modifier =
                                Modifier
                                    .width(240.dp)
                                    .padding(top = 12.dp),
                        )
                    }
                    BasicSpace()
                    Row(
                        modifier =
                            Modifier
                                .wrapContentWidth()
                                .widthIn(min = 300.dp),
                    ) {
                        BasicSpace()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier.onGloballyPositioned {
                                    globalTrueBranchEndNodeBottomOffset =
                                        it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                },
                        ) {
                            BasicSpace()
                            TrueLabel(
                                modifier =
                                    Modifier.onGloballyPositioned {
                                        globalTrueLabelTopOffset =
                                            it.boundsInRoot().topCenter + Offset(x = 4.dp.toPx(), 0f)
                                        globalTrueLabelBottomOffset =
                                            it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                    },
                            )
                            BasicSpace()
                            if (conditionalNode.trueNodes.isEmpty()) {
                                AddNewActionNodeButton(
                                    onAddActionNode = {
                                        onTrueNodesUpdated(listOf(it).toMutableList())
                                    },
                                    modifier =
                                        Modifier.onGloballyPositioned {
                                            globalTrueBranchStartNodeTopOffset =
                                                it.boundsInRoot().topCenter +
                                                Offset(
                                                    x = 4.dp.toPx(),
                                                    0f,
                                                )
                                        },
                                )
                            } else {
                                conditionalNode.trueNodes.forEachIndexed { index, actionNode ->
                                    ActionNodeContainer(
                                        project = project,
                                        composeNode = composeNode,
                                        index = index,
                                        onActionNodesUpdated = { _, actionNodes ->
                                            onTrueNodesUpdated(actionNodes)
                                        },
                                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                                        currentActionType = currentActionType,
                                        actionNodes = conditionalNode.trueNodes,
                                        actionNode = actionNode,
                                        focusedActionNode = focusedActionNode,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                globalTrueBranchStartNodeTopOffset =
                                                    it.boundsInRoot().topCenter +
                                                    Offset(
                                                        x = 4.dp.toPx(),
                                                        0f,
                                                    )
                                            },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(160.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier.onGloballyPositioned {
                                    globalFalseBranchEndNodeBottomOffset =
                                        it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                },
                        ) {
                            Spacer(Modifier.size(16.dp))
                            FalseLabel(
                                modifier =
                                    Modifier.onGloballyPositioned {
                                        globalFalseLabelTopOffset =
                                            it.boundsInRoot().topCenter + Offset(x = 4.dp.toPx(), 0f)
                                        globalFalseLabelBottomOffset =
                                            it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                    },
                            )
                            BasicSpace()
                            if (conditionalNode.falseNodes.isEmpty()) {
                                AddNewActionNodeButton(
                                    onAddActionNode = {
                                        onFalseNodesUpdated(listOf(it).toMutableList())
                                    },
                                    modifier =
                                        Modifier.onGloballyPositioned {
                                            globalFalseBranchStartNodeTopOffset =
                                                it.boundsInRoot().topCenter +
                                                Offset(
                                                    x = 4.dp.toPx(),
                                                    0f,
                                                )
                                        },
                                )
                            } else {
                                conditionalNode.falseNodes.forEachIndexed { index, actionNode ->
                                    ActionNodeContainer(
                                        project = project,
                                        composeNode = composeNode,
                                        index = index,
                                        onActionNodesUpdated = { _, actionNodes ->
                                            onFalseNodesUpdated(actionNodes)
                                        },
                                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                                        currentActionType = currentActionType,
                                        actionNodes = conditionalNode.falseNodes,
                                        actionNode = actionNode,
                                        focusedActionNode = focusedActionNode,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                globalFalseBranchStartNodeTopOffset =
                                                    it.boundsInRoot().topCenter +
                                                    Offset(
                                                        x = 4.dp.toPx(),
                                                        0f,
                                                    )
                                            },
                                    )
                                }
                            }
                        }
                        BasicSpace()
                    }
                    BasicSpace()
                }
            }

            val lineColor = MaterialTheme.colorScheme.outlineVariant

            Canvas(modifier = Modifier.fillMaxSize()) {
                val trueLabelPlusOffsetAtTopLeftCorner =
                    Offset(x = trueLabelTopOffset.x, y = trueLabelTopOffset.y - 16.dp.toPx())
                val falseLabelPlusOffsetAtTopRightCorner =
                    Offset(x = falseLabelTopOffset.x, y = falseLabelTopOffset.y - 16.dp.toPx())
                drawLine(
                    color = lineColor,
                    start = ifBlockBottomOffset,
                    end = ifBlockBottomOffset + Offset(x = 0f, y = 16.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelPlusOffsetAtTopLeftCorner,
                    end = falseLabelPlusOffsetAtTopRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelPlusOffsetAtTopLeftCorner,
                    end = trueLabelTopOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseLabelPlusOffsetAtTopRightCorner,
                    end = falseLabelTopOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelBottomOffset,
                    end = trueLabelBottomOffset + Offset(x = 0f, y = basicSpaceSize.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseLabelBottomOffset,
                    end = falseLabelBottomOffset + Offset(x = 0f, y = basicSpaceSize.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                val trueBranchPlusOffsetAtBottomLeftCorner =
                    Offset(x = trueBranchEndBottomOffset.x, y = bottomOffset.y)
                val falsePlusOffsetAtBottomRightCorner =
                    Offset(x = falseBranchEndBottomOffset.x, y = bottomOffset.y)
                drawLine(
                    color = lineColor,
                    start = trueBranchEndBottomOffset,
                    end = trueBranchPlusOffsetAtBottomLeftCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseBranchEndBottomOffset,
                    end = falsePlusOffsetAtBottomRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueBranchPlusOffsetAtBottomLeftCorner,
                    end = falsePlusOffsetAtBottomRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
            }
        }
    }
}

@Composable
private fun ForkedActionNode(
    project: Project,
    composeNode: ComposeNode,
    forkedActionNode: ActionNode.Forked,
    currentActionType: ActionType,
    focusedActionNode: FocusableActionNode?,
    onTrueNodesUpdated: (MutableList<ActionNode>) -> Unit,
    onFalseNodesUpdated: (MutableList<ActionNode>) -> Unit,
    onFocusableActionNodeClicked: (FocusableActionNode) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    var overlayVisible by remember { mutableStateOf(false) }
    val overlayModifier =
        if (overlayVisible) {
            Modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
        } else {
            Modifier
        }
    var menuOpened by remember { mutableStateOf(false) }
    if (menuOpened) {
        PlatformCursorDropdownMenu(
            expanded = true,
            onDismissRequest = {
                menuOpened = false
            },
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = stringResource(Res.string.remove),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }, onClick = {
                        onRemoveActionNodeWithActionId(forkedActionNode.id)
                        menuOpened = false
                    })
                }
            }
        }
    }
    Box(
        modifier =
            Modifier
                .onPointerEvent(PointerEventType.Enter) {
                    overlayVisible = true
                }.onPointerEvent(PointerEventType.Exit) {
                    overlayVisible = false
                }.then(overlayModifier),
    ) {
        if (overlayVisible) {
            ComposeFlowIconButton(
                onClick = {
                    menuOpened = true
                },
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                )
            }
        }
        Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            val density = LocalDensity.current
            var windowOffset by remember { mutableStateOf(Offset.Zero) }
            var bottomOffset by remember { mutableStateOf(Offset.Zero) }
            var forkedActionBlockBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueLabelTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueLabelBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseLabelTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseLabelBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueBranchStartNodeTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseBranchStartNodeTopOffset by remember { mutableStateOf(Offset.Zero) }
            var globalTrueBranchEndNodeBottomOffset by remember { mutableStateOf(Offset.Zero) }
            var globalFalseBranchEndNodeBottomOffset by remember { mutableStateOf(Offset.Zero) }

            val trueLabelTopOffset by remember(
                windowOffset,
                globalTrueLabelTopOffset,
            ) { derivedStateOf { globalTrueLabelTopOffset - windowOffset } }
            val trueLabelBottomOffset by remember(
                windowOffset,
                globalTrueLabelBottomOffset,
            ) { derivedStateOf { globalTrueLabelBottomOffset - windowOffset } }
            val falseLabelTopOffset by remember(
                windowOffset,
                globalFalseLabelTopOffset,
            ) { derivedStateOf { globalFalseLabelTopOffset - windowOffset } }
            val falseLabelBottomOffset by remember(
                windowOffset,
                globalFalseLabelBottomOffset,
            ) { derivedStateOf { globalFalseLabelBottomOffset - windowOffset } }
            val trueBranchEndBottomOffset by remember(
                windowOffset,
                globalTrueBranchEndNodeBottomOffset,
            ) { derivedStateOf { globalTrueBranchEndNodeBottomOffset - windowOffset } }
            val falseBranchEndBottomOffset by remember(
                windowOffset,
                globalFalseBranchEndNodeBottomOffset,
            ) { derivedStateOf { globalFalseBranchEndNodeBottomOffset - windowOffset } }
            with(density) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        modifier.onGloballyPositioned {
                            windowOffset = it.boundsInWindow().topLeft
                            bottomOffset = it.boundsInParent().bottomCenter
                        },
                ) {
                    SimpleActionNode(
                        project = project,
                        actionNode = forkedActionNode,
                        focusedActionNode,
                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                        modifier =
                            Modifier.onGloballyPositioned {
                                forkedActionBlockBottomOffset =
                                    it.boundsInParent().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                            },
                    )
                    BasicSpace()
                    Row(
                        modifier =
                            Modifier
                                .wrapContentWidth()
                                .widthIn(min = 300.dp),
                    ) {
                        BasicSpace()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier.onGloballyPositioned {
                                    globalTrueBranchEndNodeBottomOffset =
                                        it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                },
                        ) {
                            BasicSpace()
                            TrueLabel(
                                modifier =
                                    Modifier.onGloballyPositioned {
                                        globalTrueLabelTopOffset =
                                            it.boundsInRoot().topCenter + Offset(x = 4.dp.toPx(), 0f)
                                        globalTrueLabelBottomOffset =
                                            it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                    },
                            )
                            BasicSpace()
                            if (forkedActionNode.trueNodes.isEmpty()) {
                                AddNewActionNodeButton(
                                    onAddActionNode = {
                                        onTrueNodesUpdated(listOf(it).toMutableList())
                                    },
                                    modifier =
                                        Modifier.onGloballyPositioned {
                                            globalTrueBranchStartNodeTopOffset =
                                                it.boundsInRoot().topCenter +
                                                Offset(
                                                    x = 4.dp.toPx(),
                                                    0f,
                                                )
                                        },
                                )
                            } else {
                                forkedActionNode.trueNodes.forEachIndexed { index, actionNode ->
                                    ActionNodeContainer(
                                        project = project,
                                        composeNode = composeNode,
                                        index = index,
                                        onActionNodesUpdated = { _, actionNodes ->
                                            onTrueNodesUpdated(actionNodes)
                                        },
                                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                                        currentActionType = currentActionType,
                                        actionNodes = forkedActionNode.trueNodes,
                                        actionNode = actionNode,
                                        focusedActionNode = focusedActionNode,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                globalTrueBranchStartNodeTopOffset =
                                                    it.boundsInRoot().topCenter +
                                                    Offset(
                                                        x = 4.dp.toPx(),
                                                        0f,
                                                    )
                                            },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(160.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier.onGloballyPositioned {
                                    globalFalseBranchEndNodeBottomOffset =
                                        it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                },
                        ) {
                            Spacer(Modifier.size(16.dp))
                            FalseLabel(
                                modifier =
                                    Modifier.onGloballyPositioned {
                                        globalFalseLabelTopOffset =
                                            it.boundsInRoot().topCenter + Offset(x = 4.dp.toPx(), 0f)
                                        globalFalseLabelBottomOffset =
                                            it.boundsInRoot().bottomCenter + Offset(x = 4.dp.toPx(), 0f)
                                    },
                            )
                            BasicSpace()
                            if (forkedActionNode.falseNodes.isEmpty()) {
                                AddNewActionNodeButton(
                                    onAddActionNode = {
                                        onFalseNodesUpdated(listOf(it).toMutableList())
                                    },
                                    modifier =
                                        Modifier.onGloballyPositioned {
                                            globalFalseBranchStartNodeTopOffset =
                                                it.boundsInRoot().topCenter +
                                                Offset(
                                                    x = 4.dp.toPx(),
                                                    0f,
                                                )
                                        },
                                )
                            } else {
                                forkedActionNode.falseNodes.forEachIndexed { index, actionNode ->
                                    ActionNodeContainer(
                                        project = project,
                                        composeNode = composeNode,
                                        index = index,
                                        onActionNodesUpdated = { _, actionNodes ->
                                            onFalseNodesUpdated(actionNodes)
                                        },
                                        onFocusableActionNodeClicked = onFocusableActionNodeClicked,
                                        onRemoveActionNodeWithActionId = onRemoveActionNodeWithActionId,
                                        onUpdateActionWithActionNodeId = onUpdateActionWithActionNodeId,
                                        currentActionType = currentActionType,
                                        actionNodes = forkedActionNode.falseNodes,
                                        actionNode = actionNode,
                                        focusedActionNode = focusedActionNode,
                                        modifier =
                                            Modifier.onGloballyPositioned {
                                                globalFalseBranchStartNodeTopOffset =
                                                    it.boundsInRoot().topCenter +
                                                    Offset(
                                                        x = 4.dp.toPx(),
                                                        0f,
                                                    )
                                            },
                                    )
                                }
                            }
                        }
                        BasicSpace()
                    }
                    BasicSpace()
                }
            }

            val lineColor = MaterialTheme.colorScheme.outlineVariant

            Canvas(modifier = Modifier.fillMaxSize()) {
                val trueLabelPlusOffsetAtTopLeftCorner =
                    Offset(x = trueLabelTopOffset.x, y = trueLabelTopOffset.y - 16.dp.toPx())
                val falseLabelPlusOffsetAtTopRightCorner =
                    Offset(x = falseLabelTopOffset.x, y = falseLabelTopOffset.y - 16.dp.toPx())
                drawLine(
                    color = lineColor,
                    start = forkedActionBlockBottomOffset,
                    end = forkedActionBlockBottomOffset + Offset(x = 0f, y = 16.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelPlusOffsetAtTopLeftCorner,
                    end = falseLabelPlusOffsetAtTopRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelPlusOffsetAtTopLeftCorner,
                    end = trueLabelTopOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseLabelPlusOffsetAtTopRightCorner,
                    end = falseLabelTopOffset,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueLabelBottomOffset,
                    end = trueLabelBottomOffset + Offset(x = 0f, y = basicSpaceSize.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseLabelBottomOffset,
                    end = falseLabelBottomOffset + Offset(x = 0f, y = basicSpaceSize.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                val trueBranchPlusOffsetAtBottomLeftCorner =
                    Offset(x = trueBranchEndBottomOffset.x, y = bottomOffset.y)
                val falsePlusOffsetAtBottomRightCorner =
                    Offset(x = falseBranchEndBottomOffset.x, y = bottomOffset.y)
                drawLine(
                    color = lineColor,
                    start = trueBranchEndBottomOffset,
                    end = trueBranchPlusOffsetAtBottomLeftCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = falseBranchEndBottomOffset,
                    end = falsePlusOffsetAtBottomRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
                drawLine(
                    color = lineColor,
                    start = trueBranchPlusOffsetAtBottomLeftCorner,
                    end = falsePlusOffsetAtBottomRightCorner,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashPathEffect,
                )
            }
        }
    }
}

@Composable
private fun TrueLabel(
    modifier: Modifier = Modifier,
    label: String = "True",
) {
    Column(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp),
                ).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(4.dp),
                ),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(4.dp),
        )
    }
}

@Composable
private fun FalseLabel(
    modifier: Modifier = Modifier,
    label: String = "False",
) {
    Column(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                ).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp),
                ),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(4.dp),
        )
    }
}

@Composable
private fun AddNewActionNodeButton(
    onAddActionNode: (ActionNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var openMenu by remember { mutableStateOf(false) }
    ComposeFlowIconButton(
        onClick = {
            openMenu = true
        },
        modifier = modifier.padding(start = 8.dp),
    ) {
        ComposeFlowIcon(
            imageVector = Icons.Outlined.AddCircleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }

    if (openMenu) {
        PlatformCursorDropdownMenu(
            expanded = true,
            onDismissRequest = {
                openMenu = false
            },
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(Res.string.add_action),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }, onClick = {
                        onAddActionNode(ActionNode.Simple())
                        openMenu = false
                    })
                    DropdownMenuItem(text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(Res.string.add_conditional_action),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }, onClick = {
                        onAddActionNode(ActionNode.Conditional())
                        openMenu = false
                    })
                }
            }
        }
    }
}

@Composable
private fun EmptyActionContainer(
    currentActionType: ActionType,
    onActionNodesUpdated: (ActionType, List<ActionNode>) -> Unit,
) {
    Column {
        Icon(
            imageVector = ComposeFlowIcons.Placeholder,
            contentDescription = "Empty actions",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(140.dp),
        )

        TextButton(
            onClick = {
                onActionNodesUpdated(currentActionType, listOf(ActionNode.Simple()))
            },
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text("+ " + stringResource(Res.string.add_action))
        }

        TextButton(
            onClick = {
                onActionNodesUpdated(currentActionType, listOf(ActionNode.Conditional()))
            },
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text("+ " + stringResource(Res.string.add_conditional_action))
        }
    }
}

@Composable
private fun ActionDetailsPane(
    project: Project,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
    focusedActionNode: FocusableActionNode?,
    onUpdateActionWithActionNodeId: (ActionNodeId, Action) -> Unit,
    onRemoveActionNodeWithActionId: (ActionNodeId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (focusedActionNode != null) {
            ActionDetailsContainer(
                project = project,
                composeNode = composeNode,
                composeNodeCallbacks = composeNodeCallbacks,
                focusableActionNode = focusedActionNode,
                onEditAction = {
                    it?.let {
                        onUpdateActionWithActionNodeId(focusedActionNode.id, it)
                    }
                },
                onRemoveActionNode = {
                    it?.let {
                        onRemoveActionNodeWithActionId(it)
                    }
                },
            )
        }
    }
}

@Composable
private fun BasicSpace(modifier: Modifier = Modifier) {
    Spacer(modifier.size(basicSpaceSize))
}
