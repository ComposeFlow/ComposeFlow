package io.composeflow.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberWindowSize(): DpSize {
    val windowInfo = LocalWindowInfo.current
    return remember(windowInfo) {
        DpSize(
            windowInfo.containerSize.width.dp,
            windowInfo.containerSize.height.dp,
        )
    }
}
