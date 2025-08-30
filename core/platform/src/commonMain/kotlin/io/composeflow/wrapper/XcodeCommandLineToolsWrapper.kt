package io.composeflow.wrapper

import io.composeflow.device.Device

expect class XcodeCommandLineToolsWrapper {
    suspend fun listSimulators(): List<Device.IosSimulator>

    suspend fun isToolAvailable(): Boolean
}
