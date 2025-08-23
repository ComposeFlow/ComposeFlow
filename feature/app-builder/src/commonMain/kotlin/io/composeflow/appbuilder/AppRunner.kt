package io.composeflow.appbuilder

import co.touchlab.kermit.Logger
import co.touchlab.kermit.SystemWriter
import co.touchlab.kermit.loggerConfigInit
import io.composeflow.appbuilder.wrapper.AdbWrapper
import io.composeflow.appbuilder.wrapper.AndroidEmulatorWrapper
import io.composeflow.appbuilder.wrapper.GradleCommandLineRunner
import io.composeflow.appbuilder.wrapper.GradleWrapper
import io.composeflow.appbuilder.wrapper.XcodeCommandLineToolsWrapper
import io.composeflow.auth.google.extractClientIdFromGoogleServicesJson
import io.composeflow.di.ServiceLocator
import io.composeflow.formatter.FormatterWrapper
import io.composeflow.json.jsonSerializer
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.model.device.Device
import io.composeflow.model.device.EmulatorStatus
import io.composeflow.model.device.SimulatorStatus
import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.settings.PathSetting
import io.composeflow.ui.statusbar.StatusBarUiState
import io.composeflow.wrapper.unzip
import io.composeflow.wrapper.zipDirectory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.util.concurrent.CancellationException
import kotlin.io.path.createTempDirectory

object AppRunner {
    // TODO: Replace this with DI
    var buildLogger = Logger(loggerConfigInit(SystemWriter()))
    private val androidEmulatorWrapper = AndroidEmulatorWrapper()
    private val adbWrapper = AdbWrapper()
    private val xcodeToolsWrapper = XcodeCommandLineToolsWrapper(buildLogger)
    private val ioDispatcher: CoroutineDispatcher =
        ServiceLocator.getOrPutWithKey(ServiceLocator.KEY_IO_DISPATCHER) {
            Dispatchers.IO
        }

    suspend fun runAndroidApp(
        device: Device.AndroidEmulator,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        portNumber: Int,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String> = emptyMap(),
        copyLocalFileInstructions: Map<String, String> = emptyMap(),
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
        localJavaHomePath: PathSetting,
    ) {
        buildLogger.i("runAndroidApp. $device, portNumber: $portNumber")
        withContext(ioDispatcher) {
            val appDir =
                prepareAppDir(
                    packageName = packageName,
                    projectName = projectName,
                    copyInstructions = copyInstructions,
                    copyLocalFileInstructions = copyLocalFileInstructions,
                    writeFileInstructions = writeFileInstructions,
                    firebaseAppInfo = firebaseAppInfo,
                )
            // For isolating problem when build error happens, not formatting the code
            // when building the app
            addComposeBuilderImplementation(appDir, fileSpecs, formatCode = false)
            val buildingMessage = "Building the app at directory: $appDir"
            buildLogger.i(buildingMessage)
            onStatusBarUiStateChanged(StatusBarUiState.Loading(buildingMessage))

            runAppOnAndroidDevice(
                appDir = appDir,
                device = device,
                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                portNumber = portNumber,
                packageName = packageName,
                projectName = projectName,
                localJavaHomePath = localJavaHomePath,
            )
        }
    }

    suspend fun runIosApp(
        device: Device.IosSimulator,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String> = emptyMap(),
        copyLocalFileInstructions: Map<String, String> = emptyMap(),
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        buildLogger.i("runIosApp. $device")
        withContext(ioDispatcher) {
            val appDir =
                prepareAppDir(
                    packageName = packageName,
                    projectName = projectName,
                    copyInstructions = copyInstructions,
                    copyLocalFileInstructions = copyLocalFileInstructions,
                    writeFileInstructions = writeFileInstructions,
                    firebaseAppInfo = firebaseAppInfo,
                )

            // For isolating problem when build error happens, not formatting the code
            // when building the app
            addComposeBuilderImplementation(appDir, fileSpecs, formatCode = false)
            val buildingMessage = "Building the app at directory: $appDir"
            buildLogger.i(buildingMessage)
            onStatusBarUiStateChanged(StatusBarUiState.Loading(buildingMessage))

            xcodeToolsWrapper.buildAndLaunchSimulator(
                appDir = appDir,
                simulator = device,
                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                packageName = packageName,
                projectName = projectName,
            )
        }
    }

