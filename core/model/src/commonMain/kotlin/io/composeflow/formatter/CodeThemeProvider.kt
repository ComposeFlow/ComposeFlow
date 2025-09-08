package io.composeflow.formatter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Common interface for code themes across platforms.
 * JVM implementation will wrap the actual CodeTheme,
 * while WASM will provide a stub implementation.
 */
expect class PlatformCodeTheme

val LocalCodeTheme = compositionLocalOf<PlatformCodeTheme> { error("No CodeTheme provided") }

@Composable
expect fun ProvideCodeTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
)
