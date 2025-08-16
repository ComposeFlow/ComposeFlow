package io.composeflow.ui.treeview.node.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface HoverableNode {
    val isHovered: Boolean
    var isHoveredState: Boolean
    var onToggleHovered: (Boolean) -> Unit

    fun setHovered(isHovered: Boolean)
}

internal class HoverableNodeHandler : HoverableNode {
    override val isHovered: Boolean
        get() = isHoveredState

    override var isHoveredState: Boolean by mutableStateOf(false)

    override var onToggleHovered: (Boolean) -> Unit by mutableStateOf({})

    override fun setHovered(isHovered: Boolean) {
        onToggleHovered(isHovered)
    }
}
