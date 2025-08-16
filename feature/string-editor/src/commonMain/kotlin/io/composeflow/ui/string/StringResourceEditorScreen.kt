package io.composeflow.ui.string

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.action_add
import io.composeflow.add_new_string_resource
import io.composeflow.add_string_resource
import io.composeflow.ai_login_needed
import io.composeflow.apply_change
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.auth.isAiEnabled
import io.composeflow.cancel
import io.composeflow.default_locale_label
import io.composeflow.delete_string_resource_confirmation
import io.composeflow.delete_string_resources
import io.composeflow.delete_string_resources_confirmation
import io.composeflow.edit_supported_locales
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.StringResource
import io.composeflow.more_options
import io.composeflow.needs_translation_update
import io.composeflow.no_locales_found_matching
import io.composeflow.remove
import io.composeflow.remove_locale
import io.composeflow.remove_locale_confirmation
import io.composeflow.search_locales_placeholder
import io.composeflow.select_all
import io.composeflow.set_as_default_locale
import io.composeflow.string_resource_default_value_placeholder
import io.composeflow.string_resource_description
import io.composeflow.string_resource_description_placeholder
import io.composeflow.string_resource_description_tooltip
import io.composeflow.string_resource_key
import io.composeflow.string_resource_key_placeholder
import io.composeflow.string_resource_key_tooltip
import io.composeflow.string_resources
import io.composeflow.translate_selected_strings
import io.composeflow.translating_strings
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.PointerIconResizeHorizontal
import io.composeflow.ui.Tooltip
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.textfield.SmallOutlinedTextField
import io.composeflow.update_translations
import io.composeflow.updating_translations
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

private val DEFAULT_COLUMN_WIDTH = 200.dp

