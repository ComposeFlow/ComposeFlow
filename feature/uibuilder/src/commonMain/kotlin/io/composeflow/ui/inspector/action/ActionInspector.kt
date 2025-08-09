package io.composeflow.ui.inspector.action

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.cancel
import io.composeflow.edit
import io.composeflow.edit_actions_with_editor
import io.composeflow.focus_single_composable_to_modify_actions
import io.composeflow.model.action.Action
import io.composeflow.model.action.ActionNode
import io.composeflow.model.action.ActionType
import io.composeflow.model.action.Auth
import io.composeflow.model.action.CallApi
import io.composeflow.model.action.DateOrTimePicker
import io.composeflow.model.action.FirestoreAction
import io.composeflow.model.action.Navigation
import io.composeflow.model.action.Share
import io.composeflow.model.action.ShowConfirmationDialog
import io.composeflow.model.action.ShowInformationDialog
import io.composeflow.model.action.ShowMessaging
import io.composeflow.model.action.ShowModalWithComponent
import io.composeflow.model.action.ShowNavigationDrawer
import io.composeflow.model.action.StateAction
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.project.findApiDefinitionOrNull
import io.composeflow.open_action_editor
import io.composeflow.remove
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionInspector(
    project: Project,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    val focusedNodes = project.screenHolder.findFocusedNodes()
    if (focusedNodes.isEmpty()) {
        Text(text = "No node selected")
    } else if (focusedNodes.size > 1) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.focus_single_composable_to_modify_actions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        val composeNode = focusedNodes[0]
        var dialogOpen by remember { mutableStateOf(false) }
        val actionInEdit by remember { mutableStateOf<Action?>(null) }
        var actionEditorDialogOpen by remember { mutableStateOf(false) }
        val actionTypes =
            composeNode.trait.value
                .actionTypes()
                .sortedBy { it.priority }
        var selectedActionType by remember(
            composeNode.id,
            actionTypes,
        ) { mutableStateOf(actionTypes.first()) }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        dialogOpen = true
                    },
                    enabled = actionTypes.isNotEmpty(),
                ) {
                    Text("+ Add action")
                }

                Text(
                    text = "for",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )

                var actionTypeDropDownExpanded by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            actionTypeDropDownExpanded = true
                        },
                ) {
                    Text(
                        text = selectedActionType.name,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier
                                .padding(end = 8.dp),
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    DropdownMenu(
                        expanded = actionTypeDropDownExpanded,
                        onDismissRequest = { actionTypeDropDownExpanded = false },
                    ) {
                        actionTypes.forEach { action ->
                            DropdownMenuItem(
                                onClick = {
                                    actionTypeDropDownExpanded = false
                                    selectedActionType = action
                                },
                                text = {
                                    Text(
                                        action.name,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    Tooltip(stringResource(Res.string.open_action_editor)) {
                        TextButton(onClick = {
                            actionEditorDialogOpen = true
                        }) {
                            Text("Action Editor")
                        }
                    }
                }
            }
            ActionTriggersContent(
                project = project,
                composeNode = composeNode,
                composeNodeCallbacks = composeNodeCallbacks,
                actionType = selectedActionType,
            )
        }

        val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (dialogOpen) {
            onAnyDialogIsShown()
            val closeDialog = {
                dialogOpen = false
                onAllDialogsClosed()
            }
            PositionCustomizablePopup(
                expanded = dialogOpen,
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
                    actionInEdit = actionInEdit,
                    onActionSelected = {
                        val newActionsMap =
                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                putAll(composeNode.actionsMap)
                            }
//                    it.onActionAdded(project)
                        newActionsMap[selectedActionType]?.add(it.asActionNode())
                        composeNodeCallbacks.onActionsMapUpdated(
                            composeNode,
                            newActionsMap,
                        )
                    },
                    onCloseClick = {
                        closeDialog()
                    },
                )
            }
        }

        if (actionEditorDialogOpen) {
            onAnyDialogIsShown()
            val closeDialog = {
                actionEditorDialogOpen = false
                onAllDialogsClosed()
            }
            ActionEditorDialog(
                project = project,
                composeNode = composeNode,
                composeNodeCallbacks = composeNodeCallbacks,
                onCloseDialog = closeDialog,
            )
        }
    }
}

