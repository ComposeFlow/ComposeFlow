package io.composeflow.ui.top

import androidx.compose.runtime.Composable

/**
 * Platform-specific theme provider for the top screen.
 * JVM uses Jewel IntUiTheme, WASM uses standard Material theme.
 */
@Composable
expect fun ProvideTopScreenTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
)
