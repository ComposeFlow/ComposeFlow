package io.composeflow.ui.datatype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_enum
import io.composeflow.add_enum_entry
import io.composeflow.delete_data_type
import io.composeflow.delete_enum_value
import io.composeflow.edit_value
import io.composeflow.editor.validator.KotlinVariableNameValidator
import io.composeflow.enum
import io.composeflow.enum_tooltip
import io.composeflow.model.project.Project
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.popup.SingleTextInputDialog
import io.composeflow.ui.reorderable.ComposeFlowReorderableItem
import io.composeflow.value
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EnumListHeader(
    onEnumAdded: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nameDialogOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.enum),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val contentDesc = stringResource(Res.string.enum_tooltip)
            Tooltip(contentDesc) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = contentDesc,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .size(18.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))
        val addDataType = stringResource(Res.string.add_enum)
        Tooltip(addDataType) {
            ComposeFlowIconButton(
                onClick = {
                    nameDialogOpen = true
                },
                modifier =
                    Modifier
                        .hoverIconClickable()
                        .hoverOverlay(),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = addDataType,
                )
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogClosed = LocalOnAllDialogsClosed.current
    if (nameDialogOpen) {
        onAnyDialogIsShown()
        val dialogClosed = {
            onAllDialogClosed()
            nameDialogOpen = false
        }
        NewNameDialog(
            label = stringResource(Res.string.add_enum),
            onCloseClick = {
                dialogClosed()
            },
            onNameConfirmed = {
                onEnumAdded(it)
                dialogClosed()
            },
        )
    }
}

