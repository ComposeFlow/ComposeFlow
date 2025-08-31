package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.ui.statusbar.StatusBarUiState
import org.gradle.tooling.GradleConnector
import java.io.File
import java.io.OutputStream

class GradleWrapper(
    private val projectRoot: File,
    private val buildLogger: Logger,
) {
    fun assembleDebug() =
        runTask(
            task = "assembleDebug",
            onStatusBarUiStateChanged = { _ -> },
        )

    private fun runTask(
        task: String,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        val connection =
            GradleConnector
                .newConnector()
                .forProjectDirectory(projectRoot)
                .useBuildDistribution()
                .connect()
        connection.use {
            val build =
                it.newBuild().apply {
                    fun loggerOutputStream(logger: (log: String) -> Unit) =
                        object : OutputStream() {
                            private val buffer = StringBuilder()

                            override fun write(b: Int) {
                                if (b == '\n'.code) {
                                    logger(buffer.toString())
                                    buffer.clear()
                                } else {
                                    buffer.append(b.toChar())
                                }
                            }
                        }
                    setStandardError(
                        loggerOutputStream { log ->
                            buildLogger.e(log)
                            onStatusBarUiStateChanged(StatusBarUiState.Failure(log))
                        },
                    )
                    setStandardOutput(
                        loggerOutputStream { log ->
                            buildLogger.d(log)
                            if (log.isBuildSuccessful()) {
                                onStatusBarUiStateChanged(StatusBarUiState.Success(log))
                            } else {
                                onStatusBarUiStateChanged(StatusBarUiState.Loading(log))
                            }
                        },
                    )
                }
            try {
                build
                    .forTasks(task)
                    .run()
            } catch (e: Exception) {
                buildLogger.e(e.stackTraceToString())
                throw e
            }
        }
    }
}

private fun String.isBuildSuccessful() = contains("BUILD SUCCESSFUL")
