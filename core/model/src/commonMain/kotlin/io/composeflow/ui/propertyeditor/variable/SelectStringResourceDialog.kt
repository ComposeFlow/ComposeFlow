package io.composeflow.ui.propertyeditor.variable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_new_string_resource
import io.composeflow.cancel
import io.composeflow.confirm
import io.composeflow.initialize
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.project.Project
import io.composeflow.model.project.string.StringResource
import io.composeflow.model.project.string.addStringResources
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.StringProperty
import io.composeflow.model.property.mergeProperty
import io.composeflow.no_string_resources_available
import io.composeflow.search_string_resources
import io.composeflow.select_string_resource
import io.composeflow.ui.popup.PositionCustomizablePopup
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectStringResourceDialog(
    project: Project,
    initialProperty: AssignableProperty?,
    onCloseClick: () -> Unit,
    onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit,
    onInitializeProperty: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val initialStringValue = (initialProperty as? StringProperty.StringIntrinsicValue)?.value?.takeIf { it.isNotEmpty() }
    val initialResourceId = initialProperty?.let { (it as? StringProperty.ValueFromStringResource)?.stringResourceId }
    var selectedResourceId by remember {
        mutableStateOf(initialResourceId)
    }
    var selectionChanged by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    PositionCustomizablePopup(
        onDismissRequest = onCloseClick,
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                onCloseClick()
                true
            } else {
                false
            }
        },
    ) {
        Surface(
            modifier = modifier,
        ) {
            Column(
                modifier =
                    Modifier
                        .width(500.dp)
                        .height(600.dp)
                        .padding(16.dp),
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.select_string_resource),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(Res.string.search_string_resources)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // String resources list
                if (project.stringResourceHolder.stringResources.isNotEmpty() || initialStringValue != null) {
                    val filteredResources =
                        if (searchQuery.isBlank()) {
                            project.stringResourceHolder.stringResources
                        } else {
                            project.stringResourceHolder.stringResources.filter { resource ->
                                resource.key.contains(searchQuery, ignoreCase = true) ||
                                    resource.localizedValues[project.stringResourceHolder.defaultLocale.value]
                                        ?.contains(searchQuery, ignoreCase = true) == true
                            }
                        }

                    LazyColumn(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (initialStringValue != null && searchQuery.isBlank()) {
                            item {
                                val interactionSource = remember { MutableInteractionSource() }
                                val isHovered by interactionSource.collectIsHoveredAsState()

                                AddNewStringResourceCard(
                                    initialStringValue = initialStringValue,
                                    isHovered = isHovered,
                                    onClick = {
                                        val newResource =
                                            StringResource(
                                                key = "", // Infer key from the initial string value
                                                localizedValues =
                                                    mutableMapOf(
                                                        project.stringResourceHolder.defaultLocale.value to
                                                            initialStringValue,
                                                    ),
                                            )
                                        val errors = project.stringResourceHolder.addStringResources(listOf(newResource))
                                        if (errors.isEmpty()) {
                                            onValidPropertyChanged(
                                                initialProperty.mergeProperty(
                                                    project = project,
                                                    newProperty =
                                                        StringProperty.ValueFromStringResource(
                                                            stringResourceId = newResource.id,
                                                        ),
                                                ),
                                                null,
                                            )
                                            onCloseClick()
                                        }
                                    },
                                    modifier = Modifier.hoverable(interactionSource = interactionSource, enabled = true),
                                )
                            }
                        }

                        items(filteredResources) { stringResource ->
                            val defaultValue = stringResource.localizedValues[project.stringResourceHolder.defaultLocale.value].orEmpty()
                            val isSelected = selectedResourceId == stringResource.id
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()

                            StringResourceCard(
                                stringResource = stringResource,
                                defaultValue = defaultValue,
                                isSelected = isSelected,
                                isHovered = isHovered,
                                onClick = {
                                    selectedResourceId = if (isSelected) null else stringResource.id
                                    selectionChanged = true
                                },
                                modifier = Modifier.hoverable(interactionSource = interactionSource, enabled = true),
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.no_string_resources_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    )
                }

                // Buttons
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                ) {
                    // Initialize button
                    if (initialProperty != null) {
                        OutlinedButton(
                            onClick = {
                                if (onInitializeProperty != null) {
                                    onInitializeProperty()
                                } else {
                                    onValidPropertyChanged(
                                        StringProperty.StringIntrinsicValue(""),
                                        null,
                                    )
                                }
                                onCloseClick()
                            },
                        ) {
                            Text(
                                text = stringResource(Res.string.initialize),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))

                    // Cancel button
                    TextButton(
                        onClick = onCloseClick,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    // Confirm button
                    OutlinedButton(
                        onClick = {
                            val newProperty =
                                selectedResourceId?.let { resourceId ->
                                    StringProperty.ValueFromStringResource(
                                        stringResourceId = resourceId,
                                    )
                                } ?: StringProperty.StringIntrinsicValue()
                            onValidPropertyChanged(
                                initialProperty.mergeProperty(
                                    project = project,
                                    newProperty = newProperty,
                                ),
                                null,
                            )
                            onCloseClick()
                        },
                        enabled = selectionChanged,
                    ) {
                        Text(stringResource(Res.string.confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNewStringResourceCard(
    initialStringValue: String,
    isHovered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when {
                        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    },
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = initialStringValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.add_new_string_resource),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StringResourceCard(
    stringResource: StringResource,
    defaultValue: String,
    isSelected: Boolean,
    isHovered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            when {
                isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else -> null
            },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = defaultValue,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource.key,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}
