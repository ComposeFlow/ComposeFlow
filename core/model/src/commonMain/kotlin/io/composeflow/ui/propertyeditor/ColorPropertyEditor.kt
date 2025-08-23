package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import io.composeflow.Res
import io.composeflow.cancel
import io.composeflow.confirm
import io.composeflow.delete_color
import io.composeflow.model.parameter.wrapper.ColorWrapper
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.select_color
import io.composeflow.serializer.asString
import io.composeflow.theme_colors
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.common.ComposeFlowTheme
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.labeledbox.LabeledBorderBox
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.textfield.SmallOutlinedTextField
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// Helper functions for hex color conversion
private fun Int.toHexString(): String = this.toString(16).padStart(2, '0').uppercase()

private fun Color.toHexString(includeAlpha: Boolean = true): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return if (includeAlpha && alpha < 255) {
        "#${alpha.toHexString()}${red.toHexString()}${green.toHexString()}${blue.toHexString()}"
    } else {
        "#${red.toHexString()}${green.toHexString()}${blue.toHexString()}"
    }
}

private fun String.toColorOrNull(): Color? =
    try {
        val hex = if (startsWith("#")) substring(1) else this

        when (hex.length) {
            6 -> {
                // RGB format
                val red = hex.substring(0, 2).toInt(16) / 255f
                val green = hex.substring(2, 4).toInt(16) / 255f
                val blue = hex.substring(4, 6).toInt(16) / 255f
                Color(red = red, green = green, blue = blue)
            }

            8 -> {
                // ARGB format
                val alpha = hex.substring(0, 2).toInt(16) / 255f
                val red = hex.substring(2, 4).toInt(16) / 255f
                val green = hex.substring(4, 6).toInt(16) / 255f
                val blue = hex.substring(6, 8).toInt(16) / 255f
                Color(red = red, green = green, blue = blue, alpha = alpha)
            }

            else -> null
        }
    } catch (e: Exception) {
        null
    }

@Composable
fun ColorPropertyEditor(
    label: String,
    initialColor: ColorWrapper?,
    onColorUpdated: (Color) -> Unit,
    onThemeColorSelected: (Material3ColorWrapper) -> Unit,
    modifier: Modifier = Modifier,
    onColorDeleted: (() -> Unit)? = null,
    includeThemeColor: Boolean = true,
) {
    var dialogOpen by remember { mutableStateOf(false) }

    @Composable
    fun ColorBox(color: Color) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(8.dp),
                    ).border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .wrapContentHeight()
                .hoverIconClickable()
                .clickable {
                    dialogOpen = true
                },
    ) {
        val colorExpression =
            if (initialColor == null) {
                "Unset"
            } else if (initialColor.themeColor != null) {
                initialColor.themeColor.colorName
            } else {
                initialColor.getColor()?.asString() ?: "Unspecified"
            }

        LabeledBorderBox(
            label = label.ifEmpty { "Color" },
            modifier = Modifier.weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorBox(color = initialColor?.getColor() ?: Color.Unspecified)
                Text(
                    text = colorExpression,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp),
                )
                if (initialColor != null && onColorDeleted != null) {
                    Spacer(Modifier.weight(1f))
                    ComposeFlowIconButton(
                        onClick = {
                            onColorDeleted()
                        },
                    ) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(Res.string.delete_color),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (dialogOpen) {
        onAnyDialogIsShown()
        ColorPropertyDialog(
            initialColor = initialColor,
            onThemeColorSelected = onThemeColorSelected,
            onColorUpdated = onColorUpdated,
            onCloseClick = {
                dialogOpen = false
                onAllDialogsClosed()
            },
            includeThemeColor = includeThemeColor,
        )
    }
}

@Composable
private fun ColorPropertyDialog(
    initialColor: ColorWrapper?,
    onThemeColorSelected: (Material3ColorWrapper) -> Unit,
    onColorUpdated: (Color) -> Unit,
    onCloseClick: () -> Unit,
    includeThemeColor: Boolean,
) {
    PositionCustomizablePopup(
        onDismissRequest = {
            onCloseClick()
        },
        onKeyEvent = { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        width = 800.dp,
                        height = if (includeThemeColor) 900.dp else 480.dp,
                    ),
        ) {
            ColorPropertyDialogContent(
                initialColor = initialColor,
                onThemeColorSelected = onThemeColorSelected,
                onCloseClick = onCloseClick,
                onColorUpdated = onColorUpdated,
                includeThemeColor = includeThemeColor,
            )
        }
    }
}

