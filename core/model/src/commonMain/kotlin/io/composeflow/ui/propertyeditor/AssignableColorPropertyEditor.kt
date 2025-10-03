package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.composeflow.Res
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.defaultColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.ColorProperty
import io.composeflow.model.property.FunctionScopeParameterProperty
import io.composeflow.model.property.IntrinsicProperty
import io.composeflow.model.property.getErrorMessage
import io.composeflow.model.property.mergeProperty
import io.composeflow.model.state.StateId
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.set_from_state
import io.composeflow.set_from_state_for
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.propertyeditor.variable.SetFromStateDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun AssignableColorPropertyEditor(
    project: Project,
    node: ComposeNode,
    modifier: Modifier = Modifier,
    acceptableType: ComposeFlowType = ComposeFlowType.Color(isList = false),
    label: String = "",
    initialProperty: AssignableProperty? = null,
    destinationStateId: StateId? = null,
    onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit = { _, _ -> },
    onInitializeProperty: (() -> Unit)? = null,
    functionScopeProperties: List<FunctionScopeParameterProperty> = emptyList(),
    editable: Boolean = true,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        val errorText = initialProperty?.getErrorMessage(project, acceptableType)
        val editEnabled =
            editable && (initialProperty is IntrinsicProperty<*> || initialProperty == null)
        if (editEnabled) {
            ColorPropertyEditor(
                label = label,
                initialColor = (initialProperty as? ColorProperty.ColorIntrinsicValue)?.value,
                extendedColors = project.themeHolder.colorSchemeHolder.extendedColors.value,
                onColorUpdated = {
                    val newProperty =
                        ColorProperty.ColorIntrinsicValue(ColorWrapper(themeColor = null, color = it))
                    onValidPropertyChanged(
                        initialProperty.mergeProperty(project, newProperty),
                        null,
                    )
                },
                onThemeColorSelected = {
                    val newProperty =
                        ColorProperty.ColorIntrinsicValue(ColorWrapper(themeColor = it, color = null))
                    onValidPropertyChanged(
                        initialProperty.mergeProperty(project, newProperty),
                        null,
                    )
                },
                onColorDeleted = {
                    onValidPropertyChanged(
                        ColorProperty.ColorIntrinsicValue(defaultColorWrapper),
                        null,
                    )
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            EditableTextProperty(
                initialValue = initialProperty?.transformedValueExpression(project) ?: "",
                onValidValueChanged = {},
                enabled = false,
                label = label,
                leadingIcon = {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = null,
                    )
                },
                supportingText = errorText,
                modifier = Modifier.weight(1f),
                valueSetFromVariable = initialProperty !is IntrinsicProperty<*>,
            )
        }

        if (editable) {
            val setFromVariable = stringResource(Res.string.set_from_state)
            Tooltip(setFromVariable) {
                ComposeFlowIconButton(
                    onClick = {
                        dialogOpen = true
                    },
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.ElectricalServices,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentDescription = stringResource(Res.string.set_from_state_for),
                    )
                }
            }
        }
        if (dialogOpen) {
            onAnyDialogIsShown()
            SetFromStateDialog(
                project = project,
                initialProperty = initialProperty,
                node = node,
                acceptableType = acceptableType,
                onCloseClick = {
                    dialogOpen = false
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
