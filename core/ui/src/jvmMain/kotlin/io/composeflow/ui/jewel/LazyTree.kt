package io.composeflow.ui.jewel

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.lazy.SelectableLazyItemScope
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.tree.BasicLazyTree
import org.jetbrains.jewel.foundation.lazy.tree.DefaultTreeViewKeyActions
import org.jetbrains.jewel.foundation.lazy.tree.KeyActions
import org.jetbrains.jewel.foundation.lazy.tree.Tree
import org.jetbrains.jewel.foundation.lazy.tree.TreeElementState
import org.jetbrains.jewel.foundation.lazy.tree.TreeState
import org.jetbrains.jewel.foundation.lazy.tree.rememberTreeState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.foundation.theme.LocalContentColor
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.styling.LazyTreeColors
import org.jetbrains.jewel.ui.component.styling.LazyTreeMetrics
import org.jetbrains.jewel.ui.component.styling.LazyTreeStyle
import org.jetbrains.jewel.ui.component.styling.LocalLazyTreeStyle
import org.jetbrains.jewel.ui.theme.treeStyle

/**
 * Wrapper of LazyTree, whose selectionMode is set as [SelectionMode.Single]
 */
@Composable
fun <T> SingleSelectionLazyTree(
    tree: Tree<T>,
    modifier: Modifier = Modifier,
    onElementClick: (Tree.Element<T>) -> Unit = {},
    treeState: TreeState = rememberTreeState(),
    onElementDoubleClick: (Tree.Element<T>) -> Unit = {},
    onSelectionChange: (List<Tree.Element<T>>) -> Unit = {},
    keyActions: KeyActions = DefaultTreeViewKeyActions(treeState),
    style: LazyTreeStyle = JewelTheme.treeStyle,
    nodeContent: @Composable (SelectableLazyItemScope.(Tree.Element<T>) -> Unit),
) {
    LazyTreeWithSelectionMode(
        tree = tree,
        selectionMode = SelectionMode.Single,
        modifier = modifier,
        onElementClick = onElementClick,
        treeState = treeState,
        onElementDoubleClick = onElementDoubleClick,
        onSelectionChange = onSelectionChange,
        keyActions = keyActions,
        style = style,
        nodeContent = nodeContent,
    )
}

/**
 * Wrapper of LazyTree, whose selectionMode is set as [SelectionMode.Multiple]
 */
@Composable
fun <T> MultipleSelectionLazyTree(
    tree: Tree<T>,
    modifier: Modifier = Modifier,
    onElementClick: (Tree.Element<T>) -> Unit = {},
    treeState: TreeState = rememberTreeState(),
    onElementDoubleClick: (Tree.Element<T>) -> Unit = {},
    onSelectionChange: (List<Tree.Element<T>>) -> Unit = {},
    keyActions: KeyActions = DefaultTreeViewKeyActions(treeState),
    style: LazyTreeStyle = JewelTheme.treeStyle,
    nodeContent: @Composable (SelectableLazyItemScope.(Tree.Element<T>) -> Unit),
) {
    LazyTreeWithSelectionMode(
        tree = tree,
        selectionMode = SelectionMode.Multiple,
        modifier = modifier,
        onElementClick = onElementClick,
        treeState = treeState,
        onElementDoubleClick = onElementDoubleClick,
        onSelectionChange = onSelectionChange,
        keyActions = keyActions,
        style = style,
        nodeContent = nodeContent,
    )
}

/**
 * Wrapper of LazyTree, whose selectionMode is set as [SelectionMode.None]
 */
@Composable
fun <T> NoneSelectionLazyTree(
    tree: Tree<T>,
    modifier: Modifier = Modifier,
    onElementClick: (Tree.Element<T>) -> Unit = {},
    treeState: TreeState = rememberTreeState(),
    onElementDoubleClick: (Tree.Element<T>) -> Unit = {},
    onSelectionChange: (List<Tree.Element<T>>) -> Unit = {},
    keyActions: KeyActions = DefaultTreeViewKeyActions(treeState),
    style: LazyTreeStyle = JewelTheme.treeStyle,
    nodeContent: @Composable (SelectableLazyItemScope.(Tree.Element<T>) -> Unit),
) {
    val colors =
        LazyTreeColors(
            elementBackgroundFocused = style.colors.elementBackgroundFocused,
            elementBackgroundSelected = Color.Transparent,
            elementBackgroundSelectedFocused = Color.Transparent,
            content = style.colors.content,
            contentFocused = style.colors.contentFocused,
            contentSelected = style.colors.contentSelected,
            contentSelectedFocused = style.colors.contentSelectedFocused,
        )
    LazyTreeWithSelectionMode(
        tree = tree,
        selectionMode = SelectionMode.None,
        modifier = modifier,
        onElementClick = onElementClick,
        treeState = treeState,
        onElementDoubleClick = onElementDoubleClick,
        onSelectionChange = onSelectionChange,
        keyActions = keyActions,
        style =
            LazyTreeStyle(
                colors = colors,
                metrics = style.metrics,
                icons = style.icons,
            ),
        nodeContent = nodeContent,
    )
}

