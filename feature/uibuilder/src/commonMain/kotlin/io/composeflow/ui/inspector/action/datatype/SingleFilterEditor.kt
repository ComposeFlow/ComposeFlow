package io.composeflow.ui.inspector.action.datatype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.filter_replace_with_and_operator
import io.composeflow.filter_replace_with_or_operator
import io.composeflow.model.datatype.AndFilter
import io.composeflow.model.datatype.DataField
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.DocumentIdDropdownItem
import io.composeflow.model.datatype.FilterExpression
import io.composeflow.model.datatype.FilterFieldType
import io.composeflow.model.datatype.FilterOperator
import io.composeflow.model.datatype.OrFilter
import io.composeflow.model.datatype.SingleFilter
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.firebase.FirestoreCollection
import io.composeflow.model.property.DocumentIdProperty
import io.composeflow.remove
import io.composeflow.ui.dashedBorder
import io.composeflow.ui.dropdown.PlatformCursorDropdownMenu
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import io.composeflow.ui.propertyeditor.DropdownItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun SingleFilterEditor(
    project: Project,
    composeNode: ComposeNode,
    firestoreCollection: FirestoreCollection,
    dataType: DataType,
    filter: SingleFilter,
    onFilterChange: (FilterExpression) -> Unit,
    onRemoveFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overlayVisible by remember { mutableStateOf(false) }
    val overlayModifier =
        if (overlayVisible) {
            Modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
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
            Column(
                modifier =
                    Modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.filter_replace_with_and_operator),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }, onClick = {
                    onFilterChange(AndFilter(filters = listOf(filter)))
                    menuOpened = false
                })

                DropdownMenuItem(text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.filter_replace_with_or_operator),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }, onClick = {
                    onFilterChange(OrFilter(filters = listOf(filter)))
                    menuOpened = false
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
                    onRemoveFilter()
                    menuOpened = false
                })
            }
        }
    }

    Box(
        modifier =
            modifier
                .dashedBorder(
                    strokeWidth = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp),
                ).onPointerEvent(PointerEventType.Enter) {
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

        Column(modifier = Modifier.padding(8.dp)) {
            val selectedItem =
                when (val fieldType = filter.filterFieldType) {
                    is FilterFieldType.DataField -> {
                        dataType.findDataFieldOrNull(fieldType.dataFieldId)
                    }

                    is FilterFieldType.DocumentId ->
                        DocumentIdDropdownItem(
                            fieldType.firestoreCollectionId ?: firestoreCollection.id,
                        )
                }

            BasicDropdownPropertyEditor(
                project = project,
                items = dataType.dataFieldsDropdownItems(project),
                onValueChanged = { _, item ->
                    val itemChanged = selectedItem != item

                    val newFilter =
                        if (item is DocumentIdDropdownItem) {
                            if (itemChanged) {
                                filter.copy(
                                    filterFieldType = FilterFieldType.DocumentId(item.firestoreCollectionId),
                                    property = DocumentIdProperty.EmptyDocumentId,
                                )
                            } else {
                                filter.copy(filterFieldType = FilterFieldType.DocumentId(item.firestoreCollectionId))
                            }
                        } else if (item is DataField) {
                            if (itemChanged) {
                                val dataField = dataType.findDataFieldOrNull(item.id)
                                filter.copy(
                                    filterFieldType =
                                        FilterFieldType.DataField(
                                            dataTypeId = dataType.id,
                                            dataFieldId = item.id,
                                        ),
                                    property =
                                        dataField?.fieldType?.type()?.defaultValue()
                                            ?: DocumentIdProperty.EmptyDocumentId,
                                )
                            } else {
                                filter.copy(
                                    filterFieldType =
                                        FilterFieldType.DataField(
                                            dataTypeId = dataType.id,
                                            dataFieldId = item.id,
                                        ),
                                )
                            }
                        } else {
                            throw UnsupportedOperationException("Invalid data field")
                        }

                    onFilterChange(newFilter)
                },
                selectedItem = selectedItem,
                label = "Field",
                displayText = {
                    DropdownDisplayText(
                        project = project,
                        it,
                    )
                },
                dropDownMenuText = {
                    DropdownDisplayText(
                        project = project,
                        it,
                    )
                },
            )

            BasicDropdownPropertyEditor(
                project = project,
                items = FilterOperator.entries,
                label = "Operator",
                onValueChanged = { _, item ->
                    onFilterChange(filter.copy(operator = item))
                },
                selectedItem = filter.operator,
            )

            when (val fieldType = filter.filterFieldType) {
                is FilterFieldType.DocumentId -> {
                    DocumentIdProperty
                        .FirestoreDocumentIdProperty(
                            fieldType.firestoreCollectionId ?: firestoreCollection.id,
                        ).Editor(
                            project = project,
                            node = composeNode,
                            initialProperty = filter.property,
                            label = "Value",
                            onValidPropertyChanged = { property, _ ->
                                onFilterChange(filter.copy(property = property))
                            },
                            onInitializeProperty = {
                                onFilterChange(filter.copy(property = DocumentIdProperty.EmptyDocumentId))
                            },
                            functionScopeProperties = emptyList(),
                            modifier = Modifier,
                            destinationStateId = null,
                            validateInput = null,
                            editable = true,
                        )
                }

                is FilterFieldType.DataField -> {
                    dataType.findDataFieldOrNull(fieldType.dataFieldId)?.let {
                        it.fieldType.type().defaultValue().Editor(
                            project = project,
                            node = composeNode,
                            initialProperty = filter.property,
                            label = "Value",
                            onValidPropertyChanged = { property, _ ->
                                onFilterChange(filter.copy(property = property))
                            },
                            onInitializeProperty = {
                                onFilterChange(
                                    filter.copy(
                                        property = it.fieldType.type().defaultValue(),
                                    ),
                                )
                            },
                            functionScopeProperties = emptyList(),
                            modifier = Modifier,
                            editable = true,
                            destinationStateId = null,
                            validateInput = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownDisplayText(
    project: Project,
    item: DropdownItem,
) {
    if (item is DocumentIdDropdownItem) {
        Text(
            item.asDropdownText(),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium,
        )
    } else if (item is DataField) {
        Row {
            Text(
                item.variableName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                item.fieldType.type().displayName(project),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
