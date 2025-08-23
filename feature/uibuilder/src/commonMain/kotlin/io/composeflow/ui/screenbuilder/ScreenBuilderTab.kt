package io.composeflow.ui.screenbuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_screen
import io.composeflow.appears_in_navigation
import io.composeflow.cancel
import io.composeflow.copy_screen
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.Smartphone
import io.composeflow.delete
import io.composeflow.icon_in_navigation
import io.composeflow.label_in_navigation
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.Screen
import io.composeflow.modify_screen_name
import io.composeflow.screens
import io.composeflow.show_on_navigation
import io.composeflow.template.ScreenTemplatePair
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.inspector.propertyeditor.IconPropertyEditor
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.propertyeditor.BasicEditableTextProperty
import io.composeflow.ui.propertyeditor.BooleanPropertyEditor
import io.composeflow.ui.reorderable.ComposeFlowReorderableItem
import io.composeflow.ui.utils.TreeExpander
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ScreenBuilderTab(
    project: Project,
    onAddScreenFromTemplate: (name: String, screenTemplatePair: ScreenTemplatePair) -> Unit,
    onAddScreen: (screen: Screen) -> Unit,
    onSelectScreen: (screen: Screen) -> Unit,
    onScreenUpdated: (screen: Screen) -> Unit,
    onDeleteScreen: (screen: Screen) -> Unit,
    onCopyScreen: (screen: Screen) -> Unit,
    onScreensSwapped: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var screenToBeDeleted by remember { mutableStateOf<Screen?>(null) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
    ) {
        ScreensHeader(
            project = project,
            onAddScreenFromTemplatePair = onAddScreenFromTemplate,
            onAddScreen = onAddScreen,
        )

        val lazyListState = rememberLazyListState()
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                onScreensSwapped(from.index, to.index)
            }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier,
        ) {
            itemsIndexed(
                items = project.screenHolder.screens,
                key = { _, screen -> screen }) { i, screen ->
                ComposeFlowReorderableItem(
                    index = i,
                    reorderableLazyListState = reorderableLazyListState,
                    key = screen,
                ) {
                    val rowModifier =
                        if (project.screenHolder.currentEditable() == screen) {
                            Modifier
                        } else {
                            Modifier.alpha(
                                0.5f,
                            )
                        }
                    ScreenInfoPanel(
                        screen = screen,
                        numOfScreens = project.screenHolder.screens.size,
                        onDeleteClick = { screen ->
                            screenToBeDeleted = screen
                        },
                        onCopyScreen = onCopyScreen,
                        onSelectScreen = onSelectScreen,
                        onScreenUpdated = onScreenUpdated,
                        modifier = rowModifier,
                        reorderableScope = this,
                    )
                }
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    screenToBeDeleted?.let {
        onAnyDialogIsShown()

        DeleteScreenDialog(
            it,
            onCloseClick = {
                screenToBeDeleted = null
                onAllDialogsClosed()
            },
            onDeleteScreen = { screen ->
                onDeleteScreen(screen)
                screenToBeDeleted = null
                onAllDialogsClosed()
            },
        )
    }
}

@Composable
private fun ScreensHeader(
    project: Project,
    onAddScreenFromTemplatePair: (name: String, screenTemplatePair: ScreenTemplatePair) -> Unit,
    onAddScreen: (screen: Screen) -> Unit,
) {
    var addNewScreenDialogOpen by remember { mutableStateOf(false) }
    var screenTemplatePair by remember { mutableStateOf<ScreenTemplatePair?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.screens),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )

        val contentDesc = stringResource(Res.string.add_screen)
        Tooltip(contentDesc) {
            ComposeFlowIconButton(
                onClick = {
                    addNewScreenDialogOpen = true
                },
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .hoverOverlay(),
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Add,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.add_screen),
                )
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (addNewScreenDialogOpen) {
        onAnyDialogIsShown()
        SelectNewScreenDialog(
            project = project,
            onCloseClick = {
                addNewScreenDialogOpen = false
                onAllDialogsClosed()
            },
            onScreenTemplateSelected = {
                screenTemplatePair = it
            },
            onAddScreen = onAddScreen,
        )
    }
    screenTemplatePair?.let { pair ->
        onAnyDialogIsShown()
        ScreenNameDialog(
            initialName = pair.screen.name,
            onCloseClick = {
                screenTemplatePair = null
            },
            onNameConfirmed = {
                onAddScreenFromTemplatePair(it, pair)
                addNewScreenDialogOpen = false
                screenTemplatePair = null
                onAllDialogsClosed()
            },
        )
    }
}