@Composable
fun ColorPropertyDialogContent(
    initialColor: ColorWrapper?,
    onThemeColorSelected: (Material3ColorWrapper) -> Unit,
    onCloseClick: () -> Unit,
    onColorUpdated: (Color) -> Unit,
    includeThemeColor: Boolean,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .size(width = 760.dp, height = if (includeThemeColor) 680.dp else 420.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(Res.string.select_color),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            val resolvedColor = initialColor?.getColor()
            val controller = rememberColorPickerController()

            // Set initial color only once
            remember(resolvedColor) {
                controller.selectByColor(resolvedColor ?: Color.Unspecified, fromUser = false)
            }

            // Observe the selected color from the controller
            val selectedColor by controller.selectedColor

            Column {
                Row {
                    HsvColorPicker(
                        controller = controller,
                        modifier =
                            Modifier
                                .width(400.dp)
                                .height(260.dp)
                                .padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
                    )

                    val previewColor = selectedColor.convert(ColorSpaces.Srgb)

                    ColorPreviewInfo(
                        color = previewColor,
                        onColorChanged = { newColor ->
                            controller.selectByColor(newColor, fromUser = true)
                        },
                    )
                }

                // Alpha slider
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Alpha",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AlphaSlider(
                            controller = controller,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .padding(end = 16.dp),
                        )

                        // Alpha tile to show the color with transparency
                        AlphaTile(
                            controller = controller,
                            modifier =
                                Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                        )
                    }
                }
            }

            if (includeThemeColor) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(148.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        },
                    ) {
                        Text(
                            text = stringResource(Res.string.theme_colors),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    items(
                        Material3ColorWrapper.entries.toTypedArray(),
                    ) { themeColor ->

                        val borderModifier =
                            if (initialColor?.themeColor == themeColor) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(8.dp),
                                )
                            } else {
                                Modifier
                            }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                borderModifier
                                    .size(
                                        width = 164.dp,
                                        height = 64.dp,
                                    ).padding(8.dp)
                                    .background(
                                        color = themeColor.getAppColor(),
                                        shape = RoundedCornerShape(8.dp),
                                    ).border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                    ).clickable {
                                        onThemeColorSelected(themeColor)
                                        onCloseClick()
                                    }.hoverIconClickable(),
                        ) {
                            Text(
                                text = themeColor.colorName,
                                color = themeColor.getTextColor(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier =
                                    Modifier
                                        .padding(
                                            start = 8.dp,
                                            top = 8.dp,
                                            end = 8.dp,
                                            bottom = 8.dp,
                                        ),
                            )
                        }
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
                        onCloseClick()
                    },
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Text(stringResource(Res.string.cancel))
                }
                OutlinedButton(
                    onClick = {
                        onColorUpdated(selectedColor)
                        onCloseClick()
                    },
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            }
        }
    }
}

@Composable
fun ColorPreviewInfo(
    color: Color,
    showAlpha: Boolean = true,
    onColorChanged: ((Color) -> Unit)? = null,
) {
    var hexText by remember(color) { mutableStateOf(color.toHexString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        val red: Int = color.red.times(255).toInt()
        val green: Int = color.green.times(255).toInt()
        val blue: Int = color.blue.times(255).toInt()

        Text(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.secondary,
            text =
                if (showAlpha) {
                    "A: ${color.alpha} \n"
                } else {
                    ""
                } +
                    "R: $red \n" +
                    "G: $green \n" +
                    "B: $blue",
        )

        // Hex color input field
        if (onColorChanged != null) {
            SmallOutlinedTextField(
                value = hexText,
                onValueChange = { newHex ->
                    hexText = newHex
                    val newColor = newHex.toColorOrNull()
                    if (newColor != null) {
                        onColorChanged(newColor)
                    }
                },
                label = { Text("Hex") },
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                singleLine = true,
                placeholder = { Text("#RRGGBB or #AARRGGBB") },
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .background(
                        color,
                        shape = RoundedCornerShape(8.dp),
                    ).size(72.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ThemedColorPropertyEditorPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        ColorPropertyEditor(
            label = "Background Color",
            initialColor = ColorWrapper(Material3ColorWrapper.Primary),
            onColorUpdated = {},
            onThemeColorSelected = {},
            onColorDeleted = {},
        )
    }
}

@Preview
@Composable
fun ColorPropertyEditorPreview_Light() {
    ThemedColorPropertyEditorPreview(useDarkTheme = false)
}

@Preview
@Composable
fun ColorPropertyEditorPreview_Dark() {
    ThemedColorPropertyEditorPreview(useDarkTheme = true)
}

@Composable
private fun ThemedColorPropertyDialogContentPreview(
    useDarkTheme: Boolean,
    includeThemeColor: Boolean,
) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        ColorPropertyDialogContent(
            initialColor = ColorWrapper(Material3ColorWrapper.PrimaryContainer),
            onThemeColorSelected = {},
            onCloseClick = {},
            onColorUpdated = {},
            includeThemeColor = includeThemeColor,
        )
    }
}

@Preview
@Composable
fun ColorPropertyDialogContentPreview_Light_includeThemeColor() {
    ThemedColorPropertyDialogContentPreview(useDarkTheme = false, includeThemeColor = true)
}

@Preview
@Composable
fun ColorPropertyDialogContentPreview_Light_withoutThemeColor() {
    ThemedColorPropertyDialogContentPreview(useDarkTheme = false, includeThemeColor = false)
}

@Preview
@Composable
fun ColorPropertyDialogContentPreview_Dark_includeThemeColor() {
    ThemedColorPropertyDialogContentPreview(useDarkTheme = true, includeThemeColor = true)
}

@Preview
@Composable
fun ColorPropertyDialogContentPreview_Dark_withoutThemeColor() {
    ThemedColorPropertyDialogContentPreview(useDarkTheme = true, includeThemeColor = false)
}

@Composable
private fun ThemedColorPreviewInfoPreview(useDarkTheme: Boolean) {
    ComposeFlowTheme(useDarkTheme = useDarkTheme) {
        ColorPreviewInfo(
            color = Color(0xFF1976D2),
            onColorChanged = { /* Preview - no action */ },
        )
    }
}

@Preview
@Composable
fun ColorPreviewInfoPreview_Light() {
    ThemedColorPreviewInfoPreview(useDarkTheme = false)
}

@Preview
@Composable
fun ColorPreviewInfoPreview_Dark() {
    ThemedColorPreviewInfoPreview(useDarkTheme = true)
}
