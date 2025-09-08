package io.composeflow.ui.inspector.action

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_field_to_update
import io.composeflow.add_state_to_set
import io.composeflow.app_state
import io.composeflow.cancel
import io.composeflow.component_states
import io.composeflow.confirm
import io.composeflow.editor.validator.FloatValidator
import io.composeflow.editor.validator.IntValidator
import io.composeflow.index
import io.composeflow.model.action.Action
import io.composeflow.model.action.DataFieldUpdateProperty
import io.composeflow.model.action.FieldUpdateType
import io.composeflow.model.action.SetValueToState
import io.composeflow.model.action.StateAction
import io.composeflow.model.action.StateOperation
import io.composeflow.model.action.StateOperationForBoolean
import io.composeflow.model.action.StateOperationForBooleanList
import io.composeflow.model.action.StateOperationForCustomDataType
import io.composeflow.model.action.StateOperationForCustomDataTypeList
import io.composeflow.model.action.StateOperationForDataType
import io.composeflow.model.action.StateOperationForFloat
import io.composeflow.model.action.StateOperationForFloatList
import io.composeflow.model.action.StateOperationForInt
import io.composeflow.model.action.StateOperationForIntList
import io.composeflow.model.action.StateOperationForList
import io.composeflow.model.action.StateOperationForString
import io.composeflow.model.action.StateOperationForStringList
import io.composeflow.model.action.StateOperationProvider
import io.composeflow.model.action.StateOperationWithReadProperty
import io.composeflow.model.datatype.DataField
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.FieldType
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.findComponentOrThrow
import io.composeflow.model.project.findDataTypeOrThrow
import io.composeflow.model.project.findLocalStateOrNull
import io.composeflow.model.project.findScreenOrNull
import io.composeflow.model.property.IntProperty
import io.composeflow.model.state.ReadableState
import io.composeflow.model.state.StateHolderType
import io.composeflow.model.state.StateId
import io.composeflow.model.state.WriteableState
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.override.mutableStateListEqualsOverrideOf
import io.composeflow.remove
import io.composeflow.screen_states
import io.composeflow.select_all
import io.composeflow.select_fields_to_update
import io.composeflow.set_state
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.datafield.FieldRow
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.propertyeditor.AssignableBooleanPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.AssignableInstantPropertyEditor
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import io.composeflow.ui.state.StateLabel
import io.composeflow.ui.treeview.TreeView
import io.composeflow.ui.treeview.node.Branch
import io.composeflow.ui.treeview.node.BranchNode
import io.composeflow.ui.treeview.node.Leaf
import io.composeflow.ui.treeview.tree.Tree
import io.composeflow.ui.treeview.tree.TreeScope
import io.composeflow.unselect_all
import io.composeflow.update_type
import io.composeflow.value
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetStateActionContent(
    actionInEdit: Action?,
    onActionSelected: (Action) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clickable {
                    onActionSelected(StateAction.SetAppStateValue())
                }.fillMaxWidth()
                .hoverIconClickable()
                .hoverOverlay()
                .padding(vertical = 4.dp)
                .padding(start = 4.dp)
                .then(
                    Modifier.selectedActionModifier(
                        actionInEdit = actionInEdit,
                        predicate = {
                            it != null &&
                                it is StateAction.SetAppStateValue
                        },
                    ),
                ),
    ) {
        Text(
            text = stringResource(Res.string.set_state),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun SetStateContentDetail(
    project: Project,
    action: StateAction.SetAppStateValue,
    node: ComposeNode,
    onActionUpdated: (Action) -> Unit,
) {
    var addStateDialogOpen by remember { mutableStateOf(false) }
    Column {
        action.setValueToStates.forEachIndexed { i, setValueToState ->
            Column(
                modifier =
                    Modifier.hoverOverlay(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                        clipRadius = 8.dp,
                    ),
            ) {
                EditStateArea(
                    project = project,
                    action = action,
                    node = node,
                    setValueToState = setValueToState,
                    indexInAction = i,
                    onActionUpdated = onActionUpdated,
                )
            }
        }

        TextButton(onClick = {
            addStateDialogOpen = true
        }) {
            Text("+ ${stringResource(Res.string.add_state_to_set)}")
        }
    }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (addStateDialogOpen) {
        onAnyDialogIsShown()
        val closeDialog = {
            addStateDialogOpen = false
            onAllDialogsClosed()
        }
        AddStateToSetDialog(
            project = project,
            onDismissRequest = {
                closeDialog()
            },
            onStateSelected = {
                val writeState = project.findLocalStateOrNull(stateId = it.id)

                if (writeState != null) {
                    val type = writeState.valueType(project)
                    val operation =
                        if (type.isList) {
                            if (type is ComposeFlowType.CustomDataType) {
                                StateOperationForList.AddValueForCustomDataType()
                            } else {
                                StateOperationForList.AddValue(
                                    readProperty = type.defaultValue(),
                                )
                            }
                        } else {
                            if (type is ComposeFlowType.CustomDataType) {
                                StateOperationForDataType.DataTypeSetValue()
                            } else {
                                StateOperation.SetValue(
                                    readProperty = type.defaultValue(),
                                )
                            }
                        }
                    action.setValueToStates.add(
                        SetValueToState(writeToStateId = it.id, operation = operation),
                    )
                    onActionUpdated(action)
                }

                closeDialog()
            },
        )
    }
}

@Composable
private fun EditStateArea(
    project: Project,
    action: StateAction.SetAppStateValue,
    node: ComposeNode,
    setValueToState: SetValueToState,
    indexInAction: Int,
    onActionUpdated: (Action) -> Unit,
) {
    Column {
        val state = project.findLocalStateOrNull(setValueToState.writeToStateId) ?: return
        Row {
            StateLabel(project = project, state, modifier = Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.weight(1f))

            Tooltip(stringResource(Res.string.remove)) {
                ComposeFlowIconButton(
                    onClick = {
                        action.setValueToStates.removeAt(indexInAction)
                        onActionUpdated(action)
                    },
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        @Composable
        fun StateOperationEditor(stateOperationProvider: StateOperationProvider) {
            val entries = stateOperationProvider.entries()
            BasicDropdownPropertyEditor(
                project = project,
                items = entries,
                selectedItem = setValueToState.operation,
                label = stringResource(Res.string.update_type),
                onValueChanged = { _, item ->
                    val updatedAction =
                        setValueToState.copy(
                            operation = item,
                        )
                    action.setValueToStates[indexInAction] = updatedAction
                    onActionUpdated(action)
                },
                modifier = Modifier.width(300.dp),
            )
        }

        when (val type = state.valueType(project)) {
            is ComposeFlowType.StringType -> {
                if (type.isList) {
                    StateOperationEditor(StateOperationForStringList)
                } else {
                    StateOperationEditor(StateOperationForString)
                }
            }

            is ComposeFlowType.IntType -> {
                if (type.isList) {
                    StateOperationEditor(StateOperationForIntList)
                } else {
                    StateOperationEditor(StateOperationForInt)
                }
            }

            is ComposeFlowType.FloatType -> {
                if (type.isList) {
                    StateOperationEditor(StateOperationForFloatList)
                } else {
                    StateOperationEditor(StateOperationForFloat)
                }
            }

            is ComposeFlowType.BooleanType -> {
                if (type.isList) {
                    StateOperationEditor(StateOperationForBooleanList)
                } else {
                    StateOperationEditor(StateOperationForBoolean)
                }
            }

            is ComposeFlowType.CustomDataType -> {
                if (type.isList) {
                    StateOperationEditor(StateOperationForCustomDataTypeList)
                } else {
                    StateOperationEditor(StateOperationForCustomDataType)
                }
            }

            is ComposeFlowType.Enum<*> -> {}
            is ComposeFlowType.Color -> throw UnsupportedOperationException("Color isn't supported for the state")
            is ComposeFlowType.Brush -> throw UnsupportedOperationException("Brush isn't supported for the state")
            is ComposeFlowType.InstantType -> throw UnsupportedOperationException("Instant isn't supported for the state")
            is ComposeFlowType.JsonElementType -> throw UnsupportedOperationException("JsonElement isn't supported for the state")
            is ComposeFlowType.AnyType -> throw UnsupportedOperationException("Any isn't supported for the state")
            is ComposeFlowType.DocumentIdType -> UnsupportedOperationException("DocumentId isn't supported for the state")
            is ComposeFlowType.UnknownType -> throw UnsupportedOperationException("Unknown isn't supported for the state")
        }

        when (val operation = setValueToState.operation) {
            StateOperation.ClearValue -> {}
            StateOperation.ToggleValue -> {}
            is StateOperation.SetValue -> {
                StateOperationEditor(
                    acceptableType = state.valueType(project),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                    label = stringResource(Res.string.value),
                )
            }

            is StateOperationForList.AddValue -> {
                StateOperationEditor(
                    acceptableType = state.valueType(project, asNonList = true),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                    label = stringResource(Res.string.value),
                )
            }

            StateOperationForList.RemoveFirstValue -> {}
            StateOperationForList.RemoveLastValue -> {}
            is StateOperationForList.RemoveValueAtIndex -> {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    initialProperty = operation.indexProperty,
                    destinationStateId = setValueToState.writeToStateId,
                    onValidPropertyChanged = { property, _ ->
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = property as IntProperty,
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    validateInput = {
                        IntValidator(allowLessThanZero = false).validate(it)
                    },
                    label = stringResource(Res.string.index),
                    onInitializeProperty = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = IntProperty.IntIntrinsicValue(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                )
            }

            is StateOperationForList.UpdateValueAtIndex -> {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    initialProperty = operation.indexProperty,
                    destinationStateId = setValueToState.writeToStateId,
                    onValidPropertyChanged = { property, _ ->
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = property as IntProperty,
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    label = stringResource(Res.string.index),
                    validateInput = {
                        IntValidator(allowLessThanZero = false).validate(it)
                    },
                    onInitializeProperty = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = IntProperty.IntIntrinsicValue(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                )

                StateOperationEditor(
                    acceptableType = state.valueType(project, asNonList = true),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                    label = stringResource(Res.string.value),
                )
            }

            is StateOperationForDataType.DataTypeSetValue -> {
                StateOperationEditorForCustomDataType(
                    acceptableType = state.valueType(project, asNonList = true),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                )
            }

            is StateOperationForList.AddValueForCustomDataType -> {
                StateOperationEditorForCustomDataType(
                    acceptableType = state.valueType(project, asNonList = true),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                )
            }

            is StateOperationForList.UpdateValueAtIndexForCustomDataType -> {
                AssignableEditableTextPropertyEditor(
                    project = project,
                    node = node,
                    acceptableType = ComposeFlowType.IntType(),
                    initialProperty = operation.indexProperty,
                    destinationStateId = setValueToState.writeToStateId,
                    onValidPropertyChanged = { property, _ ->
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = property as IntProperty,
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    label = stringResource(Res.string.index),
                    validateInput = {
                        IntValidator(allowLessThanZero = false).validate(it)
                    },
                    onInitializeProperty = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copyWith(
                                        indexProperty = IntProperty.IntIntrinsicValue(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                )

                StateOperationEditorForCustomDataType(
                    acceptableType = state.valueType(project, asNonList = true),
                    project = project,
                    node = node,
                    operation = operation,
                    setValueToState = setValueToState,
                    action = action,
                    indexInAction = indexInAction,
                    onActionUpdated = onActionUpdated,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun StateOperationEditor(
    acceptableType: ComposeFlowType,
    project: Project,
    node: ComposeNode,
    operation: StateOperationWithReadProperty,
    setValueToState: SetValueToState,
    action: StateAction.SetAppStateValue,
    indexInAction: Int,
    onActionUpdated: (Action) -> Unit,
    label: String = "",
) {
    if (acceptableType.isList) return
    acceptableType.defaultValue().Editor(
        project = project,
        node = node,
        initialProperty = operation.readProperty,
        destinationStateId = setValueToState.writeToStateId,
        onValidPropertyChanged = { property, _ ->
            action.setValueToStates[indexInAction] =
                setValueToState.copy(
                    operation = operation.copyWith(readProperty = property),
                )
            onActionUpdated(action)
        },
        onInitializeProperty = {
            action.setValueToStates[indexInAction] =
                setValueToState.copy(
                    operation =
                        operation.copyWith(
                            readProperty = acceptableType.defaultValue(),
                        ),
                )
            onActionUpdated(action)
        },
        label = label,
        modifier = Modifier,
        validateInput = null,
        editable = true,
        functionScopeProperties = emptyList(),
    )
}

@Composable
private fun StateOperationEditorForCustomDataType(
    acceptableType: ComposeFlowType,
    project: Project,
    node: ComposeNode,
    operation: StateOperationForDataType,
    setValueToState: SetValueToState,
    action: StateAction.SetAppStateValue,
    indexInAction: Int,
    onActionUpdated: (Action) -> Unit,
) {
    check(acceptableType is ComposeFlowType.CustomDataType)
    val dataType =
        acceptableType.dataTypeId.let { dataTypeId ->
            project.findDataTypeOrThrow(dataTypeId)
        }

    Column {
        when (operation) {
            is StateOperationForDataType.DataTypeSetValue -> {
                EditUpdatePropertiesForDataType(
                    project = project,
                    node = node,
                    dataFieldUpdateProperties = operation.dataFieldUpdateProperties,
                    onDataFieldUpdatePropertiesUpdated = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copy(
                                        dataFieldUpdateProperties = it.toMutableList(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    dataType = dataType,
                    destinationStateId = setValueToState.writeToStateId,
                )
            }

            is StateOperationForList.AddValueForCustomDataType -> {
                EditUpdatePropertiesForDataType(
                    project = project,
                    node = node,
                    dataFieldUpdateProperties = operation.dataFieldUpdateProperties,
                    onDataFieldUpdatePropertiesUpdated = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copy(
                                        dataFieldUpdateProperties = it.toMutableList(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    dataType = dataType,
                    destinationStateId = setValueToState.writeToStateId,
                )
            }

            is StateOperationForList.UpdateValueAtIndexForCustomDataType -> {
                EditUpdatePropertiesForDataType(
                    project = project,
                    node = node,
                    dataFieldUpdateProperties = operation.dataFieldUpdateProperties,
                    onDataFieldUpdatePropertiesUpdated = {
                        action.setValueToStates[indexInAction] =
                            setValueToState.copy(
                                operation =
                                    operation.copy(
                                        dataFieldUpdateProperties = it.toMutableList(),
                                    ),
                            )
                        onActionUpdated(action)
                    },
                    dataType = dataType,
                    destinationStateId = setValueToState.writeToStateId,
                    updateValue = true,
                )
            }
        }
    }
}

@Composable
fun EditUpdatePropertiesForDataType(
    project: Project,
    node: ComposeNode,
    dataFieldUpdateProperties: List<DataFieldUpdateProperty>,
    dataType: DataType,
    onDataFieldUpdatePropertiesUpdated: (List<DataFieldUpdateProperty>) -> Unit,
    updateValue: Boolean = false,
    destinationStateId: StateId? = null,
) {
    var addFieldDialogOpen by remember { mutableStateOf(false) }
    dataFieldUpdateProperties.forEach { (fieldId, readProperty, fieldUpdateType) ->
        val dataField = dataType.findDataFieldOrNull(fieldId) ?: return
        val fieldIndex =
            dataFieldUpdateProperties.indexOfFirst { it.dataFieldId == fieldId }
        val fieldUpdateProperties = dataFieldUpdateProperties[fieldIndex]

        Column(
            modifier =
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .hoverOverlay(
                        overlayColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                        clipRadius = 8.dp,
                    ),
        ) {
            Row {
                Text(
                    text = dataField.variableName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(4.dp),
                )
                Text(
                    text = dataField.fieldType.type().displayName(project),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(4.dp),
                )
            }
            when (dataField.fieldType) {
                is FieldType.Boolean -> {
                    if (updateValue) {
                        BasicDropdownPropertyEditor(
                            project = project,
                            items = FieldUpdateType.Boolean.entries(),
                            onValueChanged = { index, item ->

                                onDataFieldUpdatePropertiesUpdated(
                                    dataFieldUpdateProperties.toMutableList().apply {
                                        set(
                                            fieldIndex,
                                            fieldUpdateProperties.copy(
                                                fieldUpdateType = item,
                                            ),
                                        )
                                    },
                                )
                            },
                            selectedItem = fieldUpdateType,
                            label = stringResource(Res.string.update_type),
                        )
                    }

                    when (fieldUpdateProperties.fieldUpdateType) {
                        FieldUpdateType.ClearValue -> {}
                        FieldUpdateType.ToggleValue -> {}
                        FieldUpdateType.SetValue -> {
                            AssignableBooleanPropertyEditor(
                                project = project,
                                node = node,
                                acceptableType = dataField.fieldType.type(),
                                initialProperty = readProperty,
                                destinationStateId = destinationStateId,
                                onValidPropertyChanged = { property, item ->
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty = property,
                                                    fieldUpdateType = fieldUpdateType,
                                                ),
                                            )
                                        },
                                    )
                                },
                                onInitializeProperty = {
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty =
                                                        dataField.fieldType
                                                            .type()
                                                            .defaultValue(),
                                                ),
                                            )
                                        },
                                    )
                                },
                                label = dataField.variableName,
                            )
                        }
                    }
                }

                is FieldType.Int -> {
                    if (updateValue) {
                        BasicDropdownPropertyEditor(
                            project = project,
                            items = FieldUpdateType.Normal.entries(),
                            onValueChanged = { _, item ->
                                onDataFieldUpdatePropertiesUpdated(
                                    dataFieldUpdateProperties.toMutableList().apply {
                                        set(
                                            fieldIndex,
                                            fieldUpdateProperties.copy(
                                                fieldUpdateType = item,
                                            ),
                                        )
                                    },
                                )
                            },
                            selectedItem = fieldUpdateType,
                            label = stringResource(Res.string.update_type),
                        )
                    }
                    when (fieldUpdateProperties.fieldUpdateType) {
                        FieldUpdateType.ClearValue -> {}
                        FieldUpdateType.ToggleValue -> {}
                        FieldUpdateType.SetValue -> {
                            AssignableEditableTextPropertyEditor(
                                project = project,
                                node = node,
                                acceptableType = dataField.fieldType.type(),
                                initialProperty = readProperty,
                                onValidPropertyChanged = { property, _ ->
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty = property,
                                                    fieldUpdateType = fieldUpdateType,
                                                ),
                                            )
                                        },
                                    )
                                },
                                onInitializeProperty = {
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty =
                                                        dataField.fieldType
                                                            .type()
                                                            .defaultValue(),
                                                ),
                                            )
                                        },
                                    )
                                },
                                validateInput = IntValidator()::validate,
                                label = dataField.variableName,
                            )
                        }
                    }
                }

                is FieldType.Float -> {
                    if (updateValue) {
                        BasicDropdownPropertyEditor(
                            project = project,
                            items = FieldUpdateType.Normal.entries(),
                            onValueChanged = { _, item ->
                                onDataFieldUpdatePropertiesUpdated(
                                    dataFieldUpdateProperties.toMutableList().apply {
                                        set(
                                            fieldIndex,
                                            fieldUpdateProperties.copy(
                                                fieldUpdateType = item,
                                            ),
                                        )
                                    },
                                )
                            },
                            selectedItem = fieldUpdateType,
                            label = stringResource(Res.string.update_type),
                        )
                    }
                    when (fieldUpdateProperties.fieldUpdateType) {
                        FieldUpdateType.ClearValue -> {}
                        FieldUpdateType.ToggleValue -> {}
                        FieldUpdateType.SetValue -> {
                            AssignableEditableTextPropertyEditor(
                                project = project,
                                node = node,
                                acceptableType = dataField.fieldType.type(),
                                initialProperty = readProperty,
                                onValidPropertyChanged = { property, _ ->
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty = property,
                                                    fieldUpdateType = fieldUpdateType,
                                                ),
                                            )
                                        },
                                    )
                                },
                                onInitializeProperty = {
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty =
                                                        dataField.fieldType
                                                            .type()
                                                            .defaultValue(),
                                                ),
                                            )
                                        },
                                    )
                                },
                                validateInput = FloatValidator()::validate,
                                label = dataField.variableName,
                            )
                        }
                    }
                }

                is FieldType.String -> {
                    if (updateValue) {
                        BasicDropdownPropertyEditor(
                            project = project,
                            items = FieldUpdateType.Normal.entries(),
                            onValueChanged = { _, item ->
                                onDataFieldUpdatePropertiesUpdated(
                                    dataFieldUpdateProperties.toMutableList().apply {
                                        set(
                                            fieldIndex,
                                            fieldUpdateProperties.copy(
                                                fieldUpdateType = item,
                                            ),
                                        )
                                    },
                                )
                            },
                            selectedItem = fieldUpdateType,
                            label = stringResource(Res.string.update_type),
                        )
                    }
                    when (fieldUpdateProperties.fieldUpdateType) {
                        FieldUpdateType.ClearValue -> {}
                        FieldUpdateType.ToggleValue -> {}
                        FieldUpdateType.SetValue -> {
                            AssignableEditableTextPropertyEditor(
                                project = project,
                                node = node,
                                acceptableType = dataField.fieldType.type(),
                                initialProperty = readProperty,
                                onValidPropertyChanged = { property, _ ->
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty = property,
                                                    fieldUpdateType = fieldUpdateType,
                                                ),
                                            )
                                        },
                                    )
                                },
                                onInitializeProperty = {
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty =
                                                        dataField.fieldType
                                                            .type()
                                                            .defaultValue(),
                                                ),
                                            )
                                        },
                                    )
                                },
                                label = dataField.variableName,
                            )
                        }
                    }
                }

                is FieldType.Instant -> {
                    if (updateValue) {
                        BasicDropdownPropertyEditor(
                            project = project,
                            items = FieldUpdateType.Normal.entries(),
                            onValueChanged = { index, item ->
                                onDataFieldUpdatePropertiesUpdated(
                                    dataFieldUpdateProperties.toMutableList().apply {
                                        set(
                                            fieldIndex,
                                            fieldUpdateProperties.copy(
                                                fieldUpdateType = item,
                                            ),
                                        )
                                    },
                                )
                            },
                            selectedItem = fieldUpdateType,
                            label = stringResource(Res.string.update_type),
                        )
                    }
                    when (fieldUpdateProperties.fieldUpdateType) {
                        FieldUpdateType.ClearValue -> {}
                        FieldUpdateType.ToggleValue -> {}
                        FieldUpdateType.SetValue -> {
                            AssignableInstantPropertyEditor(
                                project = project,
                                node = node,
                                acceptableType = dataField.fieldType.type(),
                                initialProperty = readProperty,
                                onValidPropertyChanged = { property, _ ->
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty = property,
                                                    fieldUpdateType = fieldUpdateType,
                                                ),
                                            )
                                        },
                                    )
                                },
                                onInitializeProperty = {
                                    onDataFieldUpdatePropertiesUpdated(
                                        dataFieldUpdateProperties.toMutableList().apply {
                                            set(
                                                fieldIndex,
                                                fieldUpdateProperties.copy(
                                                    dataFieldId = fieldId,
                                                    assignableProperty =
                                                        dataField.fieldType
                                                            .type()
                                                            .defaultValue(),
                                                ),
                                            )
                                        },
                                    )
                                },
                                label = dataField.variableName,
                            )
                        }
                    }
                }

                is FieldType.CustomDataType -> {}
                is FieldType.DocumentId -> {}
            }
        }
    }

    TextButton(onClick = {
        addFieldDialogOpen = true
    }) {
        Text("+ ${stringResource(Res.string.add_field_to_update)}")
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (addFieldDialogOpen) {
        onAnyDialogIsShown()
        val closeDialog = {
            addFieldDialogOpen = false
            onAllDialogsClosed()
        }
        AddFieldToSetDialog(
            project = project,
            dataType = dataType,
            initialFieldIdToReadProperties = dataFieldUpdateProperties,
            onFieldsToSetConfirmed = {
                onDataFieldUpdatePropertiesUpdated(
                    dataFieldUpdateProperties.toMutableList().apply {
                        clear()
                        addAll(it)
                    },
                )
            },
            onDismissRequest = closeDialog,
        )
    }
}

