package io.composeflow.ui.toolbar

import io.composeflow.device.Device
import io.composeflow.model.settings.PathSetting

data class PreviewAppParams(
    val projectFileName: String,
    val targetDevice: Device,
    val availableDevices: List<Device>,
    val javaHomePath: PathSetting?,
)

sealed interface PreviewAvailability {
    val previewAppParams: PreviewAppParams

    data class Available(
        override val previewAppParams: PreviewAppParams,
    ) : PreviewAvailability

    data class JdkNotInstalled(
        override val previewAppParams: PreviewAppParams,
    ) : PreviewAvailability
}