@Composable
private fun ScreenInfoPanel(
    screen: Screen,
    numOfScreens: Int,
    onSelectScreen: (Screen) -> Unit,
    onDeleteClick: (Screen) -> Unit,
    onCopyScreen: (Screen) -> Unit,
    onScreenUpdated: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    reorderableScope: ReorderableCollectionItemScope? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        ScreenRowHeader(
            expanded = expanded,
            screen = screen,
            numOfScreens = numOfScreens,
            onExpandClick = {
                expanded = !expanded
            },
            onSelectScreen = onSelectScreen,
            onDeleteClick = onDeleteClick,
            onCopyScreen = onCopyScreen,
            onScreenUpdated = onScreenUpdated,
            modifier = modifier,
            reorderableScope = reorderableScope,
        )

        if (expanded) {
            Tooltip(stringResource(Res.string.appears_in_navigation)) {
                BooleanPropertyEditor(
                    checked = screen.showOnNavigation.value,
                    label = stringResource(Res.string.show_on_navigation),
                    onCheckedChange = {
                        screen.showOnNavigation.value = !screen.showOnNavigation.value
                        onScreenUpdated(screen)
                    },
                    modifier = Modifier.padding(start = 24.dp),
                )
            }

            if (screen.showOnNavigation.value) {
                Tooltip(stringResource(Res.string.icon_in_navigation)) {
                    IconPropertyEditor(
                        currentIcon = screen.icon.value.imageVector,
                        label = "Icon in navigation",
                        onIconSelected = {
                            screen.icon.value = it
                            onScreenUpdated(screen)
                        },
                        modifier = Modifier.padding(start = 24.dp),
                    )
                }

                Tooltip(stringResource(Res.string.label_in_navigation)) {
                    BasicEditableTextProperty(
                        initialValue = screen.label.value,
                        label = "Label in navigation",
                        onValidValueChanged = {
                            screen.label.value = it
                            onScreenUpdated(screen)
                        },
                        modifier = Modifier.padding(start = 24.dp).weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenRowHeader(
    expanded: Boolean,
    screen: Screen,
    numOfScreens: Int,
    onExpandClick: () -> Unit,
    onSelectScreen: (Screen) -> Unit,
    onDeleteClick: (Screen) -> Unit,
    onCopyScreen: (Screen) -> Unit,
    onScreenUpdated: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    reorderableScope: ReorderableCollectionItemScope? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onSelectScreen(screen)
                }.padding(end = 8.dp)
                .height(32.dp)
                .hoverIconClickable(),
    ) {
        TreeExpander(
            expanded = expanded,
            onClick = {
                onExpandClick()
            },
        )

        var menuExpanded by remember { mutableStateOf(false) }
        var showNameEditDialog by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = ComposeFlowIcons.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .padding(start = 4.dp)
                        .size(16.dp),
            )

            Text(
                text = screen.name,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.secondary,
                    ),
                modifier = Modifier.padding(start = 8.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            if (reorderableScope != null) {
                with(reorderableScope) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.DragIndicator,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null,
                        modifier = Modifier.draggableHandle(),
                    )
                }
            } else {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.DragIndicator,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                )
            }

            ComposeFlowIconButton(
                onClick = {
                    menuExpanded = true
                },
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "Screen options",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        CursorDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(4.dp),
            ) {
                Column {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.modify_screen_name),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            showNameEditDialog = true
                            menuExpanded = false
                        },
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.copy_screen),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onCopyScreen(screen)
                            menuExpanded = false
                        },
                    )

                    if (numOfScreens > 1) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(Res.string.delete),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                onDeleteClick(screen)
                                menuExpanded = false
                            },
                        )
                    }
                }
            }
        }

        // Name edit dialog
        if (showNameEditDialog) {
            ScreenNameDialog(
                initialName = screen.name,
                onCloseClick = {
                    showNameEditDialog = false
                },
                onNameConfirmed = { newName ->
                    if (newName.isNotBlank()) {
                        val updatedScreen = screen.copy(name = newName)
                        updatedScreen.label.value = newName
                        onScreenUpdated(updatedScreen)
                    }
                    showNameEditDialog = false
                },
            )
        }
    }
}

@Composable
private fun DeleteScreenDialog(
    screen: Screen,
    onCloseClick: () -> Unit,
    onDeleteScreen: (Screen) -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = {
            onCloseClick()
        },
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        Surface {
            Column(
                modifier =
                    Modifier
                        .size(300.dp, 160.dp)
                        .padding(16.dp),
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append("Delete screen: ")
                            withStyle(
                                style =
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append(screen.name)
                            }
                            append(" ?")
                        },
                )

                Spacer(modifier = Modifier.weight(1f))
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
                    OutlinedButton(
                        onClick = {
                            onDeleteScreen(screen)
                        },
                        modifier =
                            Modifier
                                .padding(end = 16.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemedScreenBuilderTabPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        val mockProject =
            Project().apply {
                screenHolder.screens.addAll(
                    listOf(
                        Screen("HomeScreen", name = "Home"),
                        Screen("ProfileScreen", name = "Profile"),
                        Screen("SettingsScreen", name = "Settings"),
                    ),
                )
            }

        ScreenBuilderTab(
            project = mockProject,
            onAddScreenFromTemplate = { _, _ -> },
            onAddScreen = { },
            onSelectScreen = { },
            onScreenUpdated = { },
            onDeleteScreen = { },
            onCopyScreen = { },
            onScreensSwapped = { _, _ -> },
        )
    }
}

@Preview
@Composable
fun ScreenBuilderTabPreview_Light() {
    ThemedScreenBuilderTabPreview(useDarkTheme = false)
}

@Preview
@Composable
fun ScreenBuilderTabPreview_Dark() {
    ThemedScreenBuilderTabPreview(useDarkTheme = true)
}

@Composable
private fun ThemedDeleteScreenDialogPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        DeleteScreenDialog(
            screen = Screen("MyScreen", name = "MyScreen"),
            onCloseClick = {},
            onDeleteScreen = {},
        )
    }
}

@Preview
@Composable
fun DeleteScreenDialogPreview_Light() {
    ThemedDeleteScreenDialogPreview(useDarkTheme = false)
}

@Preview
@Composable
fun DeleteScreenDialogPreview_Dark() {
    ThemedDeleteScreenDialogPreview(useDarkTheme = true)
}
