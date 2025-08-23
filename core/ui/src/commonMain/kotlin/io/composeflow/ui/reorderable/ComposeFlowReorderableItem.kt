package io.composeflow.ui.reorderable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LazyItemScope.ComposeFlowReorderableItem(
    index: Int,
    reorderableLazyListState: ReorderableLazyListState =
        rememberReorderableLazyListState(rememberLazyListState()) { _, _ -> },
    key: Any? = null,
    content: @Composable ReorderableCollectionItemScope.() -> Unit,
) {
    ReorderableItem(
        reorderableLazyListState,
        key = key ?: "item $index",
    ) { isDragging ->
        val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
        val backgroundModifier =
            if (isDragging) {
                Modifier.background(
                    color =
                        MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = 0.8f,
                        ),
                )
            } else {
                Modifier
            }
        val clipShape =
            if (isDragging) RoundedCornerShape(8.dp) else RectangleShape
        Column(
            modifier =
                Modifier
                    .clip(clipShape)
                    .shadow(elevation.value)
                    .then(backgroundModifier),
        ) {
            content()
        }
    }
}
