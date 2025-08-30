package io.composeflow.appbuilder

import io.composeflow.device.Device
import io.composeflow.kotlinpoet.FileSpecWithDirectory
import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirebaseAppInfo
import io.composeflow.model.settings.PathSetting
import io.composeflow.ui.statusbar.StatusBarUiState

actual object AppRunner {
    actual suspend fun runAndroidApp(
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
    ) {
        // No-op for WASM
        onStatusBarUiStateChanged(StatusBarUiState.Failure("Android app building is not supported in web version"))
    }

    actual suspend fun runIosApp(
        device: Device.IosSimulator,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        // No-op for WASM
        onStatusBarUiStateChanged(StatusBarUiState.Failure("iOS app building is not supported in web version"))
    }

    actual suspend fun runJsApp(
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
        localJavaHomePath: PathSetting,
    ) {
        // No-op for WASM
        onStatusBarUiStateChanged(StatusBarUiState.Failure("JS app building is not supported in web version"))
    }

    actual fun buildApp(
        fileSpecs: List<FileSpecWithDirectory>,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        // No-op for WASM
    }

    actual suspend fun downloadCode(
        project: Project,
        fileSpecs: List<FileSpecWithDirectory>,
        onStatusBarUiStateChanged: (StatusBarUiState) -> Unit,
        packageName: String,
        projectName: String,
        copyInstructions: Map<String, String>,
        copyLocalFileInstructions: Map<String, String>,
        writeFileInstructions: Map<String, ByteArray>,
        firebaseAppInfo: FirebaseAppInfo,
    ) {
        // No-op for WASM
        onStatusBarUiStateChanged(StatusBarUiState.Failure("Code download is not supported in web version"))
    }

    actual suspend fun getAvailableDevices(): List<Device> {
        // Only web device is available in WASM
        return listOf(Device.Web)
    }
}
