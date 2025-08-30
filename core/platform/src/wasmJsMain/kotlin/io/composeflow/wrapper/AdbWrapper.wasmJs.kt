package io.composeflow.wrapper

import io.composeflow.device.Device

/**
 * No-op implementation of AdbWrapper for WASM platform.
 * ADB operations are not supported in web environment.
 */
actual class AdbWrapper {
    actual suspend fun listDevices(): List<Device.AndroidEmulator> = emptyList()
    
    actual suspend fun installApk(
        deviceId: String,
        appDirPath: String,
        apkRelativePath: String
    ) {
        // No-op: ADB operations not supported on WASM
    }
    
    actual suspend fun launchActivity(
        deviceId: String,
        applicationId: String,
        activityName: String
    ) {
        // No-op: ADB operations not supported on WASM
    }
}