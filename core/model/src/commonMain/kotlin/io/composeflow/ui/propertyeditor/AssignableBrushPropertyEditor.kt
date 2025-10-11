package io.composeflow.ui.propertyeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.composeflow.model.parameter.lazylist.LazyListChildParams
import io.composeflow.model.parameter.wrapper.BrushType
import io.composeflow.model.parameter.wrapper.BrushWrapper
import io.composeflow.model.parameter.wrapper.defaultBrushWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.property.AssignableProperty
import io.composeflow.model.property.BrushProperty
import io.composeflow.model.property.FunctionScopeParameterProperty
import io.composeflow.model.state.StateId
import io.composeflow.model.type.ComposeFlowType
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton

@Composable
fun AssignableBrushPropertyEditor(
    project: Project,
    node: ComposeNode,
    modifier: Modifier = Modifier,
    acceptableType: ComposeFlowType = ComposeFlowType.Brush(isList = false),
    label: String = "",
    initialProperty: AssignableProperty? = null,
    destinationStateId: StateId? = null,
    onValidPropertyChanged: (AssignableProperty, lazyListSource: LazyListChildParams?) -> Unit = { _, _ -> },
    onInitializeProperty: (() -> Unit)? = null,
    functionScopeProperties: List<FunctionScopeParameterProperty> = emptyList(),
    editable: Boolean = true,
) {
    val brushWrapper = (initialProperty as? BrushProperty.BrushIntrinsicValue)?.value
    var showBrushEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Brush preview and controls
        if (brushWrapper != null && brushWrapper.colors.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Brush preview
                Box(
                    modifier =
                        Modifier
                            .size(width = 80.dp, height = 40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush =
                                    brushWrapper.getBrush()
                                        ?: androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(Color.Transparent),
                                        ),
                            ).border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp),
                            ).clickable {
                                if (editable) {
                                    showBrushEditDialog = true
                                }
                            },
                )

                // Brush description
                Text(
                    text = brushWrapper.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                // Edit button
                ComposeFlowIconButton(
                    onClick = { showBrushEditDialog = true },
                    enabled = editable,
                ) {
                    ComposeFlowIcon(
                        Icons.Default.Edit,
                        contentDescription = "Edit brush",
                    )
                }

                // Delete button
                ComposeFlowIconButton(
                    onClick = {
                        onValidPropertyChanged(
                            BrushProperty.BrushIntrinsicValue(
                                BrushWrapper(
                                    brushType = BrushType.LinearGradient,
                                    colors = emptyList(), // Empty colors will make getBrush() return null
                                ),
                            ),
                            null,
                        )
                    },
                    enabled = editable,
                ) {
                    ComposeFlowIcon(
                        Icons.Default.Clear,
                        contentDescription = "Delete brush",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else {
            // Add brush button
            OutlinedButton(
                onClick = {
                    if (onInitializeProperty != null) {
                        onInitializeProperty()
                    } else {
                        onValidPropertyChanged(
                            BrushProperty.BrushIntrinsicValue(defaultBrushWrapper),
                            null,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = editable,
            ) {
                Text("Add Brush")
            }
        }
    }

    // Brush edit dialog
    if (showBrushEditDialog && brushWrapper != null && brushWrapper.colors.isNotEmpty()) {
        BrushEditDialog(
            initialBrush = brushWrapper,
            extendedColors = project.themeHolder.colorSchemeHolder.extendedColors.value,
            onDismissRequest = { showBrushEditDialog = false },
            onConfirm = { newBrush ->
                onValidPropertyChanged(BrushProperty.BrushIntrinsicValue(newBrush), null)
                showBrushEditDialog = false
            },
        )
    }
}
