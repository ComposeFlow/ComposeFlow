package io.composeflow.wrapper

import io.composeflow.device.Device

expect class AdbWrapper() {
    suspend fun listDevices(): List<Device.AndroidEmulator>

    suspend fun installApk(
        deviceId: String,
        appDirPath: String,
        apkRelativePath: String = "./composeApp/build/outputs/apk/debug/composeApp-debug.apk",
    )

    suspend fun launchActivity(
        deviceId: String,
        applicationId: String,
        activityName: String,
    )
}