@Composable
fun EnumList(
    project: Project,
    enumFocusedIndex: Int?,
    onFocusedEnumIndexUpdated: (Int) -> Unit,
) {
    Column(
        Modifier
            .padding(16.dp),
    ) {
        val dataTypes = project.customEnumHolder.enumList
        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            itemsIndexed(dataTypes) { i, enum ->
                val focusedModifier =
                    if (i == enumFocusedIndex) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(
                                    alpha = 0.8f,
                                ),
                            )
                    } else {
                        Modifier.alpha(0.4f)
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .hoverIconClickable()
                            .then(focusedModifier)
                            .clickable {
                                onFocusedEnumIndexUpdated(i)
                            },
                ) {
                    Text(
                        enum.enumName,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnumDetailContentHeader() {
    Column {
        Row {
            Text(
                stringResource(Res.string.value),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.width(240.dp),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun EnumDetail(
    project: Project,
    focusedEnumIndex: Int?,
    onEnumValueAdded: (String) -> Unit,
    onEnumValueUpdated: (Int, String) -> Unit,
    onDeleteEnumIconClicked: () -> Unit,
    onDeleteEnumValueOfIndex: (Int) -> Unit,
    onSwapEnumValueIndexes: (Int, Int) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .backgroundContainerNeutral()
                .padding(16.dp),
    ) {
        Spacer(Modifier.weight(1f))
        EnumDetailContent(
            project = project,
            focusedEnumIndex = focusedEnumIndex,
            onEnumValueAdded = onEnumValueAdded,
            onEnumValueUpdated = onEnumValueUpdated,
            onDeleteEnumIconClicked = onDeleteEnumIconClicked,
            onDeleteEnumValueOfIndex = onDeleteEnumValueOfIndex,
            onSwapEnumValueIndexes = onSwapEnumValueIndexes,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun EnumDetailContent(
    project: Project,
    focusedEnumIndex: Int?,
    onEnumValueAdded: (String) -> Unit,
    onEnumValueUpdated: (Int, String) -> Unit,
    onDeleteEnumIconClicked: () -> Unit,
    onDeleteEnumValueOfIndex: (Int) -> Unit,
    onSwapEnumValueIndexes: (Int, Int) -> Unit,
) {
    var addEnumValueDialogOpen by remember { mutableStateOf(false) }
    var indexOfEnumToBeEdited by remember { mutableStateOf<Int?>(null) }
    var indexOfEnumToBeDeleted by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier =
            Modifier
                .width(960.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surface),
    ) {
        val enum =
            focusedEnumIndex?.let { project.customEnumHolder.enumList[it] }
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            enum?.let {
                Row {
                    Text(
                        it.enumName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp).padding(bottom = 16.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    val contentDesc = stringResource(Res.string.delete_data_type)
                    Tooltip(contentDesc) {
                        IconButton(onClick = {
                            onDeleteEnumIconClicked()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                val lazyListState = rememberLazyListState()
                val reorderableLazyListState =
                    rememberReorderableLazyListState(lazyListState) { from, to ->
                        onSwapEnumValueIndexes(from.index, to.index)
                    }
                EnumDetailContentHeader()
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.heightIn(max = 800.dp),
                ) {
                    itemsIndexed(enum.values, key = { _, value -> value }) { i, value ->
                        ComposeFlowReorderableItem(
                            index = i,
                            reorderableLazyListState = reorderableLazyListState,
                            key = value,
                        ) {
                            EnumFieldRow(
                                value = value,
                                index = i,
                                onEditEnumValueDialogOpen = {
                                    indexOfEnumToBeEdited = i
                                    addEnumValueDialogOpen = true
                                },
                                onDeleteEnumDialogOpen = { index ->
                                    indexOfEnumToBeDeleted = index
                                },
                                modifier = Modifier.draggableHandle(),
                            )
                        }
                    }
                }
                TextButton(
                    onClick = {
                        addEnumValueDialogOpen = true
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("+ ${stringResource(Res.string.add_enum_entry)}")
                }
            }
        }

        val onAnyDialogIsOpen = LocalOnAnyDialogIsShown.current
        val onAllDialogsClosed = LocalOnAllDialogsClosed.current
        if (addEnumValueDialogOpen) {
            onAnyDialogIsOpen()
            val dialogClosed = {
                addEnumValueDialogOpen = false
                onAllDialogsClosed()
            }
            val initialValue =
                indexOfEnumToBeEdited?.let {
                    enum?.values?.get(it)
                }

            SingleTextInputDialog(
                textLabel = "Enum value",
                onDismissDialog = {
                    dialogClosed()
                },
                onTextConfirmed = {
                    indexOfEnumToBeEdited?.let { enumIndex ->
                        onEnumValueUpdated(enumIndex, it)
                        indexOfEnumToBeEdited = null
                    } ?: onEnumValueAdded(it)

                    dialogClosed()
                },
                initialValue = initialValue,
                validator = KotlinVariableNameValidator()::validate,
            )
        }

        indexOfEnumToBeDeleted?.let { indexToBeDeleted ->
            onAnyDialogIsOpen()
            val closeDialog = {
                indexOfEnumToBeDeleted = null
                onAllDialogsClosed()
            }
            SimpleConfirmationDialog(
                text = stringResource(Res.string.delete_enum_value) + "?",
                onConfirmClick = {
                    onDeleteEnumValueOfIndex(indexToBeDeleted)
                    closeDialog()
                },
                onCloseClick = {
                    closeDialog()
                },
            )
        }
    }
}

@Composable
private fun EnumFieldRow(
    value: String,
    index: Int,
    onEditEnumValueDialogOpen: (Int) -> Unit,
    onDeleteEnumDialogOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(240.dp),
            )

            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = "",
            )
            IconButton(
                onClick = {
                    onEditEnumValueDialogOpen(index)
                },
            ) {
                val contentDesc = stringResource(Res.string.edit_value)
                Tooltip(contentDesc) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = contentDesc,
                    )
                }
            }
            IconButton(
                onClick = {
                    onDeleteEnumDialogOpen(index)
                },
            ) {
                val contentDesc = stringResource(Res.string.delete_enum_value)
                Tooltip(contentDesc) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier =
                Modifier
                    .padding(vertical = 8.dp),
        )
    }
}
