package io.composeflow.ui.treeview.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import io.composeflow.ui.treeview.TreeViewScope
import io.composeflow.ui.treeview.node.BranchNode
import io.composeflow.ui.treeview.node.Node
import io.composeflow.ui.treeview.node.TreeApplier
import io.composeflow.ui.treeview.tree.extension.ExpandableTree
import io.composeflow.ui.treeview.tree.extension.ExpandableTreeHandler
import io.composeflow.ui.treeview.tree.extension.HoverableTree
import io.composeflow.ui.treeview.tree.extension.HoverableTreeHandler
import io.composeflow.ui.treeview.tree.extension.SelectableTree
import io.composeflow.ui.treeview.tree.extension.SelectableTreeHandler

@DslMarker
private annotation class TreeMarker

@ConsistentCopyVisibility
@Immutable
@TreeMarker
data class TreeScope internal constructor(
    val depth: Int,
    internal val isExpanded: Boolean = false,
    internal val expandMaxDepth: Int = 0,
)

@Stable
class Tree<T> internal constructor(
    val nodes: List<Node<T>>,
) : ExpandableTree<T> by ExpandableTreeHandler(nodes),
    SelectableTree<T> by SelectableTreeHandler(nodes),
    HoverableTree<T> by HoverableTreeHandler(nodes)

@Composable
fun <T> Tree(content: @Composable TreeScope.() -> Unit): Tree<T> {
    val applier = remember { TreeApplier<T>() }
    val compositionContext = rememberCompositionContext()
    val composition =
        remember(applier, compositionContext) { Composition(applier, compositionContext) }
    composition.setContent { TreeScope(depth = 0).content() }
    return remember(applier) { Tree(applier.children) }
}

fun <T> Tree<T>.handleKeyEvent(
    event: KeyEvent,
    scope: TreeViewScope<T>,
): Boolean =
    when (event.key) {
        Key.DirectionUp -> {
            selectPrevious(scope, event)
        }

        Key.DirectionDown -> {
            selectNext(scope, event)
        }

        Key.DirectionLeft -> {
            collapseSelected(scope)
        }

        Key.DirectionRight -> {
            handleRightKey(scope)
        }

        else -> false
    }

private fun <T> Tree<T>.selectPrevious(
    scope: TreeViewScope<T>,
    event: KeyEvent,
): Boolean {
    val selectedNode = selectedNodes.firstOrNull() ?: return false
    val previousNode = nodes.getOrNull(nodes.indexOf(selectedNode) - 1) ?: return false

    scope.onClick?.invoke(previousNode, false, false)

    return true
}

private fun <T> Tree<T>.selectNext(
    scope: TreeViewScope<T>,
    event: KeyEvent,
): Boolean {
    val selectedNode = selectedNodes.lastOrNull() ?: return false
    val nextNode = nodes.getOrNull(nodes.indexOf(selectedNode) + 1) ?: return false

    scope.onClick?.invoke(nextNode, false, false)

    return true
}

private fun <T> Tree<T>.collapseSelected(scope: TreeViewScope<T>): Boolean {
    val selectedNode = selectedNodes.firstOrNull() ?: return false

    if (selectedNode is BranchNode && selectedNode.isExpanded) {
        collapseNode(selectedNode)
        return true
    }

    val parent = findParent(selectedNode)
    parent?.let {
        scope.onClick?.invoke(parent, false, false)
    }

    return parent != null
}

private fun <T> Tree<T>.handleRightKey(scope: TreeViewScope<T>): Boolean {
    val selectedNode = selectedNodes.firstOrNull() ?: return false

    if (selectedNode is BranchNode && !selectedNode.isExpanded) {
        expandNode(selectedNode)
        return true
    }

    val next =
        if (selectedNode is BranchNode) {
            findFirstChild(selectedNode) ?: findNextNode(selectedNode)
        } else {
            findNextNode(selectedNode)
        }

    next?.let {
        scope.onClick?.invoke(it, false, false)
        return true
    }

    return false
}

private fun <T> Tree<T>.findParent(node: Node<T>): Node<T>? {
    val idx = nodes.indexOf(node)

    for (i in idx - 1 downTo 0) {
        if (nodes[i].depth < node.depth) return nodes[i]
    }

    return null
}

private fun <T> Tree<T>.findFirstBranch(node: Node<T>): BranchNode<T>? {
    val index = nodes.indexOf(node)

    for (i in index + 1 until nodes.size) {
        // nodes[i].depth >= node.depth &&
        if (nodes[i] is BranchNode) return nodes[i] as? BranchNode<T>
    }
    return null
}

fun <T> Tree<T>.findFirstChild(branch: BranchNode<T>): Node<T>? {
    val idx = nodes.indexOf(branch)

    for (i in idx + 1 until nodes.size) {
        if (nodes[i].depth <= branch.depth) break
        if (nodes[i].depth == branch.depth + 1) return nodes[i]
    }

    return null
}

fun <T> Tree<T>.findLastChild(node: Node<T>): Node<T>? {
    val index = nodes.indexOf(node)
    for (i in index + 1 until nodes.size) {
        if (nodes[i].depth <= node.depth) break
    }
    return node
}

fun <T> Tree<T>.findNextNode(current: Node<T>): Node<T>? = nodes.getOrNull(nodes.indexOf(current) + 1)
