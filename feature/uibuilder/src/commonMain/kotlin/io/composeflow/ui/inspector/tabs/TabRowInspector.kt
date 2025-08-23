package io.composeflow.ui.inspector.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_tab
import io.composeflow.delete_tab
import io.composeflow.edit
import io.composeflow.model.parameter.TabTrait
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.reorderable.ComposeFlowReorderableItem
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TabRowInspector(
    project: Project,
    node: ComposeNode,
) {
    var openEditTabRowDialog by remember { mutableStateOf(false) }
    Column {
        val tabs = node.children
        val tabContainer = node.parentNode
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tabs",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            ComposeFlowIconButton(
                onClick = {
                    tabContainer?.addTab()
                },
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .hoverOverlay()
                        .hoverIconClickable(),
            ) {
                val contentDesc = stringResource(Res.string.add_tab)
                Tooltip(contentDesc) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            ComposeFlowIconButton(
                onClick = {
                    openEditTabRowDialog = true
                },
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .hoverOverlay()
                        .hoverIconClickable(),
            ) {
                val contentDesc = stringResource(Res.string.edit)
                Tooltip(contentDesc) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = contentDesc,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        tabs.forEachIndexed { i, tab ->
            val tabTrait = tab.trait.value as TabTrait
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .hoverOverlay()
                        .fillMaxWidth()
                        .height(42.dp)
                        .padding(start = 16.dp),
            ) {
                Text(
                    text = tabTrait.text?.displayText(project) ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )

                if (tabs.size > 1) {
                    Spacer(Modifier.weight(1f))
                    ComposeFlowIconButton(
                        onClick = {
                            tabContainer?.removeTab(i)
                        },
                        modifier = Modifier.hoverIconClickable(),
                    ) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(Res.string.delete_tab),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (openEditTabRowDialog) {
        EditTabRowDialog(
            project = project,
            node = node,
            onCloseDialog = {
                openEditTabRowDialog = false
            },
        )
    }
}

/**
 * Dialog to edit the tabs in the TabRow.
 * This needs to be in a dedicated LazyColumn, thus extracting it as a separate composable
 * function to handle the tab editing functionality.
 */
@Composable
private fun EditTabRowDialog(
    project: Project,
    node: ComposeNode,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PositionCustomizablePopup(
        onDismissRequest = onCloseDialog,
    ) {
        Surface(
            modifier = modifier.size(width = 420.dp, height = 460.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                val tabs = node.children
                val tabContainer = node.parentNode
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tabs",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    ComposeFlowIconButton(
                        onClick = {
                            tabContainer?.addTab()
                        },
                        modifier =
                            Modifier
                                .padding(start = 16.dp)
                                .hoverOverlay()
                                .hoverIconClickable(),
                    ) {
                        val contentDesc = stringResource(Res.string.add_tab)
                        Tooltip(contentDesc) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                val lazyListState = rememberLazyListState()
                val reorderableLazyListState =
                    rememberReorderableLazyListState(lazyListState) { from, to ->
                        tabContainer?.swapTabIndex(from.index, to.index)
                    }
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier,
                ) {
                    itemsIndexed(tabs, key = { _, tab -> tab }) { i, tab ->
                        ReorderableItem(reorderableLazyListState, key = tab) { isDragging ->
                            val tabTrait = tab.trait.value as TabTrait
                            ComposeFlowReorderableItem(
                                index = i,
                                reorderableLazyListState = reorderableLazyListState,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .hoverOverlay()
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .padding(start = 16.dp),
                                ) {
                                    Text(
                                        text = tabTrait.text?.displayText(project) ?: "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )

                                    if (tabs.size > 1) {
                                        Spacer(Modifier.weight(1f))
                                        ComposeFlowIcon(
                                            imageVector = Icons.Outlined.DragIndicator,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.draggableHandle(),
                                        )
                                        ComposeFlowIconButton(
                                            onClick = {
                                                tabContainer?.removeTab(i)
                                            },
                                            modifier = Modifier.hoverIconClickable(),
                                        ) {
                                            ComposeFlowIcon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = stringResource(Res.string.delete_tab),
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
