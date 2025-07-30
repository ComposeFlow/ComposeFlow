package io.composeflow.ui.string

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.action_add
import io.composeflow.add_new_string_resource
import io.composeflow.add_string_resource
import io.composeflow.apply_change
import io.composeflow.auth.LocalFirebaseIdToken
import io.composeflow.cancel
import io.composeflow.default_locale_label
import io.composeflow.delete
import io.composeflow.delete_string_resource_confirmation
import io.composeflow.delete_string_resources
import io.composeflow.delete_string_resources_confirmation
import io.composeflow.edit_supported_locales
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.ResourceLocale
import io.composeflow.model.project.string.StringResource
import io.composeflow.more_options
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
import io.composeflow.string_resource_key
import io.composeflow.string_resource_key_placeholder
import io.composeflow.string_resources
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.modifier.backgroundContainerNeutral
import io.composeflow.ui.popup.SimpleConfirmationDialog
import io.composeflow.ui.textfield.SmallOutlinedTextField
import moe.tlaster.precompose.viewmodel.viewModel
import org.jetbrains.compose.resources.stringResource

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
            onAddStringResource = viewModel::onAddStringResource,
            onUpdateStringResourceKey = viewModel::onUpdateStringResourceKey,
            onUpdateStringResourceDescription = viewModel::onUpdateStringResourceDescription,
            onUpdateStringResourceValue = viewModel::onUpdateStringResourceValue,
            onDeleteStringResource = viewModel::onDeleteStringResource,
            onUpdateSupportedLocales = viewModel::onUpdateSupportedLocales,
            onRemoveLocale = { locale ->
                // When removing a single locale from the header menu, update the list without that locale
                val currentLocales = project.stringResourceHolder.supportedLocales.toList()
                viewModel.onUpdateSupportedLocales(currentLocales - locale)
            },
            onUpdateDefaultLocale = viewModel::onUpdateDefaultLocale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun StringResourceEditorContent(
    project: Project,
    onAddStringResource: (key: String, description: String, defaultValue: String) -> Unit,
    onUpdateStringResourceKey: (StringResource, String) -> Unit,
    onUpdateStringResourceDescription: (StringResource, String) -> Unit,
    onUpdateStringResourceValue: (StringResource, ResourceLocale, String) -> Unit,
    onDeleteStringResource: (StringResource) -> Unit,
    onUpdateSupportedLocales: (List<ResourceLocale>) -> Unit,
    onRemoveLocale: (ResourceLocale) -> Unit,
    onUpdateDefaultLocale: (ResourceLocale) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addResourceDialogOpen by remember { mutableStateOf(false) }
    var addLocaleDialogOpen by remember { mutableStateOf(false) }
    var localeToDelete by remember { mutableStateOf<ResourceLocale?>(null) }
    var selectedResources by remember { mutableStateOf(setOf<StringResource>()) }
    var showDeleteMultipleDialog by remember { mutableStateOf(false) }

    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
    val onAllDialogsClosed = LocalOnAllDialogsClosed.current

    val defaultLocale by project.stringResourceHolder.defaultLocale
    val supportedLocales =
        project.stringResourceHolder.supportedLocales.sortedWith { a, b ->
            when {
                // Default locale always comes first
                a == defaultLocale -> -1
                b == defaultLocale -> 1
                // Otherwise sort by ordinal value
                else -> a.ordinal.compareTo(b.ordinal)
            }
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
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.string_resources),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                TextButton(
                    onClick = { showDeleteMultipleDialog = true },
                    enabled = selectedResources.isNotEmpty(),
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            val horizontalScrollState = rememberScrollState()
            Column(
                modifier = Modifier.horizontalScroll(horizontalScrollState),
            ) {
                var tableWidthPx by remember { mutableStateOf(0) }
                val tableWidthDp = with(LocalDensity.current) { tableWidthPx.toDp() }

                StringResourceTableHeader(
                    supportedLocales = supportedLocales,
                    defaultLocale = defaultLocale,
                    onUpdateDefaultLocale = onUpdateDefaultLocale,
                    onRemoveLocale = { locale -> localeToDelete = locale },
                    onEditLocales = { addLocaleDialogOpen = true },
                    allSelected =
                        project.stringResourceHolder.stringResources.isNotEmpty() &&
                            project.stringResourceHolder.stringResources.all { selectedResources.contains(it) },
                    onSelectAll = { selected ->
                        selectedResources =
                            if (selected) {
                                project.stringResourceHolder.stringResources.toSet()
                            } else {
                                emptySet()
                            }
                    },
                    modifier = Modifier.onSizeChanged { tableWidthPx = it.width },
                )

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
                        StringResourceTableRow(
                            resource = resource,
                            supportedLocales = supportedLocales,
                            defaultLocale = defaultLocale,
                            isSelected = selectedResources.contains(resource),
                            onSelectionChange = { selected ->
                                selectedResources =
                                    if (selected) {
                                        selectedResources + resource
                                    } else {
                                        selectedResources - resource
                                    }
                            },
                            onUpdateKey = { onUpdateStringResourceKey(resource, it) },
                            onUpdateDescription = { onUpdateStringResourceDescription(resource, it) },
                            onUpdateValue = { locale, value ->
                                onUpdateStringResourceValue(resource, locale, value)
                            },
                        )
                    }
                }
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

    if (addLocaleDialogOpen) {
        onAnyDialogIsShown()
        EditSupportedLocalesDialog(
            currentLocales = supportedLocales,
            defaultLocale = defaultLocale,
            onUpdateLocales = { newLocales ->
                onUpdateSupportedLocales(newLocales)
                addLocaleDialogOpen = false
                onAllDialogsClosed()
            },
            onDismiss = {
                addLocaleDialogOpen = false
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
        SimpleConfirmationDialog(
            text =
                if (selectedResources.size == 1) {
                    stringResource(Res.string.delete_string_resource_confirmation, selectedResources.first().key)
                } else {
                    stringResource(Res.string.delete_string_resources_confirmation, selectedResources.size)
                },
            onConfirmClick = {
                selectedResources.forEach { resource ->
                    onDeleteStringResource(resource)
                }
                selectedResources = emptySet()
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
private fun StringResourceTableHeader(
    supportedLocales: List<ResourceLocale>,
    defaultLocale: ResourceLocale,
    onUpdateDefaultLocale: (ResourceLocale) -> Unit,
    onRemoveLocale: (ResourceLocale) -> Unit,
    onEditLocales: () -> Unit,
    allSelected: Boolean,
    onSelectAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.width(IntrinsicSize.Max)) {
        Row(Modifier.height(IntrinsicSize.Max)) {
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
            Box(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(Res.string.string_resource_key),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            VerticalDivider()
            Box(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(Res.string.string_resource_description),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            supportedLocales.forEach { locale ->
                VerticalDivider()
                LocaleHeaderCell(
                    locale = locale,
                    isDefault = locale == defaultLocale,
                    onSetAsDefault = { onUpdateDefaultLocale(locale) },
                    onRemove = { onRemoveLocale(locale) },
                    modifier = Modifier.width(200.dp).fillMaxHeight(),
                )
            }
            VerticalDivider()
            Box(
                modifier = Modifier.width(40.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onEditLocales,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(Res.string.edit_supported_locales),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun StringResourceTableRow(
    resource: StringResource,
    supportedLocales: List<ResourceLocale>,
    defaultLocale: ResourceLocale,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onUpdateKey: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateValue: (ResourceLocale, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.width(IntrinsicSize.Max)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.height(IntrinsicSize.Max),
        ) {
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
            Box(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                CellEditableText(
                    initialText = resource.key,
                    onValueChange = onUpdateKey,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            VerticalDivider()

            Box(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                CellEditableText(
                    initialText = resource.description ?: "",
                    onValueChange = onUpdateDescription,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            supportedLocales.forEach { locale ->
                VerticalDivider()
                Box(
                    modifier = Modifier.width(200.dp).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    CellEditableText(
                        initialText = resource.localizedValues[locale] ?: "",
                        onValueChange = { onUpdateValue(locale, it) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        enabled = locale == defaultLocale,
                    )
                }
            }
        }
        HorizontalDivider()
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
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                                        color =
                                            if (isDefault) {
                                                LocalContentColor.current.copy(0.6f)
                                            } else {
                                                LocalContentColor.current
                                            },
                                        modifier = Modifier.width(44.dp),
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(locale.displayNameResource),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color =
                                                if (isDefault) {
                                                    LocalContentColor.current.copy(0.6f)
                                                } else {
                                                    LocalContentColor.current
                                                },
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

private fun Modifier.consumeTabKeyEventForFocus(focusManager: FocusManager): Modifier =
    this.onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
            focusManager.moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
            true
        } else {
            false
        }
    }