    suspend fun runJsApp(
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String> = emptyMap(),
        copyLocalFileInstructions: Map<String, String> = emptyMap(),
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
        localJavaHomePath: PathSetting,
    ) {
        withContext(ioDispatcher) {
            val appDir =
                prepareAppDir(
                    packageName = packageName,
                    projectName = projectName,
                    copyInstructions = copyInstructions,
                    copyLocalFileInstructions = copyLocalFileInstructions,
                    writeFileInstructions = writeFileInstructions,
                    firebaseAppInfo = firebaseAppInfo,
                )

            // For isolating problem when build error happens, not formatting the code
            // when building the app
            addComposeBuilderImplementation(appDir, fileSpecs, formatCode = false)
            val buildingMessage = "Building the app at directory: $appDir"
            buildLogger.i(buildingMessage)
            onStatusBarUiStateChanged(StatusBarUiState.Loading(buildingMessage))

            runJsAppOnBrowser(
                appDir = appDir,
                onStatusBarUiStateChanged = onStatusBarUiStateChanged,
                localJavaHomePath = localJavaHomePath,
            )
        }
    }

    fun buildApp(
        fileSpecs: List<FileSpecWithDirectory>,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        val appDir =
            prepareAppDir(
                packageName = packageName,
                projectName = projectName,
                copyInstructions = copyInstructions,
                copyLocalFileInstructions = copyLocalFileInstructions,
                writeFileInstructions = writeFileInstructions,
                firebaseAppInfo = firebaseAppInfo,
            )

        addComposeBuilderImplementation(appDir, fileSpecs)

        val gradleWrapper =
            GradleWrapper(
                projectRoot = appDir,
                buildLogger = buildLogger,
            )
        buildLogger.i("Building the app at directory: $appDir")
        gradleWrapper.assembleDebug()
    }

    suspend fun downloadCode(
        project: Project,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String> = emptyMap(),
        copyLocalFileInstructions: Map<String, String> = emptyMap(),
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        withContext(ioDispatcher) {
            val appDir =
                prepareAppDir(
                    packageName = packageName,
                    projectName = projectName,
                    copyInstructions = copyInstructions,
                    copyLocalFileInstructions = copyLocalFileInstructions,
                    writeFileInstructions = writeFileInstructions,
                    firebaseAppInfo = firebaseAppInfo,
                )

            addComposeBuilderImplementation(appDir, fileSpecs)
            val downloadDir = prepareDownloadDir()

            onStatusBarUiStateChanged(StatusBarUiState.Loading("Zipping the contents"))

            fun generateZipFile(
                projectName: String,
                downloadDirectory: File,
            ): File {
                val file = downloadDirectory.resolve("$projectName.zip")
                if (!file.exists()) {
                    return file
                }
                var id = 1
                while (true) {
                    val name = downloadDirectory.resolve("${projectName}_${id++}.zip")
                    if (!name.exists()) {
                        return name
                    }
                }
            }

            val zipFile = generateZipFile(project.name, downloadDir)
            zipDirectory(appDir, zipFile)
            onStatusBarUiStateChanged(StatusBarUiState.Success("Download completed at: ${zipFile.absolutePath}"))
        }
    }

    suspend fun getAvailableDevices(): List<Device> {
        val toolsResult = xcodeToolsWrapper.isToolAvailable()
        val iosSimulators =
            if (toolsResult) {
                xcodeToolsWrapper.listSimulators()
            } else {
                emptyList()
            }
        val onlineSimulators = iosSimulators.filter { it.status == SimulatorStatus.Booted }
        val offlineSimulators = iosSimulators.filter { it.status != SimulatorStatus.Booted }
        val onlineEmulators = adbWrapper.listDevices()
        val offlineEmulators =
            androidEmulatorWrapper
                .listAvdsAndWait()
                .filter { avdName ->
                    !onlineEmulators.any { it.name == avdName }
                }.map {
                    Device.AndroidEmulator(
                        name = it,
                        status = EmulatorStatus.Offline,
                    )
                }
        return listOf(Device.Web) +
            onlineEmulators +
            onlineSimulators +
            offlineEmulators +
            offlineSimulators
    }