@Composable
fun StringResourceEditorScreen(
    project: Project,
    modifier: Modifier = Modifier,
) {
    val firebaseIdToken = LocalFirebaseIdToken.current
    val viewModel =
        viewModel(StringResourceEditorViewModel::class) {
            StringResourceEditorViewModel(firebaseIdToken, project)
        }
    Surface(modifier = modifier.fillMaxSize()) {
        StringResourceEditorContent(
            project = project,
            selectedResourceIds = viewModel.selectedResourceIds,
            isTranslating = viewModel.isTranslating,
            onUpdateResourceSelection = viewModel::onUpdateResourceSelection,
            onSelectAllResources = viewModel::onSelectAllResources,
            onClearResourceSelection = viewModel::onClearResourceSelection,
            onAddStringResource = viewModel::onAddStringResource,
            onUpdateStringResourceKey = viewModel::onUpdateStringResourceKey,
            onUpdateStringResourceDescription = viewModel::onUpdateStringResourceDescription,
            onUpdateStringResourceValue = viewModel::onUpdateStringResourceValue,
            onDeleteStringResources = viewModel::onDeleteStringResources,
            onUpdateSupportedLocales = viewModel::onUpdateSupportedLocales,
            onRemoveLocale = { locale ->
                // When removing a single locale from the header menu, update the list without that locale
                val currentLocales = project.stringResourceHolder.supportedLocales.toList()
                viewModel.onUpdateSupportedLocales(currentLocales - locale)
            },
            onUpdateDefaultLocale = viewModel::onUpdateDefaultLocale,
            onTranslateStrings = viewModel::onTranslateStrings,
            onTranslateNeedsUpdateStrings = viewModel::onTranslateNeedsUpdateStrings,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun StringResourceEditorContent(
    project: Project,
    selectedResourceIds: Set<String>,
    isTranslating: Boolean,
    onUpdateResourceSelection: (String, Boolean) -> Unit,
    onSelectAllResources: () -> Unit,
    onClearResourceSelection: () -> Unit,
    onAddStringResource: (key: String, description: String, defaultValue: String) -> Unit,
    onUpdateStringResourceKey: (StringResource, String) -> Unit,
    onUpdateStringResourceDescription: (StringResource, String) -> Unit,
    onUpdateStringResourceValue: (StringResource, ResourceLocale, String) -> Unit,
    onDeleteStringResources: (List<StringResource>) -> Unit,
    onUpdateSupportedLocales: (List<ResourceLocale>) -> Unit,
    onRemoveLocale: (ResourceLocale) -> Unit,
    onUpdateDefaultLocale: (ResourceLocale) -> Unit,
    onTranslateStrings: () -> Unit,
    onTranslateNeedsUpdateStrings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var addResourceDialogOpen by remember { mutableStateOf(false) }
    var editLocalesDialogOpen by remember { mutableStateOf(false) }
    var localeToDelete by remember { mutableStateOf<ResourceLocale?>(null) }
    var showDeleteMultipleDialog by remember { mutableStateOf(false) }

    val allSelected by remember {
        derivedStateOf {
            project.stringResourceHolder.stringResources.isNotEmpty() &&
                selectedResourceIds.size == project.stringResourceHolder.stringResources.size
        }
    }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current

    val defaultLocale by project.stringResourceHolder.defaultLocale
    val supportedLocales = project.stringResourceHolder.supportedLocales.sortedBy { it.ordinal }

    var keyColumnWidth by remember { mutableStateOf(DEFAULT_COLUMN_WIDTH) }
    var descriptionColumnWidth by remember { mutableStateOf(DEFAULT_COLUMN_WIDTH) }
    val localeColumnWidths = remember { mutableStateMapOf<ResourceLocale, Dp>() }
    LaunchedEffect(supportedLocales) {
        supportedLocales.forEach { locale ->
            if (locale !in localeColumnWidths) {
                localeColumnWidths[locale] = DEFAULT_COLUMN_WIDTH
            }
        }
        localeColumnWidths.keys.retainAll(supportedLocales.toSet())
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .backgroundContainerNeutral()
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(16.dp),
    ) {
        var tableWidthPx by remember { mutableStateOf(0) }
        val tableWidthDp = with(LocalDensity.current) { tableWidthPx.toDp() }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.widthIn(min = tableWidthDp).padding(bottom = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.string_resources),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(32.dp))
                if (isAiEnabled()) {
                    val hasResourcesNeedingUpdate = project.stringResourceHolder.stringResources.any { it.needsTranslationUpdate }
                    val hasSelectedResources = selectedResourceIds.isNotEmpty()

                    if (hasSelectedResources) {
                        // Show "Translate Strings" button for selected resources
                        TranslateStringsButton(
                            onClick = { onTranslateStrings() },
                            enabled = !isTranslating,
                            isTranslating = isTranslating,
                        )
                    } else if (hasResourcesNeedingUpdate) {
                        // Show "Update Translations" button for flagged resources
                        UpdateTranslationsButton(
                            onClick = { onTranslateNeedsUpdateStrings() },
                            enabled = !isTranslating,
                            isTranslating = isTranslating,
                        )
                    } else {
                        // Show disabled "Update Translations" button when nothing to translate
                        UpdateTranslationsButton(
                            onClick = { },
                            enabled = false,
                            isTranslating = false,
                        )
                    }
                } else {
                    Tooltip(stringResource(Res.string.ai_login_needed)) {
                        UpdateTranslationsButton(
                            onClick = { },
                            enabled = false,
                            isTranslating = false,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = { showDeleteMultipleDialog = true },
                    enabled = selectedResourceIds.isNotEmpty(),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.delete_string_resources))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            TextButton(
                onClick = { editLocalesDialogOpen = true },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(Res.string.edit_supported_locales))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val horizontalScrollState = rememberScrollState()
        StringResourceTableHeaderRow(
            supportedLocales = supportedLocales,
            defaultLocale = defaultLocale,
            onUpdateDefaultLocale = onUpdateDefaultLocale,
            onRemoveLocale = { locale -> localeToDelete = locale },
            allSelected = allSelected,
            onSelectAll = { selected ->
                if (selected) {
                    onSelectAllResources()
                } else {
                    onClearResourceSelection()
                }
            },
            keyColumnWidth = keyColumnWidth,
            descriptionColumnWidth = descriptionColumnWidth,
            localeColumnWidths = localeColumnWidths,
            onKeyColumnWidthChange = { delta ->
                keyColumnWidth = (keyColumnWidth + delta).coerceAtLeast(DEFAULT_COLUMN_WIDTH)
            },
            onDescriptionColumnWidthChange = { delta ->
                descriptionColumnWidth = (descriptionColumnWidth + delta).coerceAtLeast(DEFAULT_COLUMN_WIDTH)
            },
            onLocaleColumnWidthChange = { locale, delta ->
                val currentWidth = localeColumnWidths[locale] ?: DEFAULT_COLUMN_WIDTH
                localeColumnWidths[locale] = (currentWidth + delta).coerceAtLeast(DEFAULT_COLUMN_WIDTH)
            },
            horizontalScrollState = horizontalScrollState,
            modifier = Modifier.onSizeChanged { tableWidthPx = it.width },
        )
        HorizontalDivider(modifier = Modifier.width(tableWidthDp))

        TextButton(
            shape = RectangleShape,
            onClick = {
                addResourceDialogOpen = true
            },
            modifier = Modifier.width(tableWidthDp),
        ) {
            Text(text = stringResource(Res.string.add_string_resource))
        }
        HorizontalDivider(modifier = Modifier.width(tableWidthDp))

        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            items(project.stringResourceHolder.stringResources) { resource ->
                StringResourceTableDataRow(
                    resource = resource,
                    supportedLocales = supportedLocales,
                    defaultLocale = defaultLocale,
                    isSelected = selectedResourceIds.contains(resource.id),
                    onSelectionChange = { selected ->
                        onUpdateResourceSelection(resource.id, selected)
                    },
                    onUpdateKey = { onUpdateStringResourceKey(resource, it) },
                    onUpdateDescription = {
                        onUpdateStringResourceDescription(
                            resource,
                            it,
                        )
                    },
                    onUpdateValue = { locale, value ->
                        onUpdateStringResourceValue(resource, locale, value)
                    },
                    keyColumnWidth = keyColumnWidth,
                    descriptionColumnWidth = descriptionColumnWidth,
                    localeColumnWidths = localeColumnWidths,
                    horizontalScrollState = horizontalScrollState,
                )
                HorizontalDivider(modifier = Modifier.width(tableWidthDp))
            }
        }
    }

    if (addResourceDialogOpen) {
        onAnyDialogIsShown()
        AddNewResourceDialog(
            defaultLocale = defaultLocale,
            onAddResource = { key, description, defaultValue ->
                onAddStringResource(key, description, defaultValue)
                addResourceDialogOpen = false
                onAllDialogsClosed()
            },
            onDismiss = {
                addResourceDialogOpen = false
                onAllDialogsClosed()
            },
        )
    }

    if (editLocalesDialogOpen) {
        onAnyDialogIsShown()
        EditSupportedLocalesDialog(
            currentLocales = supportedLocales,
            defaultLocale = defaultLocale,
            onUpdateLocales = { newLocales ->
                onUpdateSupportedLocales(newLocales)
                editLocalesDialogOpen = false
                onAllDialogsClosed()
            },
            onDismiss = {
                editLocalesDialogOpen = false
                onAllDialogsClosed()
            },
        )
    }

    localeToDelete?.let { locale ->
        onAnyDialogIsShown()
        SimpleConfirmationDialog(
            text =
                stringResource(
                    Res.string.remove_locale_confirmation,
                    stringResource(locale.displayNameResource),
                ),
            onConfirmClick = {
                onRemoveLocale(locale)
                localeToDelete = null
                onAllDialogsClosed()
            },
            onCloseClick = {
                localeToDelete = null
                onAllDialogsClosed()
            },
            positiveText = stringResource(Res.string.remove),
        )
    }

    if (showDeleteMultipleDialog) {
        onAnyDialogIsShown()
        val selectedResources =
            project.stringResourceHolder.stringResources
                .filter { selectedResourceIds.contains(it.id) }

        SimpleConfirmationDialog(
            text =
                if (selectedResources.size == 1) {
                    stringResource(Res.string.delete_string_resource_confirmation, selectedResources.first().key)
                } else {
                    stringResource(Res.string.delete_string_resources_confirmation, selectedResources.size)
                },
            onConfirmClick = {
                onDeleteStringResources(selectedResources)
                showDeleteMultipleDialog = false
                onAllDialogsClosed()
            },
            onCloseClick = {
                showDeleteMultipleDialog = false
                onAllDialogsClosed()
            },
            positiveText = stringResource(Res.string.remove),
        )
    }
}

@Composable
private fun TranslateStringsButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isTranslating: Boolean,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Outlined.Translate,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text =
                if (isTranslating) {
                    stringResource(Res.string.translating_strings)
                } else {
                    stringResource(Res.string.translate_selected_strings)
                },
        )
    }
}

