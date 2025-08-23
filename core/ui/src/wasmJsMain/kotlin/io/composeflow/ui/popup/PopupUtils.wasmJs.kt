package io.composeflow.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

@Composable
actual fun rememberDefaultPopupPositionProvider(): PopupPositionProvider =
    object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            // Center the popup on the screen for WASM
            val x = (windowSize.width - popupContentSize.width) / 2
            val y = (windowSize.height - popupContentSize.height) / 2
            return IntOffset(x, y)
        }
    }