    private suspend fun runAppOnAndroidDevice(
        appDir: File,
        device: Device.AndroidEmulator,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        portNumber: Int,
        packageName: String,
        projectName: String,
        localJavaHomePath: PathSetting,
    ) = withContext(ioDispatcher) {
        val avds = androidEmulatorWrapper.listAvdsAndWait()
        if (avds.isEmpty()) {
            onStatusBarUiStateChanged(
                StatusBarUiState.Failure("No available emulators. Set up an emulator in Android Studio or from command line"),
            )
            throw CancellationException("No available emulators")
        }
        // When launching an emulator, emulator command tends to run indefinitely as it keeps the
        // emulator open. Thus, when the emulator hasn't launched, the process will wait until the
        // emulator is manually closed.
        // To avoid that it doesn't wait the completion of the process.
        // Instead, it checks the emulator status by calling adb devices in the next while loop
        if (device.status == EmulatorStatus.Offline) {
            androidEmulatorWrapper.runAvd(device.name, portNumber = portNumber)
        }

        var devices: List<Device.AndroidEmulator>?
        try {
            withTimeoutOrNull(30_000L) {
                // timeout after 30 seconds
                while (isActive) { // continue looping as long as the coroutine is active
                    devices = adbWrapper.listDevices()
                    if (devices?.isNotEmpty() == true &&
                        devices!!.firstOrNull { it.status.portNumber == portNumber }?.status == EmulatorStatus.Device
                    ) {
                        onStatusBarUiStateChanged(StatusBarUiState.Loading("Device ${device.name} is online"))
                        break // break the loop if the result is not empty
                    }
                    yield()
                    onStatusBarUiStateChanged(StatusBarUiState.Loading("Waiting for ${device.name} to come online"))
                    delay(1000L) // delay for a certain time (e.g., 1 second) before retrying
                }
            }
        } catch (e: TimeoutCancellationException) {
            onStatusBarUiStateChanged(StatusBarUiState.Failure("Failed to launch emulator. Check if emulator is running"))
            throw e
        }

        val gradleWrapper =
            GradleCommandLineRunner(
                projectRoot = appDir,
                buildLogger = buildLogger,
                localJavaHomePath = localJavaHomePath.path(),
            )

        gradleWrapper.assembleDebug(
            onStatusBarUiStateChanged,
        )
        adbWrapper.installApk(
            appDir = appDir,
            deviceId = device.adbTarget(),
        )
        adbWrapper.launchActivity(
            deviceId = device.adbTarget(),
            "$packageName.$projectName",
            "$packageName.MainActivity",
        )
    }

    private suspend fun runJsAppOnBrowser(
        appDir: File,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        localJavaHomePath: PathSetting,
    ) = withContext(ioDispatcher) {
        val gradleWrapper =
            GradleCommandLineRunner(
                projectRoot = appDir,
                buildLogger = buildLogger,
                localJavaHomePath = localJavaHomePath.path(),
            )
        gradleWrapper.jsBrowserDevelopmentRun(
            onStatusBarUiStateChanged,
        )
    }

    private fun addComposeBuilderImplementation(
        appDir: File,
        fileSpecs: List<FileSpecWithDirectory>,
        formatCode: Boolean = true,
    ) {
        fileSpecs.forEach {
            try {
                val outputDir = appDir.resolve(it.baseDirectory.directoryName)
                val ktFile =
                    outputDir
                        .resolve(it.fileSpec.packageName.replace(".", File.separator))
                        .resolve("${it.fileSpec.name}.kt")
                ktFile.parentFile?.mkdirs()
                val fileContent =
                    if (formatCode) {
                        FormatterWrapper.format(
                            fileName = it.fileSpec.name,
                            text = it.fileSpec.toString(),
                            isScript = false,
                        )
                    } else {
                        it.fileSpec.toString()
                    }
                ktFile.writeText(fileContent)
            } catch (e: Exception) {
                Logger.e("Failed to create file: $it", e)
            }
        }
    }

