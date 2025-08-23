package io.composeflow.ui.themeeditor

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import io.composeflow.Res
import io.composeflow.cancel
import io.composeflow.chane_to_day_theme
import io.composeflow.chane_to_night_theme
import io.composeflow.confirm
import io.composeflow.dark_scheme
import io.composeflow.light_scheme
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.project.Project
import io.composeflow.reset
import io.composeflow.reset_to_default_colors
import io.composeflow.seed_color
import io.composeflow.seed_color_description
import io.composeflow.serializer.asString
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.LocalOnShowSnackbar
import io.composeflow.ui.Tooltip
import io.composeflow.ui.adaptive.ProvideDeviceSizeDp
import io.composeflow.ui.background.DotPatternBackground
import io.composeflow.ui.common.AppTheme
import io.composeflow.ui.common.ProvideAppThemeTokens
import io.composeflow.ui.emptyCanvasNodeCallbacks
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.propertyeditor.BasicDropdownPropertyEditor
import io.composeflow.ui.propertyeditor.ColorPreviewInfo
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.update_colors
import io.composeflow.updated_color_schemes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorEditorContent(
    project: Project,
    callbacks: ThemeEditorCallbacks,
    modifier: Modifier = Modifier,
) {
    Surface {
        Row(
            modifier = modifier.fillMaxSize(),
        ) {
            val colorSchemeHolder = project.themeHolder.colorSchemeHolder
            var sourceColor by remember(colorSchemeHolder.sourceColor) {
                mutableStateOf(
                    colorSchemeHolder.sourceColor,
                )
            }
            var paletteStyle by remember(colorSchemeHolder.paletteStyle) {
                mutableStateOf(
                    colorSchemeHolder.paletteStyle,
                )
            }
            val lightScheme by remember(
                sourceColor,
                paletteStyle,
                colorSchemeHolder.lightColorScheme.value,
            ) {
                mutableStateOf(
                    sourceColor?.let {
                        dynamicColorScheme(
                            it,
                            isDark = false,
                            isAmoled = false,
                            style = paletteStyle,
                        )
                    }
                        ?: colorSchemeHolder.lightColorScheme.value.toColorScheme(),
                )
            }
            val darkScheme by remember(
                sourceColor,
                paletteStyle,
                colorSchemeHolder.darkColorScheme.value,
            ) {
                mutableStateOf(
                    sourceColor?.let {
                        dynamicColorScheme(
                            it,
                            isDark = true,
                            isAmoled = false,
                            style = paletteStyle,
                        )
                    } ?: colorSchemeHolder.darkColorScheme.value.toColorScheme(),
                )
            }
            ColorSchemeEditor(
                project = project,
                callbacks = callbacks,
                sourceColor = sourceColor,
                paletteStyle = paletteStyle,
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                onEditCanceled = {
                    sourceColor = colorSchemeHolder.sourceColor
                    paletteStyle = colorSchemeHolder.paletteStyle
                },
                onSourceColorChanged = {
                    sourceColor = it
                },
                onPaletteStyleChanged = {
                    paletteStyle = it
                },
            )
            ColorSchemeDetailsContainer(
                lightScheme = lightScheme,
                darkScheme = darkScheme,
            )
            CanvasPreview(
                project = project,
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ColorSchemeEditor(
    project: Project,
    callbacks: ThemeEditorCallbacks,
    sourceColor: Color?,
    paletteStyle: PaletteStyle,
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    onEditCanceled: () -> Unit,
    onSourceColorChanged: (Color?) -> Unit,
    onPaletteStyleChanged: (PaletteStyle) -> Unit,
) {
    var colorInEdit by remember { mutableStateOf(false) }
    var resetToDefaultColorsDialogOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .width(240.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp),
        ) {
            Text(
                text = "Color scheme builder",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (colorInEdit) {
            Column(modifier = Modifier.animateContentSize(keyframes { durationMillis = 100 })) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorPickerProperty(
                        label = stringResource(Res.string.seed_color),
                        initialColor = sourceColor ?: Color.Black,
                        onColorConfirmed = {
                            onSourceColorChanged(it)
                        },
                    )
                    val seedColorDesc = stringResource(Res.string.seed_color_description)
                    Tooltip(seedColorDesc) {
                        ComposeFlowIcon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = seedColorDesc,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                BasicDropdownPropertyEditor(
                    project = project,
                    items = PaletteStyle.entries,
                    label = "Palette style",
                    onValueChanged = { _, item ->
                        onPaletteStyleChanged(item)
                    },
                    selectedItem = paletteStyle,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    val onShowSnackbar = LocalOnShowSnackbar.current
                    val contentDesc = stringResource(Res.string.reset_to_default_colors)
                    val updateColorSchemes = stringResource(Res.string.updated_color_schemes)
                    Tooltip(contentDesc) {
                        IconButton(onClick = {
                            resetToDefaultColorsDialogOpen = true
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            colorInEdit = false
                            onEditCanceled()
                        },
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                    OutlinedButton(
                        onClick = {
                            callbacks.onColorSchemeUpdated(
                                sourceColor ?: Color.Black,
                                paletteStyle,
                                lightScheme,
                                darkScheme,
                            )
                            colorInEdit = false

                            coroutineScope.launch {
                                onShowSnackbar(updateColorSchemes, null)
                            }
                        },
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        } else {
            TextButton(
                onClick = {
                    colorInEdit = true
                },
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(stringResource(Res.string.update_colors))
            }
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (resetToDefaultColorsDialogOpen) {
        onAnyDialogIsShown()
        val closeDialog = {
            onAllDialogsClosed()
            resetToDefaultColorsDialogOpen = false
        }
        SimpleConfirmationDialog(
            text = "${stringResource(Res.string.reset_to_default_colors)}?",
            onCloseClick = {
                closeDialog()
            },
            onConfirmClick = {
                callbacks.onColorResetToDefault()
                colorInEdit = false
                onSourceColorChanged(null)
                closeDialog()
            },
            positiveText = stringResource(Res.string.reset),
        )
    }
}

@Composable
private fun ColorPickerProperty(
    initialColor: Color,
    label: String,
    onColorConfirmed: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .height(38.dp)
                .clickable {
                    dialogOpen = true
                }.hoverIconClickable()
                .focusable(),
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .wrapContentWidth()
                        .padding(end = 8.dp)
                        .align(Alignment.CenterVertically),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .background(
                            color = initialColor,
                            shape = RoundedCornerShape(8.dp),
                        ).border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                        ),
            )
        }
    }
    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
    if (dialogOpen) {
        onAnyDialogIsShown()
        ColorPickerDialog(
            initialColor = initialColor,
            onCloseClick = {
                dialogOpen = false
                onAllDialogsClosed()
            },
            onColorConfirmed = onColorConfirmed,
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onCloseClick: () -> Unit,
    onColorConfirmed: (Color) -> Unit,
) {
    val controller = rememberColorPickerController()

    // Set initial color only once
    remember(initialColor) {
        controller.selectByColor(initialColor, fromUser = false)
    }

    // Observe the selected color
    val selectedColor by controller.selectedColor
    PositionCustomizablePopup(
        onDismissRequest = {
            onCloseClick()
        },
        onKeyEvent = {
            when (it.key) {
                Key.Escape -> {
                    onCloseClick()
                    true
                }

                Key.Enter -> {
                    onColorConfirmed(selectedColor)
                    onCloseClick()
                    true
                }

                else -> {
                    false
                }
            }
        },
    ) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Row {
                    HsvColorPicker(
                        controller = controller,
                        modifier =
                            Modifier
                                .width(860.dp)
                                .height(300.dp)
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                    )

                    ColorPreviewInfo(
                        selectedColor.convert(ColorSpaces.Srgb),
                        showAlpha = false,
                    )
                }

                Row {
                    sampleColors.forEach { sampleColor ->
                        Box(
                            modifier =
                                Modifier
                                    .padding(start = 16.dp)
                                    .background(
                                        sampleColor,
                                        shape = RoundedCornerShape(8.dp),
                                    ).size(32.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                    ).hoverIconClickable()
                                    .clickable {
                                        controller.selectByColor(sampleColor, fromUser = true)
                                    },
                        )
                    }
                }

                Row(
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
                            onColorConfirmed(selectedColor)
                            onCloseClick()
                        },
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}

val sampleColors =
    listOf(
        Color(0xFFD32F2F),
        Color(0xFFC2185B),
        Color(0xFF7B1FA2),
        Color(0xFF512DA8),
        Color(0xFF303F9F),
        Color(0xFF1976D2),
        Color(0xFF0288D1),
        Color(0xFF0097A7),
        Color(0xFF00796B),
        Color(0xFF388E3C),
        Color(0xFF689F38),
        Color(0xFFAFB42B),
        Color(0xFFFBC02D),
        Color(0xFFFFA000),
        Color(0xFFF57C00),
        Color(0xFFE64A19),
        Color(0xFF5D4037),
        Color(0xFF616161),
        Color(0xFF455A64),
        Color(0xFF263238),
    )

@Composable
private fun ColorSchemeDetailsContainer(
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
) {
    LazyColumn(modifier = Modifier.wrapContentWidth()) {
        item {
            ColorSchemePalette(
                colorScheme = lightScheme,
                isDarkTheme = false,
            )
        }
        item {
            ColorSchemePalette(
                colorScheme = darkScheme,
                isDarkTheme = true,
            )
        }
    }
}

@Composable
private fun ColorSchemePalette(
    colorScheme: ColorScheme,
    isDarkTheme: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .background(
                    colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                ).border(
                    width = 1.dp,
                    color = colorScheme.outline,
                    shape = RoundedCornerShape(8.dp),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .width(820.dp),
        ) {
            Text(
                text =
                    if (isDarkTheme) {
                        stringResource(Res.string.dark_scheme)
                    } else {
                        stringResource(Res.string.light_scheme)
                    },
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row {
                VerticalColorPalette(
                    colorPair = "Primary" to colorScheme.primary,
                    onColorPair = "On Primary" to colorScheme.onPrimary,
                    containerColorPair = "Primary Container" to colorScheme.primaryContainer,
                    onContainerColorPair = "On Primary Container" to colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(2.dp))
                VerticalColorPalette(
                    colorPair = "Secondary" to colorScheme.secondary,
                    onColorPair = "On Secondary" to colorScheme.onSecondary,
                    containerColorPair = "Secondary Container" to colorScheme.secondaryContainer,
                    onContainerColorPair = "On Secondary Container" to colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(2.dp))
                VerticalColorPalette(
                    colorPair = "Tertiary" to colorScheme.tertiary,
                    onColorPair = "On Tertiary" to colorScheme.onTertiary,
                    containerColorPair = "Tertiary Container" to colorScheme.tertiaryContainer,
                    onContainerColorPair = "On Tertiary Container" to colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(start = 16.dp))
                VerticalColorPalette(
                    colorPair = "Error" to colorScheme.error,
                    onColorPair = "On Error" to colorScheme.onError,
                    containerColorPair = "Error Container" to colorScheme.errorContainer,
                    onContainerColorPair = "On Error Container" to colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.padding(top = 16.dp))
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.surfaceDim)
                                    .fillMaxWidth()
                                    .height(100.dp),
                        ) {
                            Text(
                                text = "Surface Dim",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.surfaceDim.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(2.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.surface)
                                    .fillMaxWidth()
                                    .height(100.dp),
                        ) {
                            Text(
                                text = "Surface",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.surface.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(2.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.surfaceBright)
                                    .fillMaxWidth()
                                    .height(100.dp),
                        ) {
                            Text(
                                text = "Surface Bright",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.surfaceBright.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(start = 16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.inverseSurface)
                                    .fillMaxWidth()
                                    .height(100.dp),
                        ) {
                            Text(
                                text = "Inverse Surface",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.inverseSurface.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
            }

            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            SelectionContainer {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(color = colorScheme.surfaceContainerLowest)
                                            .fillMaxWidth()
                                            .height(100.dp),
                                ) {
                                    Text(
                                        text = "Surf. Container Lowest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                    )

                                    Text(
                                        text = colorScheme.surfaceContainerLowest.asString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SelectionContainer {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(color = colorScheme.surfaceContainerLow)
                                            .fillMaxWidth()
                                            .height(100.dp),
                                ) {
                                    Text(
                                        text = "Surf. Container Low",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                    )

                                    Text(
                                        text = colorScheme.surfaceContainerLow.asString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SelectionContainer {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(color = colorScheme.surfaceContainer)
                                            .fillMaxWidth()
                                            .height(100.dp),
                                ) {
                                    Text(
                                        text = "Surf. Container",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                    )

                                    Text(
                                        text = colorScheme.surfaceContainer.asString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SelectionContainer {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(color = colorScheme.surfaceContainerHigh)
                                            .fillMaxWidth()
                                            .height(100.dp),
                                ) {
                                    Text(
                                        text = "Surf. Container High",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                    )

                                    Text(
                                        text = colorScheme.surfaceContainerHigh.asString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SelectionContainer {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(color = colorScheme.surfaceContainerHighest)
                                            .fillMaxWidth()
                                            .height(100.dp),
                                ) {
                                    Text(
                                        text = "Surf. Container Highest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp),
                                    )

                                    Text(
                                        text = colorScheme.surfaceContainerHighest.asString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurface,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.padding(start = 16.dp))
                Column(modifier = Modifier.width(199.dp).height(100.dp)) {
                    Row(modifier = Modifier.weight(1f)) {
                        SelectionContainer {
                            Box(
                                modifier =
                                    Modifier
                                        .background(color = colorScheme.inverseOnSurface)
                                        .fillMaxWidth()
                                        .height(100.dp),
                            ) {
                                Text(
                                    text = "Inverse On Surface",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurface,
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp),
                                )

                                Text(
                                    text = colorScheme.inverseOnSurface.asString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurface,
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.padding(top = 2.dp))
                    Row(modifier = Modifier.weight(1f)) {
                        SelectionContainer {
                            Box(
                                modifier =
                                    Modifier
                                        .background(color = colorScheme.inversePrimary)
                                        .fillMaxWidth()
                                        .height(100.dp),
                            ) {
                                Text(
                                    text = "Inverse Primary",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.primary,
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopStart)
                                            .padding(8.dp),
                                )

                                Text(
                                    text = colorScheme.inversePrimary.asString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.primary,
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.height(60.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.onSurface)
                                    .fillMaxSize(),
                        ) {
                            Text(
                                text = "onSurface",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.onSurface.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.onSurfaceVariant)
                                    .fillMaxSize(),
                        ) {
                            Text(
                                text = "On Surface Variant",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surfaceVariant,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.onSurfaceVariant.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surfaceVariant,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.outline)
                                    .fillMaxSize(),
                        ) {
                            Text(
                                text = "Outline",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.outline.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.surface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.outlineVariant)
                                    .fillMaxSize(),
                        ) {
                            Text(
                                text = "Outline Variant",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.outlineVariant.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(start = 16.dp))
                Row(modifier = Modifier.width(199.dp)) {
                    SelectionContainer {
                        Box(
                            modifier =
                                Modifier
                                    .background(color = colorScheme.scrim)
                                    .fillMaxSize(),
                        ) {
                            Text(
                                text = "Scrim",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                            )

                            Text(
                                text = colorScheme.scrim.asString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalColorPalette(
    colorPair: Pair<String, Color>,
    onColorPair: Pair<String, Color>,
    containerColorPair: Pair<String, Color>,
    onContainerColorPair: Pair<String, Color>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SelectionContainer {
            Box(
                modifier =
                    Modifier
                        .background(color = colorPair.second)
                        .fillMaxWidth()
                        .height(100.dp),
            ) {
                Text(
                    text = colorPair.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = onColorPair.second,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                )

                Text(
                    text = colorPair.second.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onColorPair.second,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }
        SelectionContainer {
            Box(
                modifier =
                    Modifier
                        .background(color = onColorPair.second)
                        .fillMaxWidth()
                        .height(60.dp),
            ) {
                Text(
                    text = onColorPair.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorPair.second,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                )

                Text(
                    text = onColorPair.second.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorPair.second,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }

        SelectionContainer {
            Box(
                modifier =
                    Modifier
                        .background(color = containerColorPair.second)
                        .fillMaxWidth()
                        .height(100.dp),
            ) {
                Text(
                    text = containerColorPair.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColorPair.second,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                )

                Text(
                    text = containerColorPair.second.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColorPair.second,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }

        SelectionContainer {
            Box(
                modifier =
                    Modifier
                        .background(color = onContainerColorPair.second)
                        .fillMaxWidth()
                        .height(60.dp),
            ) {
                Text(
                    text = onContainerColorPair.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = containerColorPair.second,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                )

                Text(
                    text = onContainerColorPair.second.asString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = containerColorPair.second,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun CanvasPreview(
    project: Project,
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    var useDarkTheme by remember { mutableStateOf(false) }
    var deviceSizeDp by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    Column(
        modifier =
            Modifier
                .padding(start = 16.dp)
                .backgroundContainerNeutral(),
    ) {
        ProvideAppThemeTokens(
            isDarkTheme = useDarkTheme,
            lightScheme = lightScheme,
            darkScheme = darkScheme,
            typography = project.themeHolder.fontHolder.generateTypography(),
        ) {
            DotPatternBackground(
                dotRadius = 1.dp,
                dotMargin = 12.dp,
                dotColor = Color.Black,
                modifier = modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                ) {
                    items(project.screenHolder.screens) { screen ->
                        Spacer(Modifier.padding(top = 16.dp))
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent),
                        ) {
                            ProvideDeviceSizeDp(deviceSizeDp) {
                                AppTheme {
                                    Surface(
                                        modifier =
                                            Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .align(Alignment.CenterHorizontally)
                                                .onGloballyPositioned {
                                                    deviceSizeDp = it.size / density.density.toInt()
                                                },
                                    ) {
                                        screen.contentRootNode().RenderedNodeInCanvas(
                                            project = project,
                                            canvasNodeCallbacks = emptyCanvasNodeCallbacks,
                                            paletteRenderParams = PaletteRenderParams(isThumbnail = true),
                                            zoomableContainerStateHolder = ZoomableContainerStateHolder(),
                                            modifier =
                                                Modifier
                                                    .onClick(enabled = false, onClick = {})
                                                    .align(Alignment.CenterHorizontally)
                                                    .size(width = 416.dp, height = 886.dp),
                                        )
                                    }
                                    Spacer(Modifier.padding(bottom = 16.dp))
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.height(64.dp),
                ) {
                    Spacer(Modifier.weight(1f))
                    Card(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .hoverIconClickable()
                                .align(Alignment.CenterVertically)
                                .zIndex(10f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        val contentDesc =
                            if (useDarkTheme) {
                                stringResource(Res.string.chane_to_day_theme)
                            } else {
                                stringResource(Res.string.chane_to_night_theme)
                            }
                        Tooltip(contentDesc) {
                            Icon(
                                modifier =
                                    Modifier
                                        .clickable {
                                            useDarkTheme = !useDarkTheme
                                        }.padding(8.dp)
                                        .size(24.dp),
                                imageVector =
                                    if (useDarkTheme) {
                                        Icons.Default.ModeNight
                                    } else {
                                        Icons.Default.WbSunny
                                    },
                                contentDescription = contentDesc,
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
    }
}
