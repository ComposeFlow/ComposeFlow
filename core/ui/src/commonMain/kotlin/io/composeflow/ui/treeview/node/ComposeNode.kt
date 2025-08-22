package io.composeflow.ui.treeview.node

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.composeflow.ui.treeview.tree.TreeScope

@Composable
fun <T> TreeScope.Leaf(
    content: T,
    key: Any,
    customIcon: NodeComponent<T>? = null,
    customName: NodeComponent<T>? = null,
    name: String = "",
) {
    key(key) {
        val (isSelected, setSelected) = rememberSaveable(key) { mutableStateOf(false) }
        val (isHovered, setHovered) = rememberSaveable(key) { mutableStateOf(false) }

        ComposeNode<LeafNode<T>, TreeApplier<T>>(
            factory = {
                LeafNode(
                    content = content,
                    name = name,
                    depth = depth,
                    key = key.toString(),
                    iconComponent = customIcon ?: { DefaultNodeIcon(it) },
                    nameComponent = customName ?: { DefaultNodeName(it) },
                )
            },
            update = {
                set(isSelected) { this.isSelectedState = isSelected }
                set(setSelected) { this.onToggleSelected = setSelected }
                set(isHovered) { this.isHoveredState = isHovered }
                set(setHovered) { this.onToggleHovered = setHovered }
            },
        )
    }
}

@Composable
fun <T> TreeScope.Branch(
    content: T,
    key: Any,
    customIcon: NodeComponent<T>? = null,
    customName: NodeComponent<T>? = null,
    name: String = "",
    children: @Composable TreeScope.() -> Unit = {},
) {
    key(key) {
        val (isSelected, setSelected) = rememberSaveable(key) { mutableStateOf(false) }
        val (isExpanded, setExpanded) = rememberSaveable(key) { mutableStateOf(isExpanded && depth <= expandMaxDepth) }
        val (expandMaxDepth, setExpandMaxDepth) = rememberSaveable(key) { mutableStateOf(expandMaxDepth) }
        val (isHovered, setHovered) = rememberSaveable(key) { mutableStateOf(false) }

        ComposeNode<BranchNode<T>, TreeApplier<T>>(
            factory = {
                BranchNode(
                    content = content,
                    name = name,
                    depth = depth,
                    key = key.toString(),
                    iconComponent = customIcon ?: { DefaultNodeIcon(it) },
                    nameComponent = customName ?: { DefaultNodeName(it) },
                )
            },
            update = {
                set(isSelected) { this.isSelectedState = isSelected }
                set(setSelected) { this.onToggleSelected = setSelected }
                set(isExpanded) { this.isExpandedState = isExpanded }
                set(setExpanded) {
                    this.onToggleExpanded = { isExpanded, maxDepth ->
                        setExpanded(isExpanded)
                        setExpandMaxDepth(maxDepth)
                    }
                }
                set(isHovered) { this.isHoveredState = isHovered }
                set(setHovered) { this.onToggleHovered = setHovered }
            },
        )

        if (isExpanded && depth <= expandMaxDepth) {
            TreeScope(depth.inc(), true, expandMaxDepth)
                .children()
        }
    }
}

internal class TreeApplier<T> : AbstractApplier<Node<T>?>(null) {
    val children = mutableStateListOf<Node<T>>()

    override fun insertTopDown(
        index: Int,
        instance: Node<T>?,
    ) {
        checkNotNull(instance)
        check(current == null)
        children.add(index, instance)
    }

    override fun insertBottomUp(
        index: Int,
        instance: Node<T>?,
    ) {}

    override fun remove(
        index: Int,
        count: Int,
    ) {
        check(current == null)
        children.removeRange(index, index + count)
    }

    override fun move(
        from: Int,
        to: Int,
        count: Int,
    ) {
        check(current == null)
        @Suppress("UNCHECKED_CAST")
        (children as MutableList<Node<T>?>).move(from, to, count)
    }

    override fun onClear() {
        check(current == null)
        children.clear()
    }
}
