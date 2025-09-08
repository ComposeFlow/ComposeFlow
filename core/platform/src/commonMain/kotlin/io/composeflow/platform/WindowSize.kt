package io.composeflow.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize

/**
 * Platform-specific window size provider
 */
@Composable
expect fun rememberWindowSize(): DpSize