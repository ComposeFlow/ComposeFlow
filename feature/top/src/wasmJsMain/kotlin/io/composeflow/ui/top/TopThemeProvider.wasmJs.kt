package io.composeflow.ui.top

import androidx.compose.runtime.Composable

@Composable
actual fun ProvideTopScreenTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    // WASM doesn't need Jewel UI, just use the content directly
    content()
}
