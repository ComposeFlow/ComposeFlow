package io.composeflow.ui.inspector.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.composeflow.Res
import io.composeflow.add_new_modifier
import io.composeflow.edit_modifier_in_editor
import io.composeflow.model.modifier.ModifierWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNodeCallbacks
import io.composeflow.ui.LocalOnAllDialogsClosed
import io.composeflow.ui.LocalOnAnyDialogIsShown
import io.composeflow.ui.Tooltip
import io.composeflow.ui.icon.ComposeFlowIcon
import io.composeflow.ui.icon.ComposeFlowIconButton
import io.composeflow.ui.modifier.hoverOverlay
import io.composeflow.ui.popup.PositionCustomizablePopup
import io.composeflow.ui.reorderable.ComposeFlowReorderableItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

fun LazyListScope.modifierInspector(
    project: Project,
    listState: LazyListState,
    composeNodeCallbacks: ComposeNodeCallbacks,
) {
    val focusedNodes = project.screenHolder.findFocusedNodes()
    when {
        focusedNodes.isEmpty() -> {
        }

        focusedNodes.size > 1 -> {
        }

        else -> {
            val composeNode = focusedNodes.first()
            item {
                val coroutineScope = rememberCoroutineScope()
                var addModifierDialogVisible by remember { mutableStateOf(false) }
                var editModifierDialogVisible by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Modifiers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    ComposeFlowIconButton(
                        onClick = {
                            addModifierDialogVisible = true
                        },
                        modifier =
                            Modifier
                                .padding(start = 16.dp)
                                .hoverOverlay(),
                    ) {
                        val contentDesc = stringResource(Res.string.add_new_modifier)
                        Tooltip(contentDesc) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    ComposeFlowIconButton(
                        onClick = {
                            editModifierDialogVisible = true
                        },
                        modifier =
                            Modifier
                                .padding(start = 4.dp)
                                .hoverOverlay(),
                    ) {
                        val contentDesc = stringResource(Res.string.edit_modifier_in_editor)
                        Tooltip(contentDesc) {
                            ComposeFlowIcon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = contentDesc,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
                    val onAllDialogsClosed = LocalOnAllDialogsClosed.current
                    if (addModifierDialogVisible) {
                        onAnyDialogIsShown()
                        val modifiers =
                            ModifierWrapper
                                .values()
                                .filter { modifier ->
                                    composeNode.parentNode?.let {
                                        modifier.hasValidParent(it.trait.value)
                                    } ?: true
                                }.mapIndexed { i, modifier ->
                                    i to modifier
                                }

                        AddModifierDialog(
                            modifiers = modifiers,
                            onModifierSelected = {
                                addModifierDialogVisible = false
                                composeNodeCallbacks.onModifierAdded(
                                    composeNode,
                                    modifiers[it].second,
                                )
                                coroutineScope.launch {
                                    listState.animateScrollToItem(it + 1)
                                }
                                onAllDialogsClosed()
                            },
                            onCloseClick = {
                                addModifierDialogVisible = false
                                onAllDialogsClosed()
                            },
                        )
                    }

                    if (editModifierDialogVisible) {
                        EditModifierDialog(
                            project = project,
                            composeNodeCallbacks = composeNodeCallbacks,
                            onCloseDialog = {
                                editModifierDialogVisible = false
                            },
                        )
                    }
                }
            }

            composeNode.modifierList.forEachIndexed { i, chain ->

                val onVisibilityToggleClicked = {
                    chain.visible.value = !chain.visible.value
                    composeNodeCallbacks.onModifierUpdatedAt(composeNode, i, chain)
                }

                item {
                    SingleModifierInspector(
                        project = project,
                        composeNode = composeNode,
                        i = i,
                        chain = chain,
                        composeNodeCallbacks = composeNodeCallbacks,
                        onVisibilityToggleClicked = onVisibilityToggleClicked,
                    )
                }
            }
        }
    }
}

@Composable
fun SingleModifierInspector(
    project: Project,
    composeNode: ComposeNode,
    i: Int,
    chain: ModifierWrapper,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onVisibilityToggleClicked: () -> Unit = {},
    reorderableLazyListState: ReorderableLazyListState? = null,
) {
    val issues =
        if (!composeNode.isContentRoot()) {
            chain.generateIssues(composeNode.parentNode?.trait?.value)
        } else {
            emptyList()
        }

    @Composable
    fun ModifierItem(content: @Composable () -> Unit) {
        var isHighlighted by remember { mutableStateOf(false) }
        val highlightAlpha = animateFloatAsState(if (isHighlighted) 0.4f else 0f)
        if (composeNode.pendingModifierCommittedIndex.value == i) {
            LaunchedEffect(Unit) {
                isHighlighted = true
                delay(1000)
                isHighlighted = false
                composeNode.pendingModifierCommittedIndex.value = null
            }
        }
        val highlight =
            if (isHighlighted) {
                Modifier.background(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = highlightAlpha.value),
                    shape = RoundedCornerShape(16.dp),
                )
            } else {
                Modifier
            }

        val issueContainer =
            if (issues.isNotEmpty()) {
                Modifier.background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                )
            } else {
                Modifier
            }
        if (reorderableLazyListState != null) {
            Column(
                modifier = highlight.then(issueContainer),
            ) {
                content()
            }
        } else {
            if (issues.isNotEmpty()) {
                Tooltip(issues.first().errorMessage(project)) {
                    Column(
                        modifier = highlight.then(issueContainer),
                    ) {
                        content()
                    }
                }
            } else {
                Column(
                    modifier = highlight.then(issueContainer),
                ) {
                    content()
                }
            }
        }
    }

    when (chain) {
        is ModifierWrapper.Align -> {
            ModifierItem {
                AlignModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.AlignHorizontal -> {
            ModifierItem {
                AlignHorizontalModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.AlignVertical -> {
            ModifierItem {
                AlignVerticalModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Alpha -> {
            ModifierItem {
                AlphaModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.AspectRatio -> {
            ModifierItem {
                AspectRatioModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Background -> {
            ModifierItem {
                BackgroundModifierInspector(
                    project = project,
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Border -> {
            ModifierItem {
                BorderModifierInspector(
                    project = project,
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Clip -> {
            ModifierItem {
                ClipModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Height -> {
            ModifierItem {
                HeightModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Padding -> {
            ModifierItem {
                PaddingModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.FillMaxSize -> {
            ModifierItem {
                FillMaxSizeModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.FillMaxWidth -> {
            ModifierItem {
                FillMaxWidthModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.FillMaxHeight -> {
            ModifierItem {
                FillMaxHeightModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Offset -> {
            ModifierItem {
                OffsetModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Rotate -> {
            ModifierItem {
                RotateModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Scale -> {
            ModifierItem {
                ScaleModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Shadow -> {
            ModifierItem {
                ShadowModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Size -> {
            ModifierItem {
                SizeModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Weight -> {
            ModifierItem {
                WeightModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.Width -> {
            ModifierItem {
                WidthModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.WrapContentHeight -> {
            ModifierItem {
                WrapContentHeightModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.WrapContentWidth -> {
            ModifierItem {
                WrapContentWidthModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.WrapContentSize -> {
            ModifierItem {
                WrapContentSizeModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.ZIndex -> {
            ModifierItem {
                ZIndexModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.HorizontalScroll -> {
            ModifierItem {
                HorizontalScrollModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }

        is ModifierWrapper.VerticalScroll -> {
            ModifierItem {
                VerticalScrollModifierInspector(
                    node = composeNode,
                    wrapper = chain,
                    modifierIndex = i,
                    composeNodeCallbacks = composeNodeCallbacks,
                    onVisibilityToggleClicked = onVisibilityToggleClicked,
                )
            }
        }
    }
}

@Composable
fun EditModifierDialog(
    project: Project,
    composeNodeCallbacks: ComposeNodeCallbacks,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusedNodes = project.screenHolder.findFocusedNodes()
    when {
        focusedNodes.isEmpty() -> {
        }

        focusedNodes.size > 1 -> {
        }

        else -> {
            val composeNode = focusedNodes.first()
            PositionCustomizablePopup(
                onDismissRequest = onCloseDialog,
            ) {
                Surface(
                    modifier = modifier.size(width = 420.dp, height = 460.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    val lazyListState = rememberLazyListState()
                    val reorderableLazyListState =
                        rememberReorderableLazyListState(lazyListState) { from, to ->
                            if (from.index != to.index) {
                                // Use the composeNode that's already in scope
                                composeNodeCallbacks.onModifierSwapped(
                                    composeNode,
                                    from.index,
                                    to.index,
                                )
                            }
                        }
                    Column {
                        var addModifierDialogVisible by remember { mutableStateOf(false) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Modifiers",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            )

                            ComposeFlowIconButton(
                                onClick = {
                                    addModifierDialogVisible = true
                                },
                                modifier =
                                    Modifier
                                        .padding(start = 16.dp)
                                        .hoverOverlay(),
                            ) {
                                val contentDesc = stringResource(Res.string.add_new_modifier)
                                Tooltip(contentDesc) {
                                    ComposeFlowIcon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = contentDesc,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            val onAnyDialogIsShown = LocalOnAnyDialogIsShown.current
                            val onAllDialogsClosed = LocalOnAllDialogsClosed.current
                            if (addModifierDialogVisible) {
                                onAnyDialogIsShown()
                                val modifiers =
                                    ModifierWrapper
                                        .values()
                                        .filter { modifier ->
                                            composeNode.parentNode?.let {
                                                modifier.hasValidParent(it.trait.value)
                                            } ?: true
                                        }.mapIndexed { i, modifier ->
                                            i to modifier
                                        }

                                AddModifierDialog(
                                    modifiers = modifiers,
                                    onModifierSelected = {
                                        addModifierDialogVisible = false
                                        composeNodeCallbacks.onModifierAdded(
                                            composeNode,
                                            modifiers[it].second,
                                        )
                                        onAllDialogsClosed()
                                    },
                                    onCloseClick = {
                                        addModifierDialogVisible = false
                                        onAllDialogsClosed()
                                    },
                                )
                            }
                        }

                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier,
                        ) {
                            itemsIndexed(
                                composeNode.modifierList,
                                key = { index, modifier -> "${composeNode.id}_modifier_$index" },
                            ) { i, chain ->
                                ComposeFlowReorderableItem(
                                    index = i,
                                    reorderableLazyListState,
                                    key = "${composeNode.id}_modifier_$i",
                                ) {
                                    ProvideModifierReorderAllowed(reorderableCollectionItemScope = this) {
                                        val onVisibilityToggleClicked = {
                                            chain.visible.value = !chain.visible.value
                                            composeNodeCallbacks.onModifierUpdatedAt(
                                                composeNode,
                                                i,
                                                chain,
                                            )
                                        }
                                        SingleModifierInspector(
                                            project = project,
                                            composeNode = composeNode,
                                            i = i,
                                            chain = chain,
                                            composeNodeCallbacks = composeNodeCallbacks,
                                            onVisibilityToggleClicked = onVisibilityToggleClicked,
                                            reorderableLazyListState = reorderableLazyListState,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
