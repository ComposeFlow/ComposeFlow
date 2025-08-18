package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.editor.validator.ValidateResult
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.string.updateStringResourceDefaultLocaleValue
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.FloatProperty
import io.composeflow.model.property.FunctionScopeParameterProperty
import io.composeflow.model.property.IntProperty
import io.composeflow.model.property.IntrinsicProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.getErrorMessage
import io.composeflow.model.property.leadingIcon
import io.composeflow.model.property.mergeProperty
import io.composeflow.model.state.StateId
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.set_from_state
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.propertyeditor.variable.SelectStringResourceDialog
import io.composeflow.ui.propertyeditor.variable.SetFromStateDialog
import io.composeflow.use_string_resource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AssignableEditableTextPropertyEditor(
    project: Project,
    node: ComposeNode,
    onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
    modifier: Modifier = Modifier,
    acceptableType: ComposeFlowType = ComposeFlowType.StringType(isList = false),
    label: String = "",
    initialProperty: AssignableProperty? = null,
    destinationStateId: StateId? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    onInitializeProperty: (() -> Unit)? = null,
    functionScopeProperties: List<FunctionScopeParameterProperty> = emptyList(),
    validateInput: (String) -> ValidateResult = { ValidateResult.Success },
    singleLine: Boolean = true,
    variableAssignable: Boolean = true,
    editable: Boolean = true,
) {
    var stateDialogOpen by remember { mutableStateOf(false) }
    var stringResourceDialogOpen by remember { mutableStateOf(false) }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    val propertyValueEditable =
        initialProperty is IntrinsicProperty<*> || initialProperty is StringProperty.ValueFromStringResource
    val textFieldEnabled = editable && (propertyValueEditable || initialProperty == null)

    val resolvedLeadingIcon =
        if (leadingIcon != null) {
            leadingIcon
        } else {
            {
                ComposeFlowIcon(
                    imageVector = acceptableType.leadingIcon(),
                    contentDescription = "",
                )
            }
        }
    val errorText = initialProperty?.getErrorMessage(project, acceptableType)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        EditableTextProperty(
            enabled = textFieldEnabled,
            onValidValueChanged = {
                val newProperty =
                    when (acceptableType) {
                        is ComposeFlowType.IntType -> IntProperty.IntIntrinsicValue(it.toInt())
                        is ComposeFlowType.FloatType -> FloatProperty.FloatIntrinsicValue(it.toFloat())
                        is ComposeFlowType.StringType -> StringProperty.StringIntrinsicValue(it)
                        else -> throw IllegalArgumentException("Invalid acceptable type: $acceptableType")
                    }
                onValidPropertyChanged(
                    initialProperty.mergeProperty(
                        project = project,
                        newProperty,
                    ),
                    null,
                )
            },
            initialValue =
                if (textFieldEnabled) {
                    initialProperty?.displayText(project)
                } else {
                    val resolvedExpression = initialProperty?.transformedValueExpression(project)
                    resolvedExpression
                } ?: "",
            label = label,
            placeholder = placeholder,
            validateInput = validateInput,
            leadingIcon = resolvedLeadingIcon,
            singleLine = singleLine,
            modifier = Modifier.weight(1f),
            supportingText = errorText,
            valueSetFromVariable = initialProperty !is IntrinsicProperty<*>,
        )

        // String resource button
        if (editable && acceptableType is ComposeFlowType.StringType) {
            val useStringResource = stringResource(Res.string.use_string_resource)
            Tooltip(useStringResource) {
                ComposeFlowIconButton(
                    onClick = {
                        stringResourceDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.TextFields,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentDescription = useStringResource,
                    )
                }
            }
        }

        // State/variable button
        if (editable && variableAssignable) {
            val setFromVariable = stringResource(Res.string.set_from_state)
            Tooltip(setFromVariable) {
                ComposeFlowIconButton(
                    onClick = {
                        stateDialogOpen = true
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.ElectricalServices,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentDescription = setFromVariable,
                    )
                }
            }
        }

        // String resource dialog
        if (stringResourceDialogOpen) {
            onAnyDialogIsShown()
            SelectStringResourceDialog(
                project = project,
                initialProperty = initialProperty,
                onCloseClick = {
                    stringResourceDialogOpen = false
                    onAllDialogsClosed()
                },
                onValidPropertyChanged = onValidPropertyChanged,
                onInitializeProperty = onInitializeProperty,
            )
        }

        // State dialog
        if (stateDialogOpen) {
            onAnyDialogIsShown()
            SetFromStateDialog(
                project = project,
                initialProperty = initialProperty,
                node = node,
                acceptableType = acceptableType,
                onCloseClick = {
                    stateDialogOpen = false
                    onAllDialogsClosed()
                },
                onInitializeProperty = onInitializeProperty,
                onValidPropertyChanged = onValidPropertyChanged,
                defaultValue = acceptableType.defaultValue(),
                destinationStateId = destinationStateId,
                functionScopeProperties = functionScopeProperties,
            )
        }
    }
}
