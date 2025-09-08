package io.composeflow.ui.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import io.composeflow.ui.dropdown.PlatformCursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_new_modifier
import io.composeflow.bring_to_front
import io.composeflow.component_description
import io.composeflow.convert_to_component
import io.composeflow.copy
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.ComposeLogo
import io.composeflow.delete
import io.composeflow.double_click
import io.composeflow.edit_component
import io.composeflow.keyboard.getCtrlKeyStr
import io.composeflow.keyboard.getDeleteKeyStr
import io.composeflow.model.palette.TraitCategory
import io.composeflow.model.parameter.ComponentTrait
import io.composeflow.model.parameter.ComposeTrait
import io.composeflow.model.parameter.entries
import io.composeflow.model.project.CanvasEditable
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.model.project.findComponentOrThrow
import io.composeflow.paste
import io.composeflow.redo
import io.composeflow.select_parent_composable
import io.composeflow.send_to_back
import io.composeflow.ui.CanvasNodeCallbacks
import io.composeflow.ui.Tooltip
import io.composeflow.ui.handleMessages
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.mousePointerEvents
import io.composeflow.undo
import io.composeflow.wrap_with
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiBuilderContextMenuDropDown(
    project: Project,
    composeNodeCallbacks: ComposeNodeCallbacks,
    canvasNodeCallbacks: CanvasNodeCallbacks,
    copiedNodes: List<ComposeNode>,
    currentEditable: CanvasEditable,
    onAddModifier: () -> Unit,
    onCloseMenu: () -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onFocusedStatusUpdated: (ComposeNode) -> Unit,
    onHoveredStatusUpdated: (ComposeNode, Boolean) -> Unit,
    onOpenConvertToComponentDialog: (ComposeNode) -> Unit,
    coroutineScope: CoroutineScope,
) {
    var wrapWithMenuExpanded by remember { mutableStateOf(false) }
    val focusedNodes = currentEditable.findFocusedNodes()
    PlatformCursorDropdownMenu(
        expanded = true,
        onDismissRequest = {
            onCloseMenu()
        },
        modifier =
            Modifier
                .width(280.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(Res.string.undo),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "(${getCtrlKeyStr()} + Z)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        }, onClick = {
            canvasNodeCallbacks.onUndo()
            onCloseMenu()
        })
        DropdownMenuItem(text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(Res.string.redo),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "(${getCtrlKeyStr()} + Y)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        }, onClick = {
            canvasNodeCallbacks.onRedo()
            onCloseMenu()
        })

        if (
            focusedNodes.size == 1 &&
            focusedNodes.firstOrNull()?.isContentRoot() == false &&
            focusedNodes
                .first()
                .trait.value
                .isEditable()
        ) {
            ContextMenuEditItems(
                canvasNodeCallbacks = canvasNodeCallbacks,
                onAddModifier = onAddModifier,
                onShowSnackbar = onShowSnackbar,
                coroutineScope = coroutineScope,
                onCloseMenu = onCloseMenu,
                copiedNodes = copiedNodes,
            )
        }

        if (
            focusedNodes.size == 1 &&
            focusedNodes.firstOrNull()?.isRoot() == false &&
            !focusedNodes.first().isContentRoot() &&
            focusedNodes.first().trait.value !is ComponentTrait &&
            focusedNodes
                .first()
                .trait.value
                .isEditable() &&
            TraitCategory.ScreenOnly !in
            focusedNodes
                .first()
                .trait.value
                .paletteCategories()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComposeFlowIcon(
                        imageVector = ComposeFlowIcons.ComposeLogo,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(Res.string.convert_to_component),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    val componentDescription = stringResource(Res.string.component_description)
                    Tooltip(componentDescription) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = componentDescription,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }, onClick = {
                onOpenConvertToComponentDialog(focusedNodes.first())
                onCloseMenu()
            })
        } else if (focusedNodes.size == 1 &&
            focusedNodes.firstOrNull()?.trait?.value is ComponentTrait
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            check(focusedNodes.first().componentId != null)
            val component = project.findComponentOrThrow(focusedNodes.first().componentId!!)
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComposeFlowIcon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(Res.string.edit_component),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "(${stringResource(Res.string.double_click)})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.alpha(0.6f),
                    )
                }
            }, onClick = {
                composeNodeCallbacks.onEditComponent(component)
                onCloseMenu()
            })
        }

        if (
            focusedNodes.size == 1 &&
            focusedNodes.firstOrNull()?.isRoot() == false &&
            !focusedNodes.first().isContentRoot() &&
            focusedNodes
                .first()
                .trait.value
                .isEditable() &&
            !focusedNodes.first().isContentRoot() &&
            TraitCategory.ScreenOnly !in
            focusedNodes
                .first()
                .trait.value
                .paletteCategories()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.AspectRatio,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(Res.string.wrap_with),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .onPointerEvent(PointerEventType.Enter) {
                                    wrapWithMenuExpanded = true
                                },
                    )
                }
            }, onClick = {
                wrapWithMenuExpanded = true
            })

            if (wrapWithMenuExpanded) {
                PlatformCursorDropdownMenu(
                    expanded = true,
                    onDismissRequest = {
                        wrapWithMenuExpanded = false
                    },
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    ComposeTrait.entries
                        .filter { TraitCategory.WrapContainer in it.paletteCategories() }
                        .forEach { container ->
                            DropdownMenuItem(
                                text = {
                                    Row {
                                        ComposeFlowIcon(
                                            imageVector = container.icon(),
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = container.iconText(),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(start = 8.dp),
                                        )
                                    }
                                },
                                onClick = {
                                    composeNodeCallbacks.onWrapWithContainerComposable(
                                        focusedNodes.first(),
                                        container,
                                    )
                                    onCloseMenu()
                                },
                            )
                        }
                }
            }
        }

        if (focusedNodes.size == 1) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            )
            Text(
                text = stringResource(Res.string.select_parent_composable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 12.dp, top = 8.dp),
            )

            focusedNodes.first().findNodesUntilRoot().forEach {
                DropdownMenuItem(text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.mousePointerEvents(
                                node = it,
                                onFocusedStatusUpdated = onFocusedStatusUpdated,
                                onHoveredStatusUpdated = onHoveredStatusUpdated,
                            ),
                    ) {
                        Icon(
                            imageVector = it.trait.value.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = it.displayName(project),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }, onClick = {
                    onCloseMenu()
                })
            }
        }
    }
}

