package io.composeflow.ui.inspector.modifier

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.remove_modifier
import io.composeflow.tap_to_hide
import io.composeflow.tap_to_show
import io.composeflow.toggle_visibility
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverIconClickable
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.utils.TreeExpander
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableCollectionItemScope

val LocalReorderableCollectionItemScope =
    compositionLocalOf<ReorderableCollectionItemScope?> { null }

@Composable
fun ProvideModifierReorderAllowed(
    reorderableCollectionItemScope: ReorderableCollectionItemScope,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalReorderableCollectionItemScope providesDefault reorderableCollectionItemScope,
    ) {
        content()
    }
}

@Composable
fun ModifierInspectorContainer(
    node: ComposeNode,
    wrapper: ModifierWrapper,
    modifierIndex: Int,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onVisibilityToggleClicked: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    // Use external state if provided, otherwise use local state
    var localExpanded by remember { mutableStateOf(false) }
    val actualExpanded = expanded ?: localExpanded

    val setExpanded = { value: Boolean ->
        if (onExpandedChange != null) {
            onExpandedChange(value)
        } else {
            localExpanded = value
        }
    }
    Column(
        modifier =
            modifier
                .hoverOverlay()
                .padding(end = 8.dp)
                .animateContentSize(keyframes { durationMillis = 100 }),
    ) {
        val reorderScope = LocalReorderableCollectionItemScope.current
        if (reorderScope != null) {
            with(reorderScope) {
                ModifierInspectorHeaderRow(
                    expanded = actualExpanded,
                    wrapper = wrapper,
                    onExpandButtonClicked = { setExpanded(!actualExpanded) },
                    onDeleteButtonClicked = {
                        composeNodeCallbacks.onModifierRemovedAt(node, modifierIndex)
                    },
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                    modifier = Modifier.draggableHandle(),
                )
            }
        } else {
            ModifierInspectorHeaderRow(
                expanded = actualExpanded,
                wrapper = wrapper,
                onExpandButtonClicked = { setExpanded(!actualExpanded) },
                onDeleteButtonClicked = {
                    composeNodeCallbacks.onModifierRemovedAt(node, modifierIndex)
                },
                onVisibilityToggleClicked = onVisibilityToggleClicked,
            )
        }

        if (actualExpanded) {
            content()
        }
    }
}

@Composable
fun ModifierInspectorHeaderRow(
    expanded: Boolean,
    wrapper: ModifierWrapper,
    onExpandButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit,
    onVisibilityToggleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onExpandButtonClicked()
                },
    ) {
        TreeExpander(
            expanded = expanded,
            onClick = onExpandButtonClicked,
        )
        Text(
            text = wrapper.displayName(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Spacer(modifier = Modifier.weight(1f))

        Tooltip(stringResource(Res.string.toggle_visibility)) {
            ComposeFlowIconButton(
                onClick = onVisibilityToggleClicked,
                modifier = Modifier.hoverIconClickable(),
            ) {
                val icon =
                    if (wrapper.visible.value) {
                        Icons.Outlined.Visibility
                    } else {
                        Icons.Outlined.VisibilityOff
                    }
                val contentDesc =
                    if (wrapper.visible.value) {
                        stringResource(Res.string.tap_to_hide)
                    } else {
                        stringResource(
                            Res.string.tap_to_show,
                        )
                    }
                ComposeFlowIcon(
                    imageVector = icon,
                    contentDescription = contentDesc,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        val reorderScope = LocalReorderableCollectionItemScope.current
        if (reorderScope != null) {
            ComposeFlowIcon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        val removeContentDesc = stringResource(Res.string.remove_modifier)
        Tooltip(removeContentDesc) {
            ComposeFlowIconButton(
                onClick = onDeleteButtonClicked,
                modifier = Modifier.hoverIconClickable(),
            ) {
                ComposeFlowIcon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = removeContentDesc,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
