package io.composeflow.wrapper

import io.composeflow.di.ServiceLocator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

actual object CommandUtil {
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.IO
        }

    fun runCommand(command: Array<String>): Process {
        val processBuilder =
            ProcessBuilder(*command).apply {
                redirectErrorStream(true)
            }
        return processBuilder.start()
    }

    actual suspend fun runCommandAndWait(command: Array<String>): String =
        withContext(ioDispatcher) {
            val processBuilder =
                ProcessBuilder(*command).apply {
                    redirectErrorStream(true)
                }
            val process = processBuilder.start()
            process.waitFor()
            process.inputStream.bufferedReader().use(BufferedReader::readText)
        }
}