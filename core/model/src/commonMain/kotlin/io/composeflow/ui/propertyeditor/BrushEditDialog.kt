package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.brush_type
import io.composeflow.center_x
import io.composeflow.center_y
import io.composeflow.edit_brush
import io.composeflow.end_x
import io.composeflow.end_y
import io.composeflow.model.parameter.wrapper.BrushType
import io.composeflow.model.parameter.wrapper.BrushWrapper
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.TileModeWrapper
import io.composeflow.radius
import io.composeflow.start_x
import io.composeflow.start_y
import io.composeflow.tile_mode
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.reorderable.ComposeFlowReorderableItem
import io.composeflow.ui.textfield.DropdownMenuTextField
import io.composeflow.ui.textfield.SmallOutlinedTextField
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun BrushEditDialog(
    initialBrush: BrushWrapper,
    onDismissRequest: () -> Unit,
    onConfirm: (BrushWrapper) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editedBrush by remember { mutableStateOf(initialBrush) }

    PositionCustomizablePopup(
        onDismissRequest = onDismissRequest,
        onKeyEvent = { keyEvent ->
            if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown) {
                onDismissRequest()
                true
            } else {
                false
            }
        },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.size(width = 600.dp, height = 820.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.edit_brush),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column {
                    DropdownMenuTextField(
                        items = BrushType.entries.map { it.name },
                        label = stringResource(Res.string.brush_type),
                        onValueChange = { brushType ->
                            editedBrush =
                                editedBrush.copy(brushType = BrushType.valueOf(brushType))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        selectedItem = editedBrush.brushType.name,
                        enabled = true,
                    )
                }

                // Tile Mode Selection
                Column {
                    val tileModeOptions =
                        listOf(
                            "None" to null,
                            TileModeWrapper.Clamp.name to TileModeWrapper.Clamp,
                            TileModeWrapper.Repeated.name to TileModeWrapper.Repeated,
                            TileModeWrapper.Mirror.name to TileModeWrapper.Mirror,
                            TileModeWrapper.Decal.name to TileModeWrapper.Decal,
                        )

                    val currentTileModeName =
                        when (editedBrush.tileMode) {
                            TileModeWrapper.Clamp -> TileModeWrapper.Clamp.name
                            TileModeWrapper.Repeated -> TileModeWrapper.Repeated.name
                            TileModeWrapper.Mirror -> TileModeWrapper.Mirror.name
                            TileModeWrapper.Decal -> TileModeWrapper.Decal.name
                            null -> "None"
                        }

                    DropdownMenuTextField(
                        items = tileModeOptions.map { it.first },
                        label = stringResource(Res.string.tile_mode),
                        onValueChange = { tileModeName ->
                            val selectedTileMode =
                                tileModeOptions.find { it.first == tileModeName }?.second
                            editedBrush = editedBrush.copy(tileMode = selectedTileMode)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        selectedItem = currentTileModeName,
                        enabled = true,
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Colors",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        ComposeFlowIconButton(
                            onClick = {
                                editedBrush =
                                    editedBrush.copy(
                                        colors = editedBrush.colors + ColorWrapper(color = Color.Red),
                                    )
                            },
                        ) {
                            ComposeFlowIcon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Color",
                            )
                        }
                    }

                    val lazyListState = rememberLazyListState()
                    val reorderableLazyListState =
                        rememberReorderableLazyListState(lazyListState) { from, to ->
                            val updatedColors = editedBrush.colors.toMutableList()
                            val item = updatedColors.removeAt(from.index)
                            updatedColors.add(to.index, item)
                            editedBrush = editedBrush.copy(colors = updatedColors)
                        }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(
                            editedBrush.colors,
                            key = { index, color -> color },
                        ) { index, colorWrapper ->
                            ComposeFlowReorderableItem(
                                index = index,
                                reorderableLazyListState = reorderableLazyListState,
                                key = colorWrapper,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ColorPropertyEditor(
                                        label = "", // Empty label since we're in a list
                                        initialColor = colorWrapper,
                                        onColorUpdated = { newColor ->
                                            val updatedColors =
                                                editedBrush.colors.toMutableList()
                                            updatedColors[index] =
                                                ColorWrapper(
                                                    color = newColor,
                                                    themeColor = null,
                                                )
                                            editedBrush =
                                                editedBrush.copy(colors = updatedColors)
                                        },
                                        onThemeColorSelected = { themeColor ->
                                            val updatedColors =
                                                editedBrush.colors.toMutableList()
                                            updatedColors[index] =
                                                ColorWrapper(
                                                    color = null,
                                                    themeColor = themeColor,
                                                )
                                            editedBrush =
                                                editedBrush.copy(colors = updatedColors)
                                        },
                                        modifier = Modifier.weight(1f),
                                        includeThemeColor = true,
                                    )

                                    ComposeFlowIcon(
                                        imageVector = Icons.Outlined.DragIndicator,
                                        contentDescription = "Drag to reorder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.draggableHandle(),
                                    )

                                    ComposeFlowIconButton(
                                        onClick = {
                                            if (editedBrush.colors.size > 1) {
                                                val updatedColors =
                                                    editedBrush.colors.toMutableList()
                                                updatedColors.removeAt(index)
                                                editedBrush =
                                                    editedBrush.copy(colors = updatedColors)
                                            }
                                        },
                                        enabled = editedBrush.colors.size > 1,
                                    ) {
                                        ComposeFlowIcon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete Color",
                                            tint =
                                                if (editedBrush.colors.size > 1) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.38f,
                                                    )
                                                },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Brush Parameters Section
                BrushParametersSection(
                    brush = editedBrush,
                    onBrushChange = { editedBrush = it },
                )

                BrushPreview(
                    brush = editedBrush,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onConfirm(editedBrush) },
                        enabled = editedBrush.colors.isNotEmpty(),
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun BrushParametersSection(
    brush: BrushWrapper,
    onBrushChange: (BrushWrapper) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Parameters",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        when (brush.brushType) {
            BrushType.LinearGradient -> {
                LinearGradientParameters(
                    brush = brush,
                    onBrushChange = onBrushChange,
                )
            }

            BrushType.HorizontalGradient -> {
                Text(
                    text = "No additional parameters needed for horizontal gradients",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            BrushType.VerticalGradient -> {
                Text(
                    text = "No additional parameters needed for vertical gradients",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            BrushType.RadialGradient -> {
                RadialGradientParameters(
                    brush = brush,
                    onBrushChange = onBrushChange,
                )
            }

            BrushType.SweepGradient -> {
                SweepGradientParameters(
                    brush = brush,
                    onBrushChange = onBrushChange,
                )
            }
        }
    }
}

@Composable
private fun LinearGradientParameters(
    brush: BrushWrapper,
    onBrushChange: (BrushWrapper) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FloatField(
            label = stringResource(Res.string.start_x),
            value = brush.startX,
            onValueChange = { onBrushChange(brush.copy(startX = it)) },
            modifier = Modifier.weight(1f),
        )
        FloatField(
            label = stringResource(Res.string.start_y),
            value = brush.startY,
            onValueChange = { onBrushChange(brush.copy(startY = it)) },
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FloatField(
            label = stringResource(Res.string.end_x),
            value = brush.endX,
            onValueChange = { onBrushChange(brush.copy(endX = it)) },
            modifier = Modifier.weight(1f),
        )
        FloatField(
            label = stringResource(Res.string.end_y),
            value = brush.endY,
            onValueChange = { onBrushChange(brush.copy(endY = it)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RadialGradientParameters(
    brush: BrushWrapper,
    onBrushChange: (BrushWrapper) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FloatField(
            label = stringResource(Res.string.center_x),
            value = brush.centerX,
            onValueChange = { onBrushChange(brush.copy(centerX = it)) },
            modifier = Modifier.weight(1f),
        )
        FloatField(
            label = stringResource(Res.string.center_y),
            value = brush.centerY,
            onValueChange = { onBrushChange(brush.copy(centerY = it)) },
            modifier = Modifier.weight(1f),
        )
    }

    FloatField(
        label = stringResource(Res.string.radius),
        value = brush.radius,
        onValueChange = { onBrushChange(brush.copy(radius = it)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SweepGradientParameters(
    brush: BrushWrapper,
    onBrushChange: (BrushWrapper) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FloatField(
            label = stringResource(Res.string.center_x),
            value = brush.centerX,
            onValueChange = { onBrushChange(brush.copy(centerX = it)) },
            modifier = Modifier.weight(1f),
        )
        FloatField(
            label = stringResource(Res.string.center_y),
            value = brush.centerY,
            onValueChange = { onBrushChange(brush.copy(centerY = it)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FloatField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var useInfinity by remember(value) { mutableStateOf(value.isInfinite()) }
    var textValue by remember(value) {
        mutableStateOf(
            if (value.isInfinite()) "" else value.toString(),
        )
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SmallOutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    if (!useInfinity) {
                        newValue.toFloatOrNull()?.let { onValueChange(it) }
                    }
                },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !useInfinity,
                placeholder = {
                    if (useInfinity) {
                        Text("∞", style = MaterialTheme.typography.bodyMedium)
                    }
                },
            )

            // Infinity toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                androidx.compose.material3.Checkbox(
                    checked = useInfinity,
                    onCheckedChange = { checked ->
                        useInfinity = checked
                        if (checked) {
                            onValueChange(Float.POSITIVE_INFINITY)
                        } else {
                            // Set to a reasonable default when unchecking infinity
                            val defaultValue = textValue.toFloatOrNull() ?: 1f
                            onValueChange(defaultValue)
                        }
                    },
                )
                Text(
                    text = "∞",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (useInfinity) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
    }
}

@Composable
private fun BrushPreview(
    brush: BrushWrapper,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush.getBrush() ?: androidx.compose.ui.graphics.Brush
                        .linearGradient(listOf(Color.Transparent)),
                ).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp),
                ),
    )
}
