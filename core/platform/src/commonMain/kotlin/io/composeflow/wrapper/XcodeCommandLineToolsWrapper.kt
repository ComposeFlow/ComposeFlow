package io.composeflow.wrapper

import io.composeflow.device.Device
import io.composeflow.ui.statusbar.StatusBarUiState

expect class XcodeCommandLineToolsWrapper {
    suspend fun listSimulators(): List<Device.IosSimulator>
    suspend fun isToolAvailable(): Boolean
}