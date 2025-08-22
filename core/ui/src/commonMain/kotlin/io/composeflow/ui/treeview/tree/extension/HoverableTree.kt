package io.composeflow.ui.treeview.tree.extension

import io.composeflow.ui.treeview.node.Node
import io.composeflow.ui.treeview.node.extension.HoverableNode

interface HoverableTree<T> {
    fun setHovered(
        node: Node<T>,
        isHovered: Boolean,
    )
}

internal class HoverableTreeHandler<T>(
    private val nodes: List<Node<T>>,
) : HoverableTree<T> {
    override fun setHovered(
        node: Node<T>,
        isHovered: Boolean,
    ) {
        (node as? HoverableNode)?.setHovered(isHovered)
    }
}
