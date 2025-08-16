package io.composeflow.ui.treeview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import io.composeflow.ui.treeview.node.Node
import io.composeflow.ui.treeview.tree.extension.ExpandableTree
import io.composeflow.ui.treeview.tree.extension.HoverableTree
import io.composeflow.ui.treeview.tree.extension.SelectableTree

typealias OnNodeClick<T> = ((Node<T>) -> Unit)?
typealias OnNodeHover<T> = ((Node<T>, Boolean) -> Unit)?
typealias NodeIcon<T> = @Composable (Node<T>) -> Painter?

@ConsistentCopyVisibility
@Immutable
data class TreeViewScope<T> internal constructor(
    val expandableManager: ExpandableTree<T>,
    val selectableManager: SelectableTree<T>,
    val hoverableManager: HoverableTree<T>,
    val style: TreeViewStyle<T>,
    val onClick: ((node: Node<T>, ctrl: Boolean, shift: Boolean) -> Unit)? = null,
    val onLongClick: OnNodeClick<T>,
    val onDoubleClick: OnNodeClick<T>,
    val onHover: OnNodeHover<T>,
)

fun <T> TreeViewScope<T>.getNodeBackgroundColor(node: Node<T>): Color =

    when {
        node.isHovered && node.isSelected -> style.colors.hoveredSelected
        node.isHovered -> style.colors.hovered
        node.isSelected -> style.colors.selected
        else -> style.colors.normal
    }
