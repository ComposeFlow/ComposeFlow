package io.composeflow.keyboard

import io.composeflow.platform.OperatingSystem
import io.composeflow.platform.currentOperatingSystem

actual fun getCtrlKeyStr(): String =
    when (currentOperatingSystem) {
        OperatingSystem.Windows -> "Ctrl"
        OperatingSystem.Linux -> "Ctrl"
        OperatingSystem.MacOS -> "⌘"
        OperatingSystem.Unknown -> "Ctrl"
    }

actual fun getDeleteKeyStr(): String =
    when (currentOperatingSystem) {
        OperatingSystem.Windows -> "BackSpace"
        OperatingSystem.Linux -> "BackSpace"
        OperatingSystem.MacOS -> "delete"
        OperatingSystem.Unknown -> "BackSpace"
    }