@Composable
private fun UpdateTranslationsButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isTranslating: Boolean,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary,
            ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Sync,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text =
                if (isTranslating) {
                    stringResource(Res.string.updating_translations)
                } else {
                    stringResource(Res.string.update_translations)
                },
        )
    }
}

@Composable
private fun StringResourceTableHeaderRow(
    supportedLocales: List<ResourceLocale>,
    defaultLocale: ResourceLocale,
    onUpdateDefaultLocale: (ResourceLocale) -> Unit,
    onRemoveLocale: (ResourceLocale) -> Unit,
    allSelected: Boolean,
    onSelectAll: (Boolean) -> Unit,
    keyColumnWidth: Dp,
    descriptionColumnWidth: Dp,
    localeColumnWidths: Map<ResourceLocale, Dp>,
    onKeyColumnWidthChange: (Dp) -> Unit,
    onDescriptionColumnWidthChange: (Dp) -> Unit,
    onLocaleColumnWidthChange: (ResourceLocale, Dp) -> Unit,
    horizontalScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val nonDefaultLocales = supportedLocales.filter { it != defaultLocale }

    Row(modifier.height(IntrinsicSize.Max)) {
        // Fixed header columns
        Row {
            Box(
                modifier = Modifier.width(40.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = onSelectAll,
                )
            }
            VerticalDivider()
            Tooltip(
                text = stringResource(Res.string.string_resource_key_tooltip),
            ) {
                Box(
                    modifier = Modifier.width(keyColumnWidth).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(Res.string.string_resource_key),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
            DraggableColumnDivider(
                onDrag = onKeyColumnWidthChange,
            )
            Tooltip(
                text = stringResource(Res.string.string_resource_description_tooltip),
            ) {
                Box(
                    modifier = Modifier.width(descriptionColumnWidth).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(Res.string.string_resource_description),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
            DraggableColumnDivider(
                onDrag = onDescriptionColumnWidthChange,
            )

            // Default locale column
            val defaultLocaleWidth = localeColumnWidths[defaultLocale] ?: DEFAULT_COLUMN_WIDTH
            LocaleHeaderCell(
                locale = defaultLocale,
                isDefault = true,
                onSetAsDefault = { },
                onRemove = { onRemoveLocale(defaultLocale) },
                modifier = Modifier.width(defaultLocaleWidth).fillMaxHeight(),
            )
            DraggableColumnDivider(
                onDrag = { delta -> onLocaleColumnWidthChange(defaultLocale, delta) },
                isVisible = nonDefaultLocales.isNotEmpty(),
            )
        }

        // Scrollable locale columns (excluding default)
        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            nonDefaultLocales.forEach { locale ->
                val localeWidth = localeColumnWidths[locale] ?: DEFAULT_COLUMN_WIDTH
                LocaleHeaderCell(
                    locale = locale,
                    isDefault = false,
                    onSetAsDefault = { onUpdateDefaultLocale(locale) },
                    onRemove = { onRemoveLocale(locale) },
                    modifier = Modifier.width(localeWidth).fillMaxHeight(),
                )
                DraggableColumnDivider(
                    onDrag = { delta -> onLocaleColumnWidthChange(locale, delta) },
                    isVisible = locale != nonDefaultLocales.last(),
                )
            }
        }
    }
}

@Composable
private fun StringResourceTableDataRow(
    resource: StringResource,
    supportedLocales: List<ResourceLocale>,
    defaultLocale: ResourceLocale,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onUpdateKey: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateValue: (ResourceLocale, String) -> Unit,
    keyColumnWidth: Dp,
    descriptionColumnWidth: Dp,
    localeColumnWidths: Map<ResourceLocale, Dp>,
    horizontalScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    val nonDefaultLocales = supportedLocales.filter { it != defaultLocale }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(IntrinsicSize.Max),
    ) {
        // Fixed columns
        Row(modifier = Modifier.width(IntrinsicSize.Max)) {
            Box(
                modifier = Modifier.width(40.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChange,
                )
            }
            VerticalDivider()
            Row(
                modifier = Modifier.width(keyColumnWidth).fillMaxHeight().padding(4.dp),
            ) {
                CellEditableText(
                    initialText = resource.key,
                    onValueChange = onUpdateKey,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                )
                if (resource.needsTranslationUpdate) {
                    Tooltip(
                        text = stringResource(Res.string.needs_translation_update),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = stringResource(Res.string.needs_translation_update),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
            ColumnDivider()
            Box(
                modifier = Modifier.width(descriptionColumnWidth).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                CellEditableText(
                    initialText = resource.description ?: "",
                    onValueChange = onUpdateDescription,
                    textStyle =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                )
            }
            ColumnDivider()

            // Default locale column
            val defaultLocaleWidth = localeColumnWidths[defaultLocale] ?: DEFAULT_COLUMN_WIDTH
            Box(
                modifier = Modifier.width(defaultLocaleWidth).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                CellEditableText(
                    initialText = resource.localizedValues[defaultLocale] ?: "",
                    onValueChange = { onUpdateValue(defaultLocale, it) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    enabled = true,
                )
            }
            ColumnDivider(isVisible = nonDefaultLocales.isNotEmpty())
        }

        // Scrollable locale columns (excluding default)
        Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            nonDefaultLocales.forEach { locale ->
                val localeWidth = localeColumnWidths[locale] ?: DEFAULT_COLUMN_WIDTH
                Box(
                    modifier = Modifier.width(localeWidth).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    CellEditableText(
                        initialText = resource.localizedValues[locale] ?: "",
                        onValueChange = { onUpdateValue(locale, it) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        enabled = false,
                    )
                }
                ColumnDivider(isVisible = locale != nonDefaultLocales.last())
            }
        }
    }
}

@Composable
private fun LocaleHeaderCell(
    locale: ResourceLocale,
    isDefault: Boolean,
    onSetAsDefault: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        locale.flagEmoji?.let { flag ->
            Text(
                text = flag,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text =
                buildString {
                    append(stringResource(locale.displayNameResource))
                    if (isDefault) {
                        append(stringResource(Res.string.default_locale_label))
                    }
                },
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { showDropdown = true },
            modifier = Modifier.size(20.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.more_options),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
            )
        }

        CursorDropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
        ) {
            if (!isDefault) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.set_as_default_locale)) },
                    onClick = {
                        onSetAsDefault()
                        showDropdown = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.remove_locale)) },
                onClick = {
                    onRemove()
                    showDropdown = false
                },
                enabled = !isDefault,
            )
        }
    }
}

