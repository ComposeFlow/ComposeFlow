package io.composeflow.model.palette

import androidx.compose.ui.geometry.Offset
import io.composeflow.model.project.appscreen.screen.composenode.ComposeNode

data class PaletteNodeCallbacks(
    val onComposableDroppedToTarget: suspend (
        dropPosition: Offset,
        composeNode: ComposeNode,
    ) -> Unit,
    val onDraggedNodeUpdated: (ComposeNode?) -> Unit,
    val onDraggedPositionUpdated: (draggedPosition: Offset, paletteDraggable: PaletteDraggable) -> Unit,
    val onDragEnd: () -> Unit,
)

val emptyPaletteNodeCallbacks =
    PaletteNodeCallbacks(
        onComposableDroppedToTarget = { _, _ -> },
        onDraggedNodeUpdated = {},
        onDraggedPositionUpdated = { _, _ -> },
        onDragEnd = {},
    )