    private fun prepareAppDir(
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray> = emptyMap(),
        firebaseAppInfo: FirebaseAppInfo,
    ): File {
        val zipStream =
            object {}.javaClass.getResourceAsStream("/app-template.zip")
                ?: throw IllegalStateException("Couldn't find a zip file at. /app-template.zip")
        val tempDir = createTempDirectory().toFile()
        zipStream.unzip(tempDir.toPath())

        val appTemplateDir = tempDir.resolve("app-template")
        val settingsGradleKts = tempDir.resolve("app-template").resolve("settings.gradle.kts")
        val buildGradleKts = appTemplateDir.resolve("composeApp").resolve("build.gradle.kts")
        val mainApplication =
            appTemplateDir.resolve("composeApp/src/androidMain/kotlin/MainApplication.kt")
        val stringsXml = appTemplateDir.resolve("composeApp/src/androidMain/res/values/strings.xml")
        val googleServicesJson =
            appTemplateDir.resolve("composeApp").resolve("google-services.json")
        val googleServiceInfoPlist =
            appTemplateDir.resolve("iosApp").resolve("iosApp").resolve("GoogleService-Info.plist")
        replacePackageAndProject(
            file = buildGradleKts,
            packageName = packageName,
            projectName = projectName,
        )
        replacePackageAndProject(
            file = settingsGradleKts,
            packageName = packageName,
            projectName = projectName,
        )
        replacePackageAndProject(
            file = mainApplication,
            packageName = packageName,
            projectName = projectName,
        )
        replacePackageAndProject(
            file = stringsXml,
            packageName = packageName,
            projectName = projectName,
        )
        replacePackageAndProject(
            file = googleServicesJson,
            packageName = packageName,
            projectName = projectName,
        )
        replacePackageAndProject(
            file = googleServiceInfoPlist,
            packageName = packageName,
            projectName = projectName,
        )

        copyFiles(appTemplateDir, copyInstructions)
        copyLocalFiles(appTemplateDir, copyLocalFileInstructions)
        writeFiles(appTemplateDir, writeFileInstructions)

        val iosXCConfig = appTemplateDir.resolve("iosApp/Configuration/Config.xcconfig")
        replacePackageAndProject(
            file = iosXCConfig,
            packageName = packageName,
            projectName = projectName,
        )
        moveFile(
            sourceFile = mainApplication,
            targetFile =
                appTemplateDir
                    .resolve("composeApp/src/androidMain/kotlin")
                    .resolve(packageName.replace(".", "/"))
                    .resolve("MainApplication.kt"),
        )
        handleFirebaseConfig(
            packageName = packageName,
            projectName = projectName,
            firebaseAppInfo,
            googleServicesJson,
            googleServiceInfoPlist,
            appTemplateDir,
        )

        val gradlew = appTemplateDir.resolve("gradlew")
        if (gradlew.exists()) {
            gradlew.setExecutable(true)
        }

        return appTemplateDir
    }

    private fun handleFirebaseConfig(
        packageName: String,
        projectName: String,
        firebaseAppInfo: FirebaseAppInfo,
        googleServicesJson: File,
        googleServiceInfoPlist: File,
        appTemplateDir: File,
    ) {
        firebaseAppInfo.androidApp?.let { firebaseAndroidApp ->
            googleServicesJson.writeText(firebaseAndroidApp.config)
            val clientId =
                extractClientIdFromGoogleServicesJson(
                    firebaseAndroidApp.config,
                    packageName = "$packageName.$projectName",
                )
            val appInitializer =
                appTemplateDir
                    .resolve("composeApp")
                    .resolve("src/commonMain/kotlin/io/composeflow/AppInitializer.kt")
            appInitializer.writeText(
                appInitializer.readText().replace("\"OAUTH_CLIENT_ID\"", clientId ?: "\"\""),
            )
        }
        val infoPlist = appTemplateDir.resolve("iosApp").resolve("iosApp").resolve("Info.plist")
        val placeHolderString = "<key>firebasePlaceholder</key><string>firebasePlaceholder</string>"
        firebaseAppInfo.iOSApp?.let { firebaseIosApp ->
            googleServiceInfoPlist.writeText(firebaseIosApp.config)

            val plistMap = PlistUtil.parsePlistToMap(firebaseIosApp.config)
            val firebaseConfigInPlist = """
    <key>GIDClientID</key>
    <string>${plistMap["CLIENT_ID"]}</string>
    <key>CFBundleURLTypes</key>
    <array>
      <dict>
        <key>CFBundleURLSchemes</key>
        <array>
          <string>${plistMap["REVERSED_CLIENT_ID"]}</string>
        </array>
      </dict>
    </array>"""
            infoPlist.writeText(
                infoPlist.readText().replace(placeHolderString, firebaseConfigInPlist),
            )
        } ?: run {
            infoPlist.writeText(infoPlist.readText().replace(placeHolderString, ""))
        }

        firebaseAppInfo.webApp?.let {
            val webAppConfig = jsonSerializer.parseToJsonElement(it.config)
            val map = (webAppConfig as JsonObject).toMap()
            val jsAuthInitializer =
                appTemplateDir
                    .resolve("composeApp")
                    .resolve("src/jsMain/kotlin/io/composeflow/platform/AuthInitializer.js.kt")
            jsAuthInitializer.writeText(
                jsAuthInitializer
                    .readText()
                    .replace("\"WEB_APPLICATION_ID\"", map["appId"].toString())
                    .replace("\"API_KEY\"", map["apiKey"].toString())
                    .replace("\"AUTH_DOMAIN\"", map["authDomain"].toString())
                    .replace("\"PROJECT_ID\"", map["projectId"].toString())
                    .replace("\"STORAGE_BUCKET\"", map["storageBucket"].toString()),
            )
        }
    }