@Composable
private fun AddNewResourceDialog(
    defaultLocale: ResourceLocale,
    onAddResource: (key: String, description: String, defaultValue: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var resourceKey by remember { mutableStateOf("") }
    var resourceDescription by remember { mutableStateOf("") }
    var resourceDefaultValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_new_string_resource)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                SmallOutlinedTextField(
                    value = resourceKey,
                    onValueChange = { resourceKey = it },
                    placeholder = { Text(stringResource(Res.string.string_resource_key_placeholder)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .consumeTabKeyEventForFocus(focusManager),
                )
                SmallOutlinedTextField(
                    value = resourceDescription,
                    onValueChange = { resourceDescription = it },
                    placeholder = { Text(stringResource(Res.string.string_resource_description_placeholder)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .consumeTabKeyEventForFocus(focusManager),
                )
                SmallOutlinedTextField(
                    value = resourceDefaultValue,
                    onValueChange = { resourceDefaultValue = it },
                    placeholder = {
                        Text(
                            stringResource(
                                Res.string.string_resource_default_value_placeholder,
                                stringResource(defaultLocale.displayNameResource),
                            ),
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .consumeTabKeyEventForFocus(focusManager),
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAddResource(resourceKey, resourceDescription, resourceDefaultValue)
                },
            ) {
                Text(stringResource(Res.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun EditSupportedLocalesDialog(
    currentLocales: List<ResourceLocale>,
    defaultLocale: ResourceLocale,
    onUpdateLocales: (List<ResourceLocale>) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialLocalesSet = remember(currentLocales) { currentLocales.toSet() }
    var selectedLocales by remember(currentLocales) { mutableStateOf(currentLocales.toSet()) }
    val hasChanges = selectedLocales != initialLocalesSet

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_supported_locales)) },
        text = {
            Column {
                var searchQuery by remember { mutableStateOf("") }

                val allLocales = ResourceLocale.entries
                val displayNames =
                    allLocales.associateWith { locale ->
                        stringResource(locale.displayNameResource)
                    }
                val filteredLocales =
                    remember(searchQuery, displayNames) {
                        if (searchQuery.isBlank()) {
                            allLocales
                        } else {
                            allLocales.filter { locale ->
                                displayNames[locale]?.startsWith(searchQuery, ignoreCase = true) == true ||
                                    locale.languageCode.startsWith(searchQuery, ignoreCase = true) ||
                                    locale.name.startsWith(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                // Calculate if all visible non-default locales are selected
                val visibleNonDefaultLocales = filteredLocales.filter { it != defaultLocale }
                val allVisibleSelected =
                    visibleNonDefaultLocales.isNotEmpty() &&
                        visibleNonDefaultLocales.all { selectedLocales.contains(it) }

                SmallOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(Res.string.search_locales_placeholder)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true,
                )

                // Select/Deselect all locales
                if (visibleNonDefaultLocales.isNotEmpty()) {
                    Row(
                        modifier =
                            Modifier.align(Alignment.End).padding(start = 16.dp, end = 16.dp, bottom = 8.dp).onClick {
                                selectedLocales =
                                    if (allVisibleSelected) {
                                        selectedLocales - visibleNonDefaultLocales.toSet()
                                    } else {
                                        selectedLocales + visibleNonDefaultLocales
                                    }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.select_all),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Checkbox(
                            checked = allVisibleSelected,
                            onCheckedChange = { checked ->
                                selectedLocales =
                                    if (checked) {
                                        selectedLocales + visibleNonDefaultLocales
                                    } else {
                                        selectedLocales - visibleNonDefaultLocales.toSet()
                                    }
                            },
                        )
                    }
                }

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (filteredLocales.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Text(
                                text = stringResource(Res.string.no_locales_found_matching, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                            )
                        }
                    } else {
                        items(filteredLocales) { locale ->
                            val isSelected = selectedLocales.contains(locale)
                            val isDefault = locale == defaultLocale
                            Surface(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isDefault) {
                                            selectedLocales =
                                                if (isSelected) {
                                                    selectedLocales - locale
                                                } else {
                                                    selectedLocales + locale
                                                }
                                        },
                                color =
                                    when {
                                        isDefault -> MaterialTheme.colorScheme.surfaceVariant
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                contentColor =
                                    when {
                                        isDefault -> MaterialTheme.colorScheme.onSurfaceVariant
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = locale.flagEmoji.orEmpty(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = LocalContentColor.current.copy(alpha = if (isDefault) 0.6f else 1f),
                                        modifier = Modifier.width(44.dp),
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(locale.displayNameResource),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = LocalContentColor.current.copy(alpha = if (isDefault) 0.6f else 1f),
                                        )
                                        Text(
                                            text = locale.languageCode,
                                            style = MaterialTheme.typography.bodySmall,
                                            color =
                                                when {
                                                    isDefault -> LocalContentColor.current.copy(0.6f)
                                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (isDefault) {
                                        Text(
                                            text = stringResource(Res.string.default_locale_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LocalContentColor.current.copy(alpha = 0.6f),
                                        )
                                    } else {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedLocales =
                                                    if (checked) {
                                                        selectedLocales + locale
                                                    } else {
                                                        selectedLocales - locale
                                                    }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdateLocales(selectedLocales.toList())
                },
                enabled = hasChanges,
            ) {
                Text(stringResource(Res.string.apply_change))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun DraggableColumnDivider(
    onDrag: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    var isHovered by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val draggableState =
        rememberDraggableState { deltaPx ->
            with(density) {
                onDrag(deltaPx.toDp())
            }
        }

    Box(
        modifier =
            modifier
                .width(8.dp)
                .fillMaxHeight()
                .pointerHoverIcon(PointerIconResizeHorizontal)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> isHovered = true
                                PointerEventType.Exit -> isHovered = false
                            }
                        }
                    }
                }.draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { isDragging = true },
                    onDragStopped = { isDragging = false },
                ).background(
                    if (isHovered || isDragging) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        Color.Transparent
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (isVisible) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun ColumnDivider(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .width(8.dp)
                .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        if (isVisible) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
            )
        }
    }
}

private fun Modifier.consumeTabKeyEventForFocus(focusManager: FocusManager): Modifier =
    this.onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
            focusManager.moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
            true
        } else {
            false
        }
    }
