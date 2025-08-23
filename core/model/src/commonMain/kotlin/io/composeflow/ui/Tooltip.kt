package io.composeflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
// TODO: Replace this with implementation available in WASM. At the moment, WASM actual implementation
// is just showing the content as-is.
expect fun Tooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
