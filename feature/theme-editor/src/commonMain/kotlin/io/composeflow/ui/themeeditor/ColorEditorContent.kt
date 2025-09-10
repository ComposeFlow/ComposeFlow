package io.composeflow.ui.themeeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.toneColor
import com.materialkolor.palettes.CorePalette
import io.composeflow.Res
import io.composeflow.cancel
import io.composeflow.chane_to_day_theme
import io.composeflow.chane_to_night_theme
import io.composeflow.confirm
import io.composeflow.dark_scheme
import io.composeflow.keyboard.getCtrlKeyStr
import io.composeflow.light_scheme
import io.composeflow.model.palette.PaletteRenderParams
import io.composeflow.model.parameter.wrapper.Material3ColorWrapper
import io.composeflow.model.project.Project
import io.composeflow.redo
import io.composeflow.reset
import io.composeflow.reset_to_default_colors
import io.composeflow.seed_color
import io.composeflow.seed_color_description
import io.composeflow.serializer.asString
import io.composeflow.theme_color_sync_description
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
import io.composeflow.ui.switch.ComposeFlowSwitch
import io.composeflow.ui.zoomablecontainer.ZoomableContainerStateHolder
import io.composeflow.undo
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
        val coroutineScope = rememberCoroutineScope()
        val onShowSnackbar = LocalOnShowSnackbar.current
        var actionsMenuExpanded by remember { mutableStateOf(false) }

        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            val result = callbacks.onKeyPressed(event)
                            result.errorMessages.forEach {
                                coroutineScope.launch {
                                    onShowSnackbar(it, null)
                                }
                            }
                            result.consumed
                        } else {
                            false
                        }
                    }.onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                        actionsMenuExpanded = true
                    },
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
            var lightScheme by remember(colorSchemeHolder.lightColorScheme.value) {
                mutableStateOf(
                    colorSchemeHolder.lightColorScheme.value.toColorScheme(),
                )
            }
            var darkScheme by remember(colorSchemeHolder.darkColorScheme.value) {
                mutableStateOf(
                    colorSchemeHolder.darkColorScheme.value.toColorScheme(),
                )
            }
            var syncSchemes by remember { mutableStateOf(true) }
            var schemeInEdit by remember { mutableStateOf(ColorSchemeType.Light) }

            val updateColorSchemes = stringResource(Res.string.updated_color_schemes)

            fun buildScheme(isDark: Boolean): ColorScheme =
                sourceColor?.let {
                    dynamicColorScheme(
                        it,
                        isDark = isDark,
                        isAmoled = false,
                        style = paletteStyle,
                    )
                } ?: if (isDark) {
                    colorSchemeHolder.darkColorScheme.value.toColorScheme()
                } else {
                    colorSchemeHolder.lightColorScheme.value.toColorScheme()
                }

            LaunchedEffect(sourceColor, paletteStyle) {
                if (sourceColor != colorSchemeHolder.sourceColor || paletteStyle != colorSchemeHolder.paletteStyle) {
                    when {
                        syncSchemes -> {
                            lightScheme = buildScheme(false)
                            darkScheme = buildScheme(true)
                        }

                        else -> {
                            if (schemeInEdit == ColorSchemeType.Light) {
                                lightScheme = buildScheme(false)
                            } else {
                                darkScheme = buildScheme(true)
                            }
                        }
                    }
                }
            }
            ColorSchemeEditor(
                project = project,
                callbacks = callbacks,
                sourceColor = sourceColor,
                paletteStyle = paletteStyle,
                schemeInEdit = schemeInEdit,
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                syncSchemes = syncSchemes,
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
                onSyncSchemesChanged = {
                    syncSchemes = it
                },
                onSchemeInEditChanged = {
                    schemeInEdit = it
                },
                onShowSnackbar = { message, action ->
                    coroutineScope.launch {
                        onShowSnackbar(message, action)
                    }
                },
            )
            ColorSchemeDetailsContainer(
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                syncSchemes = syncSchemes,
                onChangeScheme = { (light, dark) ->
                    lightScheme = light
                    darkScheme = dark
                    coroutineScope.launch {
                        callbacks.onColorSchemeUpdated(
                            Color.Transparent,
                            paletteStyle,
                            lightScheme,
                            darkScheme,
                        )
                        onShowSnackbar(updateColorSchemes, null)
                    }
                },
            )
            CanvasPreview(
                project = project,
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                modifier = Modifier.weight(1f),
            )

            if (actionsMenuExpanded) {
                ActionsDropdownMenu(
                    expanded = actionsMenuExpanded,
                    onUndo = { callbacks.onUndo() },
                    onRedo = { callbacks.onRedo() },
                    onDismissRequest = {
                        actionsMenuExpanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ColorSchemeEditor(
    project: Project,
    callbacks: ThemeEditorCallbacks,
    sourceColor: Color?,
    paletteStyle: PaletteStyle,
    schemeInEdit: ColorSchemeType,
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    syncSchemes: Boolean,
    onEditCanceled: () -> Unit,
    onSourceColorChanged: (Color?) -> Unit,
    onPaletteStyleChanged: (PaletteStyle) -> Unit,
    onSyncSchemesChanged: (Boolean) -> Unit,
    onSchemeInEditChanged: (ColorSchemeType) -> Unit,
    onShowSnackbar: (String, String?) -> Unit,
) {
    var colorInEdit by remember { mutableStateOf(false) }
    var resetToDefaultColorsDialogOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .width(240.dp)
                .fillMaxHeight()
                .focusProperties { canFocus = true }
                .focusRequester(focusRequester),
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sync Theme Colors",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            ComposeFlowSwitch(
                checked = syncSchemes,
                onCheckedChange = onSyncSchemesChanged,
            )

            val themeColorSyncDesc = stringResource(Res.string.theme_color_sync_description)
            Tooltip(themeColorSyncDesc) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = themeColorSyncDesc,
                )
            }
        }
        Spacer(
            modifier = Modifier.height(16.dp),
        )
        HorizontalDivider()
        if (colorInEdit) {
            Column(modifier = Modifier.animateContentSize(keyframes { durationMillis = 100 })) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorPickerProperty(
                        label = stringResource(Res.string.seed_color),
                        initialColor = sourceColor ?: Color.Black,
                        onColorConfirmed = {
                            onSourceColorChanged(it)
                            focusRequester.requestFocus()
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

                AnimatedVisibility(
                    visible = !syncSchemes,
                ) {
                    BasicDropdownPropertyEditor(
                        project = project,
                        items = ColorSchemeType.entries,
                        label = "Color scheme",
                        onValueChanged = { _, item ->
                            onSchemeInEditChanged(item)
                        },
                        selectedItem = schemeInEdit,
                    )
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

                            onShowSnackbar(updateColorSchemes, null)
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

private enum class ColorSchemeType {
    Light,
    Dark,
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
                                .width(700.dp)
                                .height(300.dp)
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                    )

                    ColorPreviewInfo(
                        color = selectedColor.convert(ColorSpaces.Srgb),
                        onColorChanged = { newColor ->
                            controller.selectByColor(newColor, fromUser = true)
                        },
                    )
                }

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
                            modifier = Modifier.weight(1f).height(24.dp).padding(end = 16.dp),
                        )
                        AlphaTile(
                            controller = controller,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                        )
                    }
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
    syncSchemes: Boolean,
    onChangeScheme: (Pair<ColorScheme, ColorScheme>) -> Unit,
) {
    LazyColumn(modifier = Modifier.wrapContentWidth()) {
        item {
            ProvideAppThemeTokens(
                isDarkTheme = false,
                lightScheme = lightScheme,
            ) {
                ColorSchemePalette(
                    colorScheme = lightScheme,
                    isDarkTheme = false,
                    onColorSelected = { appColorWrapper, appColor, textColor, newColor ->
                        onChangeScheme(
                            calculateThemeColors(
                                appColorWrapper,
                                newColor,
                                lightScheme,
                                darkScheme,
                                syncSchemes,
                                isDark = false,
                            ),
                        )
                    },
                )
            }
        }
        item {
            ProvideAppThemeTokens(
                isDarkTheme = true,
                darkScheme = darkScheme,
            ) {
                ColorSchemePalette(
                    colorScheme = darkScheme,
                    isDarkTheme = true,
                    onColorSelected = { appColorWrapper, _, _, newColor ->
                        onChangeScheme(
                            calculateThemeColors(
                                appColorWrapper,
                                newColor,
                                lightScheme,
                                darkScheme,
                                syncSchemes,
                                isDark = true,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ColorSchemePalette(
    colorScheme: ColorScheme,
    isDarkTheme: Boolean = false,
    onColorSelected: (Material3ColorWrapper, Color, Color, Color) -> Unit,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    var colorSelected by remember {
        mutableStateOf<Material3ColorWrapper?>(null)
    }
    val focusRequester = remember { FocusRequester() }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current

    @Composable
    fun HorizontalColors(
        colors: List<Material3ColorWrapper>,
        modifier: Modifier = Modifier,
        maxItemsInEachRow: Int = 4,
        horizontalArrangement: Dp = 4.dp,
        itemHeight: Dp = 100.dp,
        itemWidth: Dp? = null,
        lastItemWidth: Dp? = itemWidth,
        lastItemStartPadding: Dp = 13.dp,
    ) {
        HorizontalColorsPalette(
            colors = colors,
            modifier = modifier,
            maxItemsInEachRow = maxItemsInEachRow,
            horizontalArrangement = horizontalArrangement,
            itemHeight = itemHeight,
            itemWidth = itemWidth,
            lastItemWidth = lastItemWidth,
            lastItemStartPadding = lastItemStartPadding,
            onShowColorPickerDialog = {
                colorSelected = it
                dialogOpen = true
            },
        )
    }

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
                ).focusProperties { canFocus = true }
                .focusRequester(focusRequester),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).width(820.dp),
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

            Column {
                HorizontalColors(
                    colors = coreColors(),
                )

                HorizontalColors(
                    colors = onColors(),
                    itemHeight = 60.dp,
                )

                HorizontalColors(
                    colors = containerColors(),
                )

                HorizontalColors(
                    colors = onContainerColors(),
                    itemHeight = 60.dp,
                )

                Spacer(
                    modifier = Modifier.height(16.dp),
                )

                HorizontalColors(
                    colors = surfaceColors(),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HorizontalColors(
                        modifier = Modifier.weight(1f),
                        colors = surfaceContainerColors(),
                        horizontalArrangement = 0.dp,
                        maxItemsInEachRow = 5,
                        lastItemStartPadding = 0.dp,
                    )

                    VerticalColorsPalette(
                        colors = inverseColors(),
                        itemHeight = 48.dp,
                        itemWidth = 190.dp,
                        onShowColorPickerDialog = {
                            colorSelected = it
                            dialogOpen = true
                        },
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HorizontalColors(
                        modifier = Modifier.weight(1f),
                        colors = onSurfaceColors().take(4),
                        itemHeight = 60.dp,
                        horizontalArrangement = 0.dp,
                        lastItemStartPadding = 0.dp,
                    )

                    ColorBox(
                        modifier = Modifier.size(190.dp, 60.dp),
                        color = onSurfaceColors().last(),
                        onClick = {
                            colorSelected = onSurfaceColors().last()
                            dialogOpen = true
                        },
                    )
                }
            }
        }
        if (dialogOpen && colorSelected != null) {
            onAnyDialogIsShown()
            val appColor = colorSelected!!.getAppColor()
            val textColor = colorSelected!!.getTextColor()

            ColorPickerDialog(
                initialColor = appColor,
                onCloseClick = {
                    dialogOpen = false
                    colorSelected = null
                    onAllDialogsClosed()
                },
                onColorConfirmed = {
                    onColorSelected(colorSelected!!, appColor, textColor, it)
                    onAllDialogsClosed()
                    focusRequester.requestFocus()
                },
            )
        }
    }
}

@Composable
private fun HorizontalColorsPalette(
    colors: List<Material3ColorWrapper>,
    modifier: Modifier = Modifier,
    maxItemsInEachRow: Int = 4,
    horizontalArrangement: Dp = 4.dp,
    itemHeight: Dp = 100.dp,
    itemWidth: Dp? = null,
    lastItemWidth: Dp? = itemWidth,
    lastItemStartPadding: Dp = 13.dp,
    onShowColorPickerDialog: (Material3ColorWrapper) -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalArrangement),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        colors.forEach { item ->
            val isLast = colors.last() == item
            val startPadding = if (isLast) lastItemStartPadding else 0.dp
            val width = if (isLast) lastItemWidth else itemWidth

            ColorBox(
                modifier =
                    modifier
                        .then(
                            if (width != null) Modifier.width(width) else Modifier.weight(1f),
                        ).height(itemHeight)
                        .padding(start = startPadding),
                color = item,
                onClick = {
                    onShowColorPickerDialog(item)
                },
            )
        }
    }
}

@Composable
private fun VerticalColorsPalette(
    colors: List<Material3ColorWrapper>,
    modifier: Modifier = Modifier,
    maxItemsInEachColumn: Int = 4,
    horizontalArrangement: Dp = 0.dp,
    verticalArrangement: Dp = 2.dp,
    itemHeight: Dp = 100.dp,
    itemWidth: Dp? = null,
    lastItemStartPadding: Dp = 0.dp,
    onShowColorPickerDialog: (Material3ColorWrapper) -> Unit,
) {
    FlowColumn(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalArrangement),
        verticalArrangement = Arrangement.spacedBy(verticalArrangement),
        maxItemsInEachColumn = maxItemsInEachColumn,
    ) {
        colors.forEach { item ->
            val isLast = colors.last() == item
            val startPadding = if (isLast) lastItemStartPadding else 0.dp

            ColorBox(
                modifier =
                    modifier
                        .then(
                            if (itemWidth != null) Modifier.width(itemWidth) else Modifier,
                        ).height(itemHeight)
                        .padding(start = startPadding),
                color = item,
                onClick = {
                    onShowColorPickerDialog(item)
                },
            )
        }
    }
}

@Composable
private fun ColorBox(
    color: Material3ColorWrapper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val appColor = color.getAppColor()
    val textColor = color.getTextColor()

    SelectionContainer(
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(appColor.copy(alpha = if (isHovered) 0.9f else 1f))
                    .hoverable(interactionSource),
        ) {
            Text(
                text = color.getLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            )

            Text(
                text = appColor.asString(),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            )

            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                IconButton(
                    onClick = onClick,
                ) {
                    ComposeFlowIcon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = textColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    PositionCustomizablePopup(
        onDismissRequest = {},
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
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
                onUndo()
                onDismissRequest()
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
                onRedo()
                onDismissRequest()
            })
        }
    }
}

private fun calculateThemeColors(
    appColorWrapper: Material3ColorWrapper,
    newColor: Color,
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    syncSchemes: Boolean,
    isDark: Boolean,
): Pair<ColorScheme, ColorScheme> {
    val textAppColorWrapper = appColorWrapper.toOnColorWrapper()

    val palette = CorePalette.of(newColor.toArgb())
    val contentPalette = CorePalette.contentOf(newColor.toArgb())

    val lightColor = palette.a1.toneColor(40)
    val darkColor = palette.a1.toneColor(80)
    val lightContentColor = contentPalette.a1.toneColor(100)
    val darkContentColor = contentPalette.a1.toneColor(20)

    val newLightScheme =
        lightScheme.updateColor(appColorWrapper, lightColor).run {
            textAppColorWrapper?.let { updateColor(it, lightContentColor) } ?: this
        }
    val newDarkScheme =
        darkScheme.updateColor(appColorWrapper, darkColor).run {
            textAppColorWrapper?.let { updateColor(it, darkContentColor) } ?: this
        }

    return when {
        syncSchemes -> newLightScheme to newDarkScheme
        else -> {
            if (isDark) {
                lightScheme to newDarkScheme
            } else {
                newLightScheme to darkScheme
            }
        }
    }
}

private fun coreColors() =
    listOf(
        Material3ColorWrapper.Primary,
        Material3ColorWrapper.Secondary,
        Material3ColorWrapper.Tertiary,
        Material3ColorWrapper.Error,
    )

private fun onColors() =
    listOf(
        Material3ColorWrapper.OnPrimary,
        Material3ColorWrapper.OnSecondary,
        Material3ColorWrapper.OnTertiary,
        Material3ColorWrapper.OnError,
    )

private fun containerColors() =
    listOf(
        Material3ColorWrapper.PrimaryContainer,
        Material3ColorWrapper.SecondaryContainer,
        Material3ColorWrapper.TertiaryContainer,
        Material3ColorWrapper.ErrorContainer,
    )

private fun onContainerColors() =
    listOf(
        Material3ColorWrapper.OnPrimaryContainer,
        Material3ColorWrapper.OnSecondaryContainer,
        Material3ColorWrapper.OnTertiaryContainer,
        Material3ColorWrapper.OnErrorContainer,
    )

private fun surfaceColors() =
    listOf(
        Material3ColorWrapper.SurfaceDim,
        Material3ColorWrapper.Surface,
        Material3ColorWrapper.SurfaceBright,
        Material3ColorWrapper.InverseSurface,
    )

private fun surfaceContainerColors() =
    listOf(
        Material3ColorWrapper.SurfaceContainerLowest,
        Material3ColorWrapper.SurfaceContainerLow,
        Material3ColorWrapper.SurfaceContainer,
        Material3ColorWrapper.SurfaceContainerHigh,
        Material3ColorWrapper.SurfaceContainerHighest,
    )

private fun onSurfaceColors() =
    listOf(
        Material3ColorWrapper.OnSurface,
        Material3ColorWrapper.OnSurfaceVariant,
        Material3ColorWrapper.Outline,
        Material3ColorWrapper.OutlineVariant,
        Material3ColorWrapper.Scrim,
    )

private fun inverseColors() =
    listOf(
        Material3ColorWrapper.InverseOnSurface,
        Material3ColorWrapper.InversePrimary,
    )

private fun Material3ColorWrapper.getLabel(): String =
    when (this) {
        Material3ColorWrapper.Primary -> "Primary"
        Material3ColorWrapper.OnPrimary -> "On Primary"
        Material3ColorWrapper.PrimaryContainer -> "Primary Container"
        Material3ColorWrapper.OnPrimaryContainer -> "On Primary Container"
        Material3ColorWrapper.Secondary -> "Secondary"
        Material3ColorWrapper.OnSecondary -> "On Secondary"
        Material3ColorWrapper.SecondaryContainer -> "Secondary Container"
        Material3ColorWrapper.OnSecondaryContainer -> "On Secondary Container"
        Material3ColorWrapper.Tertiary -> "Tertiary"
        Material3ColorWrapper.OnTertiary -> "On Tertiary"
        Material3ColorWrapper.TertiaryContainer -> "Tertiary Container"
        Material3ColorWrapper.OnTertiaryContainer -> "On Tertiary Container"
        Material3ColorWrapper.Error -> "Error"
        Material3ColorWrapper.OnError -> "On Error"
        Material3ColorWrapper.ErrorContainer -> "Error Container"
        Material3ColorWrapper.OnErrorContainer -> "On Error Container"
        Material3ColorWrapper.Background -> "Background"
        Material3ColorWrapper.OnBackground -> "On Background"
        Material3ColorWrapper.Surface -> "Surface"
        Material3ColorWrapper.OnSurface -> "On Surface"
        Material3ColorWrapper.SurfaceVariant -> "Surface Variant"
        Material3ColorWrapper.OnSurfaceVariant -> "On Surface Variant"
        Material3ColorWrapper.SurfaceTint -> "Surface Tint"
        Material3ColorWrapper.InverseSurface -> "Inverse Surface"
        Material3ColorWrapper.InverseOnSurface -> "Inverse On Surface"
        Material3ColorWrapper.Outline -> "Outline"
        Material3ColorWrapper.OutlineVariant -> "Outline Variant"
        Material3ColorWrapper.Scrim -> "Scrim"
        Material3ColorWrapper.SurfaceDim -> "Surface Dim"
        Material3ColorWrapper.SurfaceBright -> "Surface Bright"
        Material3ColorWrapper.SurfaceContainerLowest -> "Surface Container Lowest"
        Material3ColorWrapper.SurfaceContainerLow -> "Surface Container Low"
        Material3ColorWrapper.SurfaceContainer -> "Surface Container"
        Material3ColorWrapper.SurfaceContainerHigh -> "Surface Container High"
        Material3ColorWrapper.SurfaceContainerHighest -> "Surface Container Highest"
        Material3ColorWrapper.InversePrimary -> "Inverse Primary"
    }

private fun Material3ColorWrapper.toOnColorWrapper(): Material3ColorWrapper? =
    when (this) {
        Material3ColorWrapper.Primary -> Material3ColorWrapper.OnPrimary
        Material3ColorWrapper.PrimaryContainer -> Material3ColorWrapper.OnPrimaryContainer
        Material3ColorWrapper.InversePrimary -> Material3ColorWrapper.OnPrimaryContainer
        Material3ColorWrapper.Secondary -> Material3ColorWrapper.OnSecondary
        Material3ColorWrapper.SecondaryContainer -> Material3ColorWrapper.OnSecondaryContainer
        Material3ColorWrapper.Tertiary -> Material3ColorWrapper.OnTertiary
        Material3ColorWrapper.TertiaryContainer -> Material3ColorWrapper.OnTertiaryContainer
        Material3ColorWrapper.Background -> Material3ColorWrapper.OnBackground
        Material3ColorWrapper.Surface -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceVariant -> Material3ColorWrapper.OnSurfaceVariant
        Material3ColorWrapper.SurfaceTint -> Material3ColorWrapper.PrimaryContainer
        Material3ColorWrapper.InverseSurface -> Material3ColorWrapper.Surface
        Material3ColorWrapper.InverseOnSurface -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.Error -> Material3ColorWrapper.OnError
        Material3ColorWrapper.ErrorContainer -> Material3ColorWrapper.OnErrorContainer
        Material3ColorWrapper.Outline -> Material3ColorWrapper.Background
        Material3ColorWrapper.OutlineVariant -> Material3ColorWrapper.Secondary
        Material3ColorWrapper.SurfaceDim -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceBright -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceContainerLowest -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceContainerLow -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceContainer -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceContainerHigh -> Material3ColorWrapper.OnSurface
        Material3ColorWrapper.SurfaceContainerHighest -> Material3ColorWrapper.OnSurface
        else -> null
    }

private fun ColorScheme.updateColor(
    color: Material3ColorWrapper,
    newColor: Color,
): ColorScheme =
    when (color) {
        Material3ColorWrapper.Primary -> copy(primary = newColor)
        Material3ColorWrapper.OnPrimary -> copy(onPrimary = newColor)
        Material3ColorWrapper.PrimaryContainer -> copy(primaryContainer = newColor)
        Material3ColorWrapper.OnPrimaryContainer -> copy(onPrimaryContainer = newColor)
        Material3ColorWrapper.InversePrimary -> copy(inversePrimary = newColor)
        Material3ColorWrapper.Secondary -> copy(secondary = newColor)
        Material3ColorWrapper.OnSecondary -> copy(onSecondary = newColor)
        Material3ColorWrapper.SecondaryContainer -> copy(secondaryContainer = newColor)
        Material3ColorWrapper.OnSecondaryContainer -> copy(onSecondaryContainer = newColor)
        Material3ColorWrapper.Tertiary -> copy(tertiary = newColor)
        Material3ColorWrapper.OnTertiary -> copy(onTertiary = newColor)
        Material3ColorWrapper.TertiaryContainer -> copy(tertiaryContainer = newColor)
        Material3ColorWrapper.OnTertiaryContainer -> copy(onTertiaryContainer = newColor)
        Material3ColorWrapper.Background -> copy(background = newColor)
        Material3ColorWrapper.OnBackground -> copy(onBackground = newColor)
        Material3ColorWrapper.Surface -> copy(surface = newColor)
        Material3ColorWrapper.OnSurface -> copy(onSurface = newColor)
        Material3ColorWrapper.SurfaceVariant -> copy(surfaceVariant = newColor)
        Material3ColorWrapper.OnSurfaceVariant -> copy(onSurfaceVariant = newColor)
        Material3ColorWrapper.SurfaceTint -> copy(surfaceTint = newColor)
        Material3ColorWrapper.InverseSurface -> copy(inverseSurface = newColor)
        Material3ColorWrapper.InverseOnSurface -> copy(inverseOnSurface = newColor)
        Material3ColorWrapper.Error -> copy(error = newColor)
        Material3ColorWrapper.OnError -> copy(onError = newColor)
        Material3ColorWrapper.ErrorContainer -> copy(errorContainer = newColor)
        Material3ColorWrapper.OnErrorContainer -> copy(onErrorContainer = newColor)
        Material3ColorWrapper.Outline -> copy(outline = newColor)
        Material3ColorWrapper.OutlineVariant -> copy(outlineVariant = newColor)
        Material3ColorWrapper.Scrim -> copy(scrim = newColor)
        Material3ColorWrapper.SurfaceBright -> copy(surfaceBright = newColor)
        Material3ColorWrapper.SurfaceDim -> copy(surfaceDim = newColor)
        Material3ColorWrapper.SurfaceContainer -> copy(surfaceContainer = newColor)
        Material3ColorWrapper.SurfaceContainerHigh -> copy(surfaceContainerHigh = newColor)
        Material3ColorWrapper.SurfaceContainerHighest -> copy(surfaceContainerHighest = newColor)
        Material3ColorWrapper.SurfaceContainerLow -> copy(surfaceContainerLow = newColor)
        Material3ColorWrapper.SurfaceContainerLowest -> copy(surfaceContainerLowest = newColor)
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
        modifier = Modifier.padding(start = 16.dp).backgroundContainerNeutral(),
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
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                ) {
                    items(project.screenHolder.screens) { screen ->
                        Spacer(Modifier.padding(top = 16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().background(Color.Transparent),
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
                                                    .onClick(
                                                        enabled = false,
                                                        onClick = {},
                                                    ).align(Alignment.CenterHorizontally)
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