@Composable
private fun ContextMenuEditItems(
    canvasNodeCallbacks: CanvasNodeCallbacks,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    coroutineScope: CoroutineScope,
    onAddModifier: () -> Unit,
    onCloseMenu: () -> Unit,
    copiedNodes: List<ComposeNode>,
) {
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.copy),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "(${getCtrlKeyStr()} + C)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.6f),
            )
        }
    }, onClick = {
        val eventResult = canvasNodeCallbacks.onCopyFocusedNode()
        eventResult.handleMessages(onShowSnackbar, coroutineScope)
        onCloseMenu()
    })
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ContentPasteGo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(Res.string.paste),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "(${getCtrlKeyStr()} + V)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        },
        enabled = copiedNodes.isNotEmpty(),
        onClick = {
            val eventResult = canvasNodeCallbacks.onPaste()
            eventResult.handleMessages(onShowSnackbar, coroutineScope)
            onCloseMenu()
        },
    )
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.delete),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "(${getDeleteKeyStr()})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.alpha(0.6f),
            )
        }
    }, onClick = {
        val eventResult = canvasNodeCallbacks.onDeleteFocusedNode()
        eventResult.handleMessages(onShowSnackbar, coroutineScope)
        onCloseMenu()
    })

    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    )
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.add_new_modifier),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "(${getCtrlKeyStr()} + M)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.6f),
            )
        }
    }, onClick = {
        onAddModifier()
        onCloseMenu()
    })
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    )
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.bring_to_front),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
        }
    }, onClick = {
        canvasNodeCallbacks.onBringToFront()
        onCloseMenu()
    })
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(Res.string.send_to_back),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
            Spacer(Modifier.weight(1f))
        }
    }, onClick = {
        canvasNodeCallbacks.onSendToBack()
        onCloseMenu()
    })
}
