package io.composeflow.formatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Stub implementation for WASM
actual class PlatformCodeTheme

@Composable
actual fun ProvideCodeTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    // For WASM, we provide a simple stub implementation
    CompositionLocalProvider(LocalCodeTheme provides PlatformCodeTheme()) {
        content()
    }
}
