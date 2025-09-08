package io.composeflow.ui.inspector.propertyeditor

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.Key.Companion.Escape
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.configure_icon
import io.composeflow.delete_icon
import io.composeflow.materialicons.Filled
import io.composeflow.materialicons.ImageVectorHolder
import io.composeflow.materialicons.MaterialIcon
import io.composeflow.materialicons.Outlined
import io.composeflow.materialicons.Rounded
import io.composeflow.materialicons.Sharp
import io.composeflow.materialicons.TwoTone
import io.composeflow.select_icon
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.labeledbox.LabeledBorderBox
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.propertyeditor.propertyEditorMinWidth
import io.composeflow.ui.textfield.SmallOutlinedTextField
import org.jetbrains.compose.resources.stringResource

@Composable
fun IconPropertyEditor(
    label: String,
    onIconSelected: (ImageVectorHolder) -> Unit,
    modifier: Modifier = Modifier,
    onIconDeleted: (() -> Unit)? = null,
    currentIcon: ImageVector? = null,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .wrapContentHeight()
                .clickable {
                    dialogOpen = true
                },
    ) {
        LabeledBorderBox(
            label = label,
            modifier = Modifier.width(propertyEditorMinWidth),
        ) {
            Row {
                ComposeFlowIconButton(
                    onClick = {
                        dialogOpen = true
                    },
                ) {
                    ComposeFlowIcon(
                        imageVector = currentIcon ?: Icons.Outlined.Rectangle,
                        contentDescription = stringResource(Res.string.configure_icon),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.weight(1f))
                if (currentIcon != null && onIconDeleted != null) {
                    ComposeFlowIconButton(
                        onClick = {
                            onIconDeleted()
                        },
                    ) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(Res.string.delete_icon),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (dialogOpen) {
        onAnyDialogIsShown()
        val onCloseDialog = {
            dialogOpen = false
            onAllDialogsClosed()
        }
        PositionCustomizablePopup(
            onDismissRequest = {
                onCloseDialog()
            },
            onKeyEvent = {
                if (it.key == Escape) {
                    onCloseDialog()
                    true
                } else {
                    false
                }
            },
        ) {
            var selectedIcon: ImageVectorHolder? by remember { mutableStateOf(null) }
            Surface(
                modifier =
                    Modifier
                        .size(width = 800.dp, height = 680.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(all = 16.dp)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter) {
                                    selectedIcon?.let {
                                        onIconSelected(it)
                                        onCloseDialog()
                                        true
                                    } ?: false
                                } else {
                                    false
                                }
                            },
                ) {
                    Text(
                        text = stringResource(Res.string.select_icon),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    var filterText by remember { mutableStateOf("") }
                    val focusRequester = remember { FocusRequester() }
                    var filter by remember { mutableStateOf(MaterialIcon.Outlined) }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    SmallOutlinedTextField(
                        value = filterText,
                        onValueChange = {
                            filterText = it
                        },
                        leadingIcon = {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
                        modifier =
                            Modifier
                                .padding(bottom = 16.dp)
                                .focusable()
                                .focusRequester(focusRequester),
                    )

                    @Composable
                    fun runFilterChip(materialIcon: MaterialIcon) =
                        run {
                            val selected = filter == materialIcon
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    filter = materialIcon
                                },
                                label = {
                                    Text(materialIcon.name)
                                },
                                leadingIcon = {
                                    Box(
                                        Modifier.animateContentSize(keyframes { durationMillis = 200 }),
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        }
                    FlowRow {
                        MaterialIcon.entries.forEach {
                            runFilterChip(it)
                        }
                    }
                    val icons =
                        when (filter) {
                            MaterialIcon.Outlined -> Outlined.entries.toTypedArray()
                            MaterialIcon.Filled -> Filled.entries.toTypedArray()
                            MaterialIcon.Rounded -> Rounded.entries.toTypedArray()
                            MaterialIcon.Sharp -> Sharp.entries.toTypedArray()
                            MaterialIcon.TwoTone -> TwoTone.entries.toTypedArray()
                        }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(104.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(
                            icons.filter { it.name.contains(filterText, ignoreCase = true) },
                        ) {
                            val selected = selectedIcon == it
                            val selectedModifier =
                                Modifier
                                    .clip(shape = RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .hoverOverlay()
                                        .width(98.dp)
                                        .height(78.dp)
                                        .onClick {
                                            selectedIcon =
                                                when (filter) {
                                                    MaterialIcon.Outlined -> it as Outlined
                                                    MaterialIcon.Filled -> it as Filled
                                                    MaterialIcon.Rounded -> it as Rounded
                                                    MaterialIcon.Sharp -> it as Sharp
                                                    MaterialIcon.TwoTone -> it as TwoTone
                                                }
                                        }.then(
                                            if (selected) {
                                                selectedModifier
                                            } else {
                                                Modifier
                                            },
                                        ),
                            ) {
                                Image(
                                    imageVector = it.imageVector,
                                    contentDescription = "Icon for ${it.name}",
                                    contentScale = ContentScale.FillWidth,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                                    modifier =
                                        Modifier
                                            .width(42.dp)
                                            .padding(4.dp),
                                )

                                Text(
                                    text = it.name,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Visible,
                                    softWrap = true,
                                    modifier =
                                        Modifier
                                            .padding(4.dp),
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                onCloseDialog()
                            },
                            modifier = Modifier.padding(end = 16.dp),
                        ) {
                            Text("Cancel")
                        }
                        OutlinedButton(
                            onClick = {
                                selectedIcon?.let {
                                    onIconSelected(it)
                                }
                                onCloseDialog()
                            },
                            enabled = selectedIcon != null,
                            modifier = Modifier.padding(end = 16.dp),
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}
