package io.composeflow.ui

import androidx.compose.ui.input.pointer.PointerIcon
import java.awt.Cursor

actual val PointerIconResizeHorizontal: PointerIcon = PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
actual val PointerIconResizeVertical: PointerIcon = PointerIcon(Cursor(Cursor.S_RESIZE_CURSOR))
