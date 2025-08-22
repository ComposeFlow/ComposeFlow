package io.composeflow.ui.treeview.tree.extension

import io.composeflow.ui.treeview.node.BranchNode
import io.composeflow.ui.treeview.node.LeafNode
import io.composeflow.ui.treeview.node.Node

interface SelectableTree<T> {
    val selectedNodes: List<Node<T>>

    fun toggleSelection(node: Node<T>)

    fun selectNode(node: Node<T>)

    fun handleMultipleSelection(
        node: Node<T>,
        isCtrlOrMetaPressed: Boolean,
        isShiftPressed: Boolean,
    ): List<Node<T>>

    fun unselectNode(node: Node<T>)

    fun clearSelection()
}

internal class SelectableTreeHandler<T>(
    private val nodes: List<Node<T>>,
) : SelectableTree<T> {
    override val selectedNodes: List<Node<T>>
        get() = nodes.filter { it.isSelected }

    override fun toggleSelection(node: Node<T>) {
        if (node.isSelected) {
            unselectNode(node)
        } else {
            selectNode(node)
        }
    }

    override fun selectNode(node: Node<T>) {
        node.setSelected(true)
    }

    override fun unselectNode(node: Node<T>) {
        node.setSelected(false)
    }

    override fun clearSelection() {
        selectedNodes.forEach { it.setSelected(false) }
    }

    override fun handleMultipleSelection(
        node: Node<T>,
        isCtrlOrMetaPressed: Boolean,
        isShiftPressed: Boolean,
    ): List<Node<T>> {
        val currentlySelected = selectedNodes.toMutableList()

        return when {
            isCtrlOrMetaPressed && isShiftPressed -> {
                val lastSelected = currentlySelected.lastOrNull() ?: node
                val currentIndex = nodes.indexOf(node)
                val lastIndex = nodes.indexOf(lastSelected)

                val start = minOf(currentIndex, lastIndex)
                val end = maxOf(currentIndex, lastIndex)

                for (i in start..end) {
                    val n = nodes[i]
                    if (!currentlySelected.contains(n)) {
                        currentlySelected.add(n)
                    }
                }
                currentlySelected
            }

            isShiftPressed -> {
                val firstSelected = currentlySelected.firstOrNull() ?: node
                val currentIndex = nodes.indexOf(node)
                val firstIndex = nodes.indexOf(firstSelected)
                val start = minOf(currentIndex, firstIndex)
                val end = maxOf(currentIndex, firstIndex)

                nodes.subList(start, end + 1)
            }
            isCtrlOrMetaPressed -> {
                if (currentlySelected.contains(node)) {
                    currentlySelected.remove(node)
                } else {
                    currentlySelected.add(node)
                }
                currentlySelected
            }
            else -> listOf(node)
        }
    }

    private fun Node<T>.setSelected(isSelected: Boolean) {
        when (this) {
            is LeafNode -> setSelected(isSelected)
            is BranchNode -> setSelected(isSelected)
        }
    }
}