    private fun copyFiles(
        targetDirectory: File,
        copyInstructions: Map<String, String>,
    ) {
        copyInstructions.forEach {
            val sourceFile = object {}.javaClass.getResourceAsStream(it.key)
            val destFile = targetDirectory.resolve(it.value)

            destFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }

            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            sourceFile.use { input ->
                destFile.outputStream().use { output ->
                    input?.copyTo(output)
                    Logger.v("File copied from ${it.key} to ${it.value}")
                }
            }
        }
    }

    private fun copyLocalFiles(
        targetDirectory: File,
        copyLocalFileInstructions: Map<String, String>,
    ) {
        copyLocalFileInstructions.forEach {
            val sourceFile = File(it.key)
            val destFile = targetDirectory.resolve(it.value)
            destFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            if (sourceFile.exists()) {
                sourceFile.copyTo(destFile, overwrite = true)
                Logger.v("File copied from ${it.key} to ${it.value}")
            }
        }
    }

    private fun writeFiles(
        targetDirectory: File,
        writeFileInstructions: Map<String, ByteArray>,
    ) {
        writeFileInstructions.forEach { (path, content) ->
            val destFile = targetDirectory.resolve(path)
            destFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            destFile.writeBytes(content)
            Logger.v("File written to $path")
        }
    }

    private fun replaceTextInFile(
        file: File,
        oldText: String,
        newText: String,
    ) {
        if (file.exists()) {
            val fileContent = file.readText()
            val newContent = fileContent.replace(oldText, newText)
            file.writeText(newContent)
        } else {
            Logger.w("Trying to replace the old text: '$oldText' with new text '$newText', but the file: $file  doesn't exist")
        }
    }

    private fun replacePackageAndProject(
        file: File,
        packageName: String,
        projectName: String,
    ) {
        replaceTextInFile(file, "packageName", packageName)
        replaceTextInFile(file, "projectName", projectName)
    }

    @Suppress("unused")
    private fun moveFile(
        sourceFile: File,
        targetFile: File,
    ) {
        // Ensure the parent directory of the target file exists
        val targetDir = targetFile.parentFile
        if (targetDir != null && !targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw Exception("Failed to create target directory: ${targetDir.absolutePath}")
            }
        }

        if (sourceFile.renameTo(targetFile)) {
            Logger.v("File moved using renameTo: $targetFile")
        } else {
            // Fallback to manual copy and delete if renameTo fails
            sourceFile.copyTo(targetFile, overwrite = true)
            if (sourceFile.delete()) {
                Logger.v("File moved using copy and delete.")
            } else {
                throw Exception("Failed to delete the source file after copying.")
            }
        }
    }

    private fun prepareDownloadDir(): File {
        val downloadDir = File(System.getProperty("user.home") + "/Downloads")
        return if (downloadDir.exists() && downloadDir.isDirectory) {
            downloadDir
        } else {
            createTempDirectory().toFile()
        }
    }
}
