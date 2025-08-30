package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.device.Device
import io.composeflow.device.EmulatorStatus
import io.composeflow.removeLineBreak
import java.io.File

actual class AdbWrapper {
    private val sdkPath =
        System.getenv("ANDROID_SDK_ROOT") ?: null
    private val adbPath = sdkPath?.let { "$sdkPath/platform-tools/adb" }

    actual suspend fun listDevices(): List<Device.AndroidEmulator> =
        adbPath?.let {
            val command = arrayOf(adbPath, "devices")
            val output = CommandUtil.runCommandAndWait(command)
            output
                .split("\n")
                .filter { it.startsWith("emulator-") }
                .mapNotNull { line ->
                    val split = line.split("\\s+".toRegex())
                    val portNumber = split[0].replace("emulator-", "").toInt()
                    val deviceName = getDeviceNameFromEmulatorPort(portNumber)?.removeLineBreak()
                    val emulatorStatus = EmulatorStatus.fromString(split[1])
                    emulatorStatus.portNumber = portNumber
                    deviceName?.let {
                        Device.AndroidEmulator(
                            name = deviceName,
                            status = emulatorStatus,
                        )
                    }
                }
        } ?: emptyList()

    private suspend fun getDeviceNameFromEmulatorPort(portNumber: Int): String? {
        if (adbPath == null) {
            Logger.w("Adb path not found")
            return null
        }
        return adbPath.let { adb ->
            val command =
                arrayOf(
                    adb,
                    "-s",
                    "emulator-$portNumber",
                    "emu",
                    "avd",
                    "name",
                )
            val output = CommandUtil.runCommandAndWait(command)
            val lines = output.lines()
            lines.firstOrNull { it.isNotBlank() && it != "OK" }?.trim()
        }
    }

    actual suspend fun installApk(
        deviceId: String,
        appDirPath: String,
        apkRelativePath: String,
    ) {
        adbPath?.let {
            val appDir = File(appDirPath)
            val command =
                arrayOf(
                    it,
                    "-s",
                    deviceId,
                    "install",
                    "-r",
                    appDir.resolve(apkRelativePath).path,
                )
            CommandUtil.runCommandAndWait(command)
        } ?: Logger.w("Adb path not found")
    }

    actual suspend fun launchActivity(
        deviceId: String,
        applicationId: String,
        activityName: String,
    ) {
        adbPath?.let {
            val command =
                arrayOf(
                    it,
                    "-s",
                    deviceId,
                    "shell",
                    "am",
                    "start",
                    "-n",
                    "$applicationId/$activityName",
                    "-a",
                    "android.intent.action.MAIN",
                    "-c",
                    "android.intent.category.LAUNCHER",
                )
            CommandUtil.runCommandAndWait(command)
        } ?: Logger.w("Adb path not found")
    }
}