package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.wrapper.CommandUtil.runCommand
import io.composeflow.wrapper.CommandUtil.runCommandAndWait
import io.composeflow.removeLineBreak

actual class AndroidEmulatorWrapper actual constructor() {
    private val sdkPath =
        System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME") ?: null
    private val emulatorPath =
        sdkPath?.let {
            "$it/emulator/emulator"
        }

    actual suspend fun listAvdsAndWait(): List<String> =
        emulatorPath?.let {
            val command = arrayOf(emulatorPath, "-list-avds")
            val output = runCommandAndWait(command)
            output
                .split("\n")
                .filterNot { it.startsWith("INFO") }
                .filter { it.isNotEmpty() }
                .map { it.removeLineBreak() }
        } ?: emptyList()

    actual suspend fun runAvd(
        avdName: String,
        portNumber: Int,
    ) {
        emulatorPath?.let {
            val command = arrayOf(emulatorPath, "-port", "$portNumber", "-avd", avdName)
            runCommand(command)
        } ?: Logger.w("No emulator command is found")
    }
}
