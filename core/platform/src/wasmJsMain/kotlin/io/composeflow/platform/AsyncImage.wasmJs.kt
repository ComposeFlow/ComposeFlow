package io.composeflow.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    colorFilter: ColorFilter?,
    modifier: Modifier,
    contentScale: ContentScale,
) {
    // Simplified implementation for WASM - just render nothing
    // In a real WASM implementation, you'd want to implement proper async image loading
    // using state management and coroutines, but this is a basic no-op for compilation
}
