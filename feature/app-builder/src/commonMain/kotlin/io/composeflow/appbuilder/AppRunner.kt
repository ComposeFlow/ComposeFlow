package io.composeflow.appbuilder

import io.composeflow.device.Device
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.settings.PathSetting
import io.composeflow.ui.statusbar.StatusBarUiState

expect object AppRunner {
    suspend fun runAndroidApp(
        device: Device.AndroidEmulator,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        portNumber: Int,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
        localJavaHomePath: PathSetting,
    )

    suspend fun runIosApp(
        device: Device.IosSimulator,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    )

    suspend fun runJsApp(
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
        localJavaHomePath: PathSetting,
    )

    fun buildApp(
        fileSpecs: List<FileSpecWithDirectory>,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    )

    suspend fun downloadCode(
        project: Project,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    )

    suspend fun getAvailableDevices(): List<Device>
}