@Composable
fun AddFieldToSetDialog(
    project: Project,
    dataType: DataType,
    initialFieldIdToReadProperties: List<DataFieldUpdateProperty>,
    onFieldsToSetConfirmed: (List<DataFieldUpdateProperty>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(modifier = Modifier.size(480.dp)) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .verticalScroll(state = rememberScrollState()),
            ) {
                Text(
                    text = stringResource(Res.string.select_fields_to_update),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val editedSelectedFieldIds =
                    remember {
                        mutableStateListEqualsOverrideOf<DataFieldUpdateProperty>()
                            .apply { addAll(initialFieldIdToReadProperties) }
                    }

                fun addDataFieldIfNotPresentInEdit(dataField: DataField) {
                    editedSelectedFieldIds.add(
                        DataFieldUpdateProperty(
                            dataFieldId = dataField.id,
                            assignableProperty = dataField.fieldType.type().defaultValue(),
                        ),
                    )
                }

                fun removeDataFieldIfNotPresentInEdit(dataField: DataField) {
                    editedSelectedFieldIds.removeAll { it.dataFieldId == dataField.id }
                }

                fun toggleDataFieldInEdit(dataField: DataField) {
                    if (dataField.id in editedSelectedFieldIds.toList().map { it.dataFieldId }) {
                        removeDataFieldIfNotPresentInEdit(dataField)
                    } else {
                        addDataFieldIfNotPresentInEdit(dataField)
                    }
                }

                Row {
                    TextButton(onClick = {
                        dataType.fields.forEach { addDataFieldIfNotPresentInEdit(it) }
                    }) {
                        Text(stringResource(Res.string.select_all))
                    }
                    TextButton(onClick = {
                        dataType.fields.forEach { removeDataFieldIfNotPresentInEdit(it) }
                    }) {
                        Text(stringResource(Res.string.unselect_all))
                    }
                }
                dataType.fields.forEach { dataField ->
                    val selectedModifier =
                        if (dataField.id in
                            editedSelectedFieldIds
                                .toList()
                                .map { it.dataFieldId }
                        ) {
                            Modifier
                                .padding(start = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                ).padding(8.dp)
                        } else {
                            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                        }
                    FieldRow(
                        project = project,
                        fieldName = dataField.variableName,
                        type = dataField.fieldType.type(),
                        modifier =
                            Modifier
                                .padding(top = 4.dp)
                                .then(selectedModifier),
                        onClick = {
                            toggleDataFieldInEdit(dataField)
                        },
                    )
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    OutlinedButton(
                        onClick = {
                            onFieldsToSetConfirmed(editedSelectedFieldIds)
                            onDismissRequest()
                        },
                        enabled = editedSelectedFieldIds.toList() != initialFieldIdToReadProperties,
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStateToSetDialog(
    project: Project,
    onDismissRequest: () -> Unit,
    onStateSelected: (ReadableState) -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(modifier = Modifier.size(480.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                var selectedState by remember { mutableStateOf<ReadableState?>(null) }
                var treeInitiallyExpanded by remember { mutableStateOf(false) }

                val statesMap =
                    project
                        .screenHolder
                        .currentEditable()
                        .getStateResults(project)
                        .groupBy { it.first }

                // Data class to hold state information
                data class StateTreeData(
                    val stateHolderType: StateHolderType,
                    val state: ReadableState,
                )

                @Composable
                fun TreeScope.StateHolderBranch(
                    holderType: StateHolderType,
                    states: List<Pair<StateHolderType, ReadableState>>,
                    project: Project,
                    children: @Composable TreeScope.() -> Unit,
                ) {
                    Branch(
                        content = StateTreeData(holderType, states.first().second),
                        key = holderType.toString(),
                        children = children,
                        customName = { node ->
                            when (val stateHolderType = node.content.stateHolderType) {
                                StateHolderType.Global -> {
                                    Text(
                                        text = stringResource(Res.string.app_state),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(end = 8.dp),
                                    )
                                }

                                is StateHolderType.Screen -> {
                                    val screen = project.findScreenOrNull(stateHolderType.screenId)
                                    screen?.let { s ->
                                        Text(
                                            text = stringResource(Res.string.screen_states) + " [${s.name}]",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                        )
                                    }
                                }

                                is StateHolderType.Component -> {
                                    val component = project.findComponentOrThrow(stateHolderType.componentId)
                                    Text(
                                        text = stringResource(Res.string.component_states) + " [${component.name}]",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        },
                    )
                }

                @Composable
                fun TreeScope.StateLeaf(
                    holderType: StateHolderType,
                    state: WriteableState,
                    project: Project,
                ) {
                    Leaf(
                        content = StateTreeData(holderType, state),
                        key = state.id,
                        customName = { node ->
                            StateLabel(project = project, state = node.content.state)
                        },
                    )
                }

                val tree =
                    Tree<StateTreeData> {
                        statesMap.forEach { entry ->
                            if (entry.value.isNotEmpty()) {
                                val holderType = entry.key
                                // Add branch for state holder type
                                StateHolderBranch(holderType, entry.value, project) {
                                    entry.value.forEach { state ->
                                        val writeState = state.second
                                        if (writeState is WriteableState && writeState.userWritable) {
                                            StateLeaf(holderType, writeState, project)
                                        }
                                    }
                                }
                            }
                        }
                    }
                val lazyListState = rememberLazyListState()

                // Set initial selection
                selectedState?.let { state ->
                    tree.nodes.find { it.content.state.id == state.id }?.let { node ->
                        tree.selectNode(node)
                    }
                }

                // Expand all root nodes initially
                LaunchedEffect(Unit) {
                    if (!treeInitiallyExpanded) {
                        tree.nodes.filter { it.depth == 0 }.forEach { rootNode ->
                            if (rootNode is BranchNode) {
                                tree.expandNode(rootNode)
                            }
                        }
                        treeInitiallyExpanded = true
                    }
                }

                TreeView(
                    tree = tree,
                    listState = lazyListState,
                    onClick = { treeNode, _, _ ->
                        tree.clearSelection()
                        tree.selectNode(treeNode)

                        when (treeNode.depth) {
                            0 -> {
                                // Clicked on state holder type - don't select
                                selectedState = null
                                tree.clearSelection()
                            }
                            else -> {
                                // Clicked on actual state
                                selectedState = treeNode.content.state as? WriteableState
                            }
                        }
                    },
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        onDismissRequest()
                    }) {
                        Text(stringResource(Res.string.cancel))
                    }

                    OutlinedButton(
                        onClick = {
                            selectedState?.let {
                                onStateSelected(it)
                            }
                        },
                        enabled = selectedState != null,
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}