/**
 * Wrapper of LazyTree, whose selectionMode can be configurable through the [selectionMode]
 * argument.
 */
@Composable
private fun <T> LazyTreeWithSelectionMode(
    tree: Tree<T>,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    modifier: Modifier = Modifier,
    onElementClick: (Tree.Element<T>) -> Unit = {},
    treeState: TreeState = rememberTreeState(),
    onElementDoubleClick: (Tree.Element<T>) -> Unit = {},
    onSelectionChange: (List<Tree.Element<T>>) -> Unit = {},
    keyActions: KeyActions = DefaultTreeViewKeyActions(treeState),
    style: LazyTreeStyle = JewelTheme.treeStyle,
    nodeContent: @Composable (SelectableLazyItemScope.(Tree.Element<T>) -> Unit),
) {
    val colors = style.colors
    val metrics = style.metrics

    BasicLazyTree(
        tree = tree,
        selectionMode = selectionMode,
        onElementClick = onElementClick,
        elementBackgroundFocused = colors.elementBackgroundFocused,
        elementBackgroundSelectedFocused = colors.elementBackgroundSelectedFocused,
        elementBackgroundSelected = colors.elementBackgroundSelected,
        indentSize = metrics.indentSize,
        elementBackgroundCornerSize = metrics.elementBackgroundCornerSize,
        elementPadding = metrics.elementPadding,
        elementContentPadding = metrics.elementContentPadding,
        elementMinHeight = metrics.elementMinHeight,
        chevronContentGap = metrics.chevronContentGap,
        treeState = treeState,
        modifier = modifier,
        onElementDoubleClick = onElementDoubleClick,
        onSelectionChange = onSelectionChange,
        keyActions = keyActions,
        chevronContent = { elementState ->
            val painterProvider =
                style.icons.chevron(elementState.isExpanded, elementState.isSelected)
            val painter by painterProvider.getPainter()
            Icon(painter = painter, contentDescription = null)
        },
    ) {
        val resolvedContentColor =
            style.colors
                .contentFor(
                    TreeElementState.of(
                        focused = isActive,
                        selected = isSelected,
                        expanded = false,
                    ),
                ).value
                .takeOrElse { LocalContentColor.current }

        CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
            nodeContent(it)
        }
    }
}

@Composable
fun ProvideLazyTreeStyle(content: @Composable () -> Unit) {
    val localTreeStyle = LocalLazyTreeStyle.current
    val lazyTreeColors =
        LazyTreeColors(
            elementBackgroundFocused = MaterialTheme.colorScheme.surface,
            elementBackgroundSelected = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
            elementBackgroundSelectedFocused =
                MaterialTheme.colorScheme.tertiaryContainer.copy(
                    alpha = 0.8f,
                ),
            content = MaterialTheme.colorScheme.onSurface,
            contentFocused = localTreeStyle.colors.contentFocused,
            contentSelected = localTreeStyle.colors.contentSelected,
            contentSelectedFocused = localTreeStyle.colors.contentSelectedFocused,
        )
    val treeStyle =
        LazyTreeStyle(
            colors = lazyTreeColors,
            metrics =
                LazyTreeMetrics(
                    indentSize = localTreeStyle.metrics.indentSize,
                    elementBackgroundCornerSize = CornerSize(8.dp),
                    elementPadding = localTreeStyle.metrics.elementPadding,
                    elementContentPadding = localTreeStyle.metrics.elementContentPadding,
                    elementMinHeight = localTreeStyle.metrics.elementMinHeight,
                    chevronContentGap = localTreeStyle.metrics.chevronContentGap,
                ),
            icons = localTreeStyle.icons,
        )
    CompositionLocalProvider(LocalLazyTreeStyle provides treeStyle) {
        content()
    }
}
