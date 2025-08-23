package io.composeflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Tooltip(
    text: String,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    // In WASM, tooltips are not yet supported by Compose
    // Simply render the content without the tooltip functionality
    content()
}
