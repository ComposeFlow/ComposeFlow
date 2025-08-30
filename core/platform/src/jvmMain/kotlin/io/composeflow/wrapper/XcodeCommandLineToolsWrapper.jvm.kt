package io.composeflow.wrapper

import co.touchlab.kermit.Logger
import io.composeflow.wrapper.CommandUtil.runCommandAndWait
import io.composeflow.logger.logger
import io.composeflow.device.Device
import io.composeflow.device.SimulatorStatus
import io.composeflow.ui.statusbar.StatusBarUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import java.io.File

private const val XCODEBUILD = "xcodebuild"
private const val XCRUN = "xcrun"

actual class XcodeCommandLineToolsWrapper(
    private val buildLogger: Logger,
) {
    private var readStdOutJob: Job? = null
    private var readStdErrJob: Job? = null

    actual suspend fun isToolAvailable(): Boolean {
        return try {
            val xcodebuildOutput = runCommandAndWait(arrayOf(XCODEBUILD, "-version"))
            if (!xcodebuildOutput.startsWith("Xcode")) return false
            val xcrunOutput = runCommandAndWait(arrayOf(XCRUN, "-version"))
            val result = xcrunOutput.startsWith("xcrun")
            result
        } catch (e: Exception) {
            false
        }
    }

    private val xcrunResultRegex = """^(.+?) \(([^()]+)\) \(([^()]+)\) *$""".toRegex()

    actual suspend fun listSimulators(): List<Device.IosSimulator> {
        val command = arrayOf(XCRUN, "simctl", "list", "devices", "available")
        val output = runCommandAndWait(command)
        return output
            .split("\n")
            .map { it.trimStart() }
            .filter { it.startsWith("iPhone") }
            .mapNotNull { line ->
                val matchResult = xcrunResultRegex.find(line)
                if (matchResult != null) {
                    val (name, deviceId, status) = matchResult.destructured
                    Device.IosSimulator(
                        name = name,
                        deviceId = deviceId,
                        status = SimulatorStatus.fromString(status),
                    )
                } else {
                    null
                }
            }
    }

    suspend fun buildAndLaunchSimulator(
        appDir: File,
        simulator: Device.IosSimulator,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
    ) {
        launchSimulator(
            appDir = appDir,
            simulator = simulator,
            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
        )
        resolvePackageDependencies(
            appDir = appDir,
            onStatusBarUiStateChanged = onStatusBarUiStateChanged,
        )
        val buildResult =
            buildApp(
                appDir = appDir,
                simulator = simulator,
                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
            )
        when (buildResult) {
            is BuildResult.Success -> {
                onStatusBarUiStateChanged(StatusBarUiState.Loading("Installing the app"))
                installApp(
                    appDir = appDir,
                    simulator = simulator,
                    artifactPath = buildResult.artifactPath,
                )
                onStatusBarUiStateChanged(StatusBarUiState.Loading("Launching the app"))
                launchApp(
                    appDir = appDir,
                    simulator = simulator,
                    packageName = packageName,
                    projectName = projectName,
                )
                onStatusBarUiStateChanged(StatusBarUiState.Success("BUILD SUCCESS"))
            }

            else -> {
                onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to build the app"))
            }
        }
    }

    private suspend fun installApp(
        appDir: File,
        simulator: Device.IosSimulator,
        artifactPath: String,
    ) {
        withTimeoutOrNull(90_000L) {
            // timeout after 90 seconds
            withContext(Dispatchers.IO) {
                val command =
                    arrayOf(
                        XCRUN,
                        "simctl",
                        "install",
                        simulator.deviceId,
                        artifactPath,
                    )
                val processBuilder =
                    ProcessBuilder(*command).apply {
                        directory(appDir.resolve("iosApp"))
                    }
                Logger.d("Installing the app: ${processBuilder.command()}")
                logger.debug("Installing the app: {}", processBuilder.command())

                val process = processBuilder.start()

                process.waitFor()
            }
        }
    }

    private suspend fun launchApp(
        appDir: File,
        simulator: Device.IosSimulator,
        packageName: String,
        projectName: String,
    ) {
        withContext(Dispatchers.IO) {
            val command =
                arrayOf(
                    XCRUN,
                    "simctl",
                    "launch",
                    simulator.deviceId,
                    "$packageName.$projectName",
                )
            val processBuilder =
                ProcessBuilder(*command).apply {
                    directory(appDir.resolve("iosApp"))
                }
            Logger.d("Launching the app: ${processBuilder.command()}")
            logger.debug("Launching the app: {}", processBuilder.command())

            processBuilder.start()
        }
    }

    private suspend fun resolvePackageDependencies(
        appDir: File,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        try {
            withContext(Dispatchers.IO) {
                val command =
                    arrayOf(
                        XCODEBUILD,
                        "-resolvePackageDependencies",
                    )
                val processBuilder =
                    ProcessBuilder(*command).apply {
                        directory(appDir.resolve("iosApp"))
                    }
                Logger.d("Executing command: ${processBuilder.command()}")

                val process = processBuilder.start()

                readStdOutJob?.cancel()
                readStdOutJob =
                    launch {
                        process.inputStream.bufferedReader().use { reader ->
                            reader.forEachLine { line ->
                                onStatusBarUiStateChanged(StatusBarUiState.Loading(line))
                                buildLogger.d(line)
                                logger.debug(line)
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
                                buildLogger.e(line)
                                logger.error(line)
                            }
                        }
                    }
                process.waitFor()
            }
        } catch (e: Exception) {
            onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to resolve package dependencies"))
            BuildResult.Failure
        }
    }

    private suspend fun buildApp(
        appDir: File,
        simulator: Device.IosSimulator,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ): BuildResult? {
        return try {
            var appPath: String? = null
            withTimeoutOrNull(90_000L) {
                // timeout after 90 seconds
                withContext(Dispatchers.IO) {
                    appDir.resolve("gradlew").setExecutable(true)
                    val command =
                        arrayOf(
                            XCODEBUILD,
                            "-scheme",
                            "iosApp",
                            "-sdk",
                            "iphonesimulator",
                            "-destination",
                            "id=${simulator.deviceId}",
                            "clean",
                            "build",
                        )
                    val processBuilder =
                        ProcessBuilder(*command).apply {
                            directory(appDir.resolve("iosApp"))
                        }
                    Logger.d("Executing command: ${processBuilder.command()}")
                    logger.debug("Executing command: {}", processBuilder.command())

                    val process = processBuilder.start()

                    readStdOutJob?.cancel()
                    readStdOutJob =
                        launch {
                            process.inputStream.bufferedReader().use { reader ->
                                reader.forEachLine { line ->
                                    buildLogger.d(line)
                                    logger.debug(line)

                                    if (line.contains("/usr/bin/touch")) {
                                        val split = line.split("\\s+".toRegex())
                                        appPath = split[3]
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
                                    buildLogger.e(line)
                                    logger.error(line)
                                }
                            }
                        }
                    process.waitFor()
                    appPath?.let {
                        BuildResult.Success(it)
                    } ?: BuildResult.Failure
                }
            }
        } catch (e: Exception) {
            onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to build the app"))
            BuildResult.Failure
        }
    }

    private suspend fun launchSimulator(
        appDir: File,
        simulator: Device.IosSimulator,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
    ) {
        if (simulator.status == SimulatorStatus.Booted) return
        try {
            var simulators: List<Device.IosSimulator>?
            withTimeoutOrNull(60_000L) {
                // timeout after 90 seconds
                withContext(Dispatchers.IO) {
                    val command =
                        arrayOf(
                            XCRUN,
                            "simctl",
                            "boot",
                            simulator.deviceId,
                        )
                    val processBuilder =
                        ProcessBuilder(*command).apply {
                            directory(appDir.resolve("iosApp"))
                        }
                    processBuilder.start()
                    while (isActive) { // continue looping as long as the coroutine is active
                        simulators = listSimulators()
                        if (simulators?.isNotEmpty() == true &&
                            simulators!!.firstOrNull { it.deviceId == simulator.deviceId }?.status == SimulatorStatus.Booted
                        ) {
                            onStatusBarUiStateChanged(StatusBarUiState.Loading("Device ${simulator.name} is online"))
                            break // break the loop if the result is not empty
                        }
                        yield()
                        onStatusBarUiStateChanged(StatusBarUiState.Loading("Waiting for ${simulator.name} to boot"))
                        delay(1000L) // delay for a certain time (e.g., 1 second) before retrying
                    }
                }
            }
        } catch (e: Exception) {
            onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to launch the Simulator"))
        }
    }
}

sealed interface BuildResult {
    data class Success(
        val artifactPath: String,
    ) : BuildResult

    data object Failure : BuildResult
}
