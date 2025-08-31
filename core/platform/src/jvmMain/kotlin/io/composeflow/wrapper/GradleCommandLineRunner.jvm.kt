package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.CurrentOs
import io.composeflow.currentOs
import io.composeflow.logger.logger
import io.composeflow.ui.statusbar.StatusBarUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * Class to run gradle wrapper by running command line
 */
actual class GradleCommandLineRunner(
    private val projectRoot: File,
    private val buildLogger: Logger,
    private val localJavaHomePath: String,
) {
    private var readStdOutJob: Job? = null
    private var readStdErrJob: Job? = null

    suspend fun installDebug(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit) =
        runTask(
            task = "installDebug",
            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
        )

    actual suspend fun assembleDebug(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit) =
        runTask(
            task = "assembleDebug",
            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
        )

    actual suspend fun jsBrowserDevelopmentRun(onStatusBarUiStateChanged: (StatusBarUiState) -> Unit) =
        runTask(
            task = "jsBrowserDevelopmentRun",
            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
        )

    private suspend fun runTask(
        task: String,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        // Running the gradle wrapper from the command line instead of relying on
        // gradle-tooling-api because running the gradle wrapper from the release distributable
        // from Mac can't be reliably done because javaHome set when the app distributable is
        // launched (usually /Applications/ComposeFlow.app/Contents/runtime/Contents/Home/) and
        // the user's JAVA_HOME difference makes running the gradle wrapper impossible even if
        // the Java runtime is included inside the app package.

        withContext(Dispatchers.IO) {
            projectRoot.resolve("gradlew").setExecutable(true)
            val commands =
                when (currentOs) {
                    CurrentOs.Windows -> {
                        arrayOf("cmd.exe", "/c", "gradlew.bat", task)
                    }

                    else -> {
                        arrayOf("./gradlew", task)
                    }
                }
            val processBuilder =
                ProcessBuilder(*commands).apply {
                    directory(projectRoot)
                }

            if (localJavaHomePath.isNotEmpty()) {
                val environment = processBuilder.environment()
                environment["JAVA_HOME"] = localJavaHomePath
                logger.debug("JAVA_HOME is set for gradle wrapper : {}", localJavaHomePath)
            }

            Logger.d("Executing command: ${processBuilder.command()}")
            logger.debug("Executing command: {}", processBuilder.command())
            val process = processBuilder.start()

            try {
                readStdOutJob?.cancel()
                readStdOutJob =
                    launch {
                        process.inputStream.bufferedReader().use { reader ->
                            reader.forEachLine { line ->
                                buildLogger.d(line)
                                logger.debug(line)
                                if (line.isBuildSuccessful()) {
                                    onStatusBarUiStateChanged(StatusBarUiState.Success(line))
                                } else if (line.isJsBrowserRunSuccessful()) {
                                    val message = "Running at: ${
                                        line.replace(
                                            "<i> [webpack-dev-server] Loopback: ",
                                            "",
                                        )
                                    }"
                                    onStatusBarUiStateChanged(
                                        StatusBarUiState.JsBrowserRunSuccess(
                                            message,
                                        ),
                                    )
                                } else {
                                    onStatusBarUiStateChanged(StatusBarUiState.Loading(line))
                                }
                            }
                        }
                    }

                readStdErrJob?.cancel()
                readStdErrJob =
                    launch {
                        process.errorStream.bufferedReader().use { reader ->
                            reader.forEachLine { line ->
                                if (line.isEmpty()) {
                                    return@forEachLine
                                }
                                if (line.hasWebPackMessage() ||
                                    line.contains(IGNORE_WARNING) ||
                                    line.startsWith(
                                        "warning",
                                    )
                                ) {
                                    // For some reason, the line from webpack for the jsBrowserRun task is
                                    // emitted to the standard error.
                                    if (line.isJsBrowserRunSuccessful()) {
                                        val message = "Running at: ${
                                            line.replace(
                                                "<i> [webpack-dev-server] Loopback: ",
                                                "",
                                            )
                                        }"
                                        onStatusBarUiStateChanged(
                                            StatusBarUiState.JsBrowserRunSuccess(
                                                message,
                                            ),
                                        )
                                    } else {
                                        buildLogger.i(line)
                                        onStatusBarUiStateChanged(StatusBarUiState.Loading(line))
                                    }
                                } else {
                                    if (line.isBuildFailure()) {
                                        onStatusBarUiStateChanged(StatusBarUiState.Failure(line))
                                    }
                                    buildLogger.e(line)
                                    logger.error(line)
                                }
                            }
                        }
                    }

                // Create a suspension point so that this task is also canceled when
                // canceling the parent coroutine
                while (isActive) {
                    if (!process.isAlive) break
                    delay(1000)
                }

                readStdErrJob?.cancelAndJoin()
                readStdOutJob?.cancelAndJoin()
            } catch (e: CancellationException) {
                logger.info("Task $task was canceled")
                process.destroy()
            } finally {
                logger.info("Finally block in runTask. Cleaning up: $task")
                process.destroyForcibly()
            }
        }
    }
}

private fun String.isBuildSuccessful() = contains("BUILD SUCCESSFUL")

private fun String.isBuildFailure() = contains("error", ignoreCase = true) || contains("build failed", ignoreCase = true)

private fun String.isJsBrowserRunSuccessful() = hasWebPackMessage() && contains("Loopback: ")

private fun String.hasWebPackMessage() = contains("[webpack-dev-server]") || contains("[webpack-dev-middleware]")

private const val IGNORE_WARNING = "warning Ignored scripts due to flag."
