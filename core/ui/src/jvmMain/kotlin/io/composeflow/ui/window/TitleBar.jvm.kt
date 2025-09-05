package io.composeflow.ui.window

import io.composeflow.CurrentOs
import io.composeflow.currentOs
import io.composeflow.ui.window.titlebar.linux.LinuxTitleBar
import io.composeflow.ui.window.titlebar.mac.MacTitleBar
import io.composeflow.ui.window.titlebar.windows.WindowsTitleBar

actual fun TitleBar.Companion.getPlatformTitleBar(): TitleBar =
    when (currentOs) {
        CurrentOs.Windows -> WindowsTitleBar
        CurrentOs.Linux -> LinuxTitleBar
        CurrentOs.Mac -> MacTitleBar
        CurrentOs.Other -> error("Unsupported platform $currentOs")
    }
