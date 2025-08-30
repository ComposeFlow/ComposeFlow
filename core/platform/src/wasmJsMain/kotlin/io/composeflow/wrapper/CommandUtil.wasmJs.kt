package io.composeflow.wrapper

/**
 * No-op implementation of CommandUtil for WASM platform.
 * Command line operations are not supported in web environment.
 */
actual object CommandUtil {
    actual suspend fun runCommandAndWait(command: Array<String>): String = ""
}