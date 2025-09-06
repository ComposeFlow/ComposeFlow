package io.composeflow.ui.window.frame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalUiScale = staticCompositionLocalOf<Float?> { null }

/**
 * put this in every window because Window composable override [LocalDensity]
 */
@Composable
fun UiScaledContent(
    defaultDensity: Density = LocalDensity.current,
    uiScale: Float? = LocalUiScale.current,
    content: @Composable () -> Unit,
) {
    val density =
        remember(uiScale) {
            if (uiScale == null) {
                defaultDensity
            } else {
                Density(uiScale)
            }
        }
    CompositionLocalProvider(
        LocalDensity provides density,
        content,
    )
}
