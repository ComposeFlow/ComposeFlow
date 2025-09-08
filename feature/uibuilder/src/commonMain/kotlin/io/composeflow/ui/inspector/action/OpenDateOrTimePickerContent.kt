package io.composeflow.ui.inspector.action

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.editor.validator.IntValidator
import io.composeflow.model.action.Action
import io.composeflow.model.action.DateOrTimePicker
import io.composeflow.model.action.ShowModal
import io.composeflow.model.action.YearRangeSelectableAction
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.IntProperty
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.output_state_name_description
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.propertyeditor.AssignableEditableTextPropertyEditor
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor
import io.composeflow.ui.propertyeditor.EditableTextProperty
import io.composeflow.ui.utils.TreeExpanderInverse
import org.jetbrains.compose.resources.stringResource

@Composable
fun OpenDateOrTimePickerContent(
    actionInEdit: Action?,
    onActionSelected: (Action) -> Unit,
) {
    var showMessagingActionsOpened by remember { mutableStateOf(true) }
    Column(modifier = Modifier.animateContentSize(keyframes { durationMillis = 100 })) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.clickable {
                    showMessagingActionsOpened = !showMessagingActionsOpened
                },
        ) {
            Text(
                text = "Open date/time picker",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            TreeExpanderInverse(
                expanded = showMessagingActionsOpened,
                onClick = {
                    showMessagingActionsOpened = !showMessagingActionsOpened
                },
            )
        }
        if (showMessagingActionsOpened) {
            DateOrTimePicker.entries().forEach { dateOrTimePickerAction ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .hoverIconClickable()
                            .hoverOverlay()
                            .padding(vertical = 4.dp)
                            .padding(start = 8.dp)
                            .clickable {
                                onActionSelected(
                                    dateOrTimePickerAction,
                                )
                            }.selectedActionModifier(
                                actionInEdit = actionInEdit,
                                predicate = {
                                    actionInEdit != null &&
                                        actionInEdit is ShowModal &&
                                        actionInEdit.name == dateOrTimePickerAction.name
                                },
                            ),
                ) {
                    Text(
                        text = dateOrTimePickerAction.name,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun OpenDatePickerContent(
    project: Project,
    composeNode: ComposeNode,
    initialAction: YearRangeSelectableAction,
    onEditAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AssignableEditableTextPropertyEditor(
            project = project,
            node = composeNode,
            acceptableType = ComposeFlowType.IntType(),
            initialProperty = initialAction.minSelectableYear.value,
            label = "Min selectable year",
            onValidPropertyChanged = { property, _ ->
                initialAction.minSelectableYear.value = property
                onEditAction(initialAction)
            },
            modifier = Modifier.hoverOverlay(),
            onInitializeProperty = {
                initialAction.minSelectableYear.value = null
                onEditAction(initialAction)
            },
            validateInput = IntValidator(
                maxValue =
                    when (val maxYear = initialAction.maxSelectableYear.value) {
                        is IntProperty.IntIntrinsicValue -> maxYear.value
                        else -> Int.MAX_VALUE
                    },
            )::validate,
            leadingIcon = {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Numbers,
                    contentDescription = null,
                )
            },
        )

        AssignableEditableTextPropertyEditor(
            project = project,
            node = composeNode,
            acceptableType = ComposeFlowType.IntType(),
            initialProperty = initialAction.maxSelectableYear.value,
            label = "Max selectable year",
            onValidPropertyChanged = { property, _ ->
                initialAction.maxSelectableYear.value = property
                onEditAction(initialAction)
            },
            modifier = Modifier.hoverOverlay(),
            onInitializeProperty = {
                initialAction.maxSelectableYear.value = null
                onEditAction(initialAction)
            },
            validateInput = IntValidator(
                minValue =
                    when (val minYear = initialAction.minSelectableYear.value) {
                        is IntProperty.IntIntrinsicValue -> minYear.value
                        else -> Int.MIN_VALUE
                    },
            )::validate,
            leadingIcon = {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Numbers,
                    contentDescription = null,
                )
            },
        )

        BooleanPropertyEditor(
            checked = initialAction.onlyPastDates.value,
            onCheckedChange = {
                initialAction.onlyPastDates.value = it
                if (initialAction.onlyPastDates.value) {
                    initialAction.onlyFutureDates.value = false
                }
                onEditAction(initialAction)
            },
            label = "Only past dates",
        )
        BooleanPropertyEditor(
            checked = initialAction.onlyFutureDates.value,
            onCheckedChange = {
                initialAction.onlyFutureDates.value = it
                if (initialAction.onlyFutureDates.value) {
                    initialAction.onlyPastDates.value = false
                }
                onEditAction(initialAction)
            },
            label = "Only future dates",
        )

        Tooltip(stringResource(Res.string.output_state_name_description)) {
            EditableTextProperty(
                initialValue = initialAction.outputStateName.value,
                label = "Output state name",
                onValidValueChanged = {
                    initialAction.outputStateName.value = it
                    onEditAction(initialAction)
                },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