@Composable
private fun ActionTriggersContent(
    project: Project,
    actionType: ActionType,
    composeNode: ComposeNode,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    val actionNodes = composeNode.actionsMap[actionType] ?: mutableStateListOf()
    var dialogOpen by remember { mutableStateOf(false) }
    var actionNodeInEdit by remember { mutableStateOf<ActionNode.Simple?>(null) }
    var actionNodeIndex by remember { mutableStateOf<Int?>(null) }
    Column {
        if (actionNodes.any { it !is ActionNode.Simple }) {
            Column {
                Text(
                    "${composeNode.allActions().size} actions",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(Res.string.edit_actions_with_editor),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            Column {
                actionNodes.forEachIndexed { index, actionNode ->
                    val action = (actionNode as ActionNode.Simple).getFocusedAction()
                    Column(
                        modifier =
                            Modifier
                                .hoverOverlay()
                                .padding(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                action?.name ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.weight(1f))
                            val editContentDescription = stringResource(Res.string.edit)
                            Tooltip(editContentDescription) {
                                ComposeFlowIconButton(onClick = {
                                    dialogOpen = true
                                    actionNodeInEdit = actionNode
                                    actionNodeIndex = index
                                }) {
                                    ComposeFlowIcon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = editContentDescription,
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                            val removeDescription = stringResource(Res.string.remove)
                            Tooltip(removeDescription) {
                                ComposeFlowIconButton(onClick = {
                                    val newActionsMap =
                                        mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                            putAll(composeNode.actionsMap)
                                        }
                                    newActionsMap[actionType]?.removeAt(index)
                                    composeNodeCallbacks.onActionsMapUpdated(
                                        composeNode,
                                        newActionsMap,
                                    )
                                }) {
                                    ComposeFlowIcon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = removeDescription,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }

                        when (action) {
                            Navigation.NavigateBack -> {}
                            is Navigation.NavigateTo -> {
                                val screen = project.screenHolder.findScreen(action.screenId)
                                Text(
                                    screen?.name ?: "",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                screen?.let {
                                    NavigateToActionDetail(
                                        project = project,
                                        composeNode = composeNode,
                                        composeNodeCallbacks = composeNodeCallbacks,
                                        destinationScreen = screen,
                                        initialParamsMap = action.paramsMap,
                                        onParametersMapUpdated = { newParamsMap ->
                                            val newActionsMap =
                                                mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                    putAll(composeNode.actionsMap)
                                                }
                                            val newAction = action.copy(paramsMap = newParamsMap)
                                            newActionsMap[actionType]?.set(
                                                index,
                                                ActionNode.Simple(action = newAction),
                                            )
                                            composeNodeCallbacks.onActionsMapUpdated(
                                                composeNode,
                                                newActionsMap,
                                            )
                                        },
                                    )
                                }
                            }

                            is StateAction.SetAppStateValue -> {
                                SetStateContentDetail(
                                    project = project,
                                    action = action,
                                    node = composeNode,
                                    onActionUpdated = { action ->
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = action),
                                        )

                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is CallApi -> {
                                val api = project.findApiDefinitionOrNull(action.apiId)
                                api?.let {
                                    Text(
                                        api.name,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )

                                    CallApiActionDetail(
                                        project = project,
                                        composeNode = composeNode,
                                        composeNodeCallbacks = composeNodeCallbacks,
                                        apiDefinition = api,
                                        initialParamsMap = action.paramsMap,
                                        onParametersMapUpdated = { newParamsMap ->
                                            val newActionsMap =
                                                mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                    putAll(composeNode.actionsMap)
                                                }
                                            val newAction = action.copy(paramsMap = newParamsMap)
                                            newActionsMap[actionType]?.set(
                                                index,
                                                ActionNode.Simple(action = newAction),
                                            )
                                            composeNodeCallbacks.onActionsMapUpdated(
                                                composeNode,
                                                newActionsMap,
                                            )
                                        },
                                    )
                                }
                            }

                            is ShowInformationDialog -> {
                                ShowInformationDialogContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is ShowModalWithComponent -> {
                                ShowModalWithComponentContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is ShowNavigationDrawer -> {
                                ShowNavigationDrawerContent(
                                    project = project,
                                    initialAction = action,
                                )
                            }

                            // Needs to be edited using ActionEditorDialog
                            is ShowConfirmationDialog -> {}
                            is ShowMessaging.Snackbar -> {
                                ShowSnackbarContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is DateOrTimePicker.OpenDatePicker -> {
                                OpenDatePickerContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is DateOrTimePicker.OpenDateAndTimePicker -> {
                                OpenDatePickerContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is Share.OpenUrl -> {
                                OpenUrlContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is Auth.SignInWithGoogle -> {}
                            is Auth.CreateUserWithEmailAndPassword -> {
                                CreateUserWithEmailAndPasswordContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is Auth.SignInWithEmailAndPassword -> {
                                SignInWithEmailAndPasswordContent(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is Auth.SignOut -> {}
                            is FirestoreAction.SaveToFirestore -> {
                                SaveToFirestoreContentDetail(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is FirestoreAction.UpdateDocument -> {
                                UpdateFirestoreDocumentContentDetail(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            is FirestoreAction.DeleteDocument -> {
                                DeleteFirestoreDocumentContentDetail(
                                    project = project,
                                    composeNode = composeNode,
                                    initialAction = action,
                                    onEditAction = {
                                        val newActionsMap =
                                            mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                                                putAll(composeNode.actionsMap)
                                            }
                                        newActionsMap[actionType]?.set(
                                            index,
                                            ActionNode.Simple(action = it),
                                        )
                                        composeNodeCallbacks.onActionsMapUpdated(
                                            composeNode,
                                            newActionsMap,
                                        )
                                    },
                                )
                            }

                            null -> {}
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (dialogOpen) {
        onAnyDialogIsShown()
        val closeDialog = {
            dialogOpen = false
            onAllDialogsClosed()
        }
        PositionCustomizablePopup(
            expanded = dialogOpen,
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
                actionInEdit = actionNodeInEdit?.getFocusedAction(),
                onActionSelected = { action ->
                    val newActionsMap =
                        mutableMapOf<ActionType, MutableList<ActionNode>>().apply {
                            putAll(composeNode.actionsMap)
                        }
                    actionNodeIndex?.let { actionNodeIndex ->
                        newActionsMap[actionType]?.set(
                            actionNodeIndex,
                            action.asActionNode(),
                        )
                    } ?: newActionsMap[actionType]?.add(action.asActionNode())

                    composeNodeCallbacks.onActionsMapUpdated(
                        composeNode,
                        newActionsMap,
                    )
                },
                onCloseClick = {
                    closeDialog()
                },
            )
        }
    }
}

@Composable
fun AddActionDialogContent(
    project: Project,
    actionInEdit: Action?,
    onActionSelected: (Action) -> Unit,
    onCloseClick: () -> Unit,
) {
    Surface(modifier = Modifier.size(width = 480.dp, height = 920.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
            )

            ActionSelector(
                project = project,
                actionInEdit = actionInEdit,
                onActionSelected = onActionSelected,
                onCloseDialog = {
                    onCloseClick()
                },
                modifier = Modifier.heightIn(max = 860.dp),
            )
            Spacer(Modifier.weight(1f))
            HorizontalDivider()
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                TextButton(
                    onClick = {
                        onCloseClick()
                    },
                    modifier =
                        Modifier
                            .padding(end = 16.dp),
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    }
}

@Composable
fun ActionSelector(
    project: Project,
    actionInEdit: Action?,
    onActionSelected: (Action) -> Unit,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp),
                ),
    ) {
        item {
            NavigateToActionContent(
                project = project,
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            NavigateBackContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            SetStateActionContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            FirestoreContent(
                project = project,
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            CallApiActionContent(
                project = project,
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            ShowModalActionContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            ShowMessagingContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            OpenDateOrTimePickerContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            ShareContent(
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
        item {
            AuthContent(
                project = project,
                actionInEdit = actionInEdit,
                onActionSelected = { action ->
                    onActionSelected(action)
                    onCloseDialog()
                },
            )
        }
    }
}

@Composable
fun Modifier.selectedActionModifier(
    actionInEdit: Action?,
    predicate: (Action?) -> Boolean,
): Modifier =
    this then (
        if (predicate(actionInEdit)) {
            Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                ).padding(2.dp)
        } else {
            Modifier
        }
    )
