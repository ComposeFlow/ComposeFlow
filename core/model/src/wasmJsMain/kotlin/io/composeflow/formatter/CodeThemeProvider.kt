package io.composeflow.formatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

// Stub for wasmJs - no code editor theme support
class StubCodeTheme

val LocalCodeTheme = compositionLocalOf<StubCodeTheme> { error("No CodeTheme provided") }

@Composable
fun ProvideCodeTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    // No-op for wasmJs - just call content directly
    content()
}
