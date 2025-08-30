package io.composeflow.wrapper

expect object CommandUtil {
    suspend fun runCommandAndWait(command: Array<String>): String
}