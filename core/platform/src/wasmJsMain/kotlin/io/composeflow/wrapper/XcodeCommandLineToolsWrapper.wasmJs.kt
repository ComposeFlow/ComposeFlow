package io.composeflow.wrapper

import io.composeflow.device.Device

actual class XcodeCommandLineToolsWrapper {
    actual suspend fun listSimulators(): List<Device.IosSimulator> = emptyList()

    actual suspend fun isToolAvailable(): Boolean = false
}
