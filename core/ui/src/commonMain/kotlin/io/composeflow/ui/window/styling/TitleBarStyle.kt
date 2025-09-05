package io.composeflow.ui.window.styling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class TitleBarStyle(
    val colors: TitleBarColors,
    val metrics: TitleBarMetrics,
) {
    companion object
}

@Immutable
data class TitleBarColors(
    val background: Color,
    val gradientStartColor: Color,
) {
    companion object
}

@Immutable
data class TitleBarMetrics(
    val height: Dp,
    val gradientStartX: Dp,
    val gradientEndX: Dp,
) {
    companion object
}

@Composable
fun TitleBarStyle.Companion.dark(
    colors: TitleBarColors = TitleBarColors.dark(),
    metrics: TitleBarMetrics = TitleBarMetrics.default(),
): TitleBarStyle =
    TitleBarStyle(
        colors = colors,
        metrics = metrics,
    )

@Composable
fun TitleBarColors.Companion.dark(
    backgroundColor: Color = Color(0xFF2B2D30),
    gradientStartColor: Color = Color(0xFFB0C6FF),
): TitleBarColors =
    TitleBarColors(
        background = backgroundColor,
        gradientStartColor = gradientStartColor,
    )

fun TitleBarMetrics.Companion.default(
    height: Dp = 40.dp,
    gradientStartX: Dp = (-100).dp,
    gradientEndX: Dp = 400.dp,
): TitleBarMetrics =
    TitleBarMetrics(
        height = height,
        gradientStartX = gradientStartX,
        gradientEndX = gradientEndX,
    )
